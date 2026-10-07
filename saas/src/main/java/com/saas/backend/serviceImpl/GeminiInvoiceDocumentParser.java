package com.saas.backend.serviceImpl;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.backend.dto.BankDetailsDto;
import com.saas.backend.dto.InvoiceExtractionResult;
import com.saas.backend.dto.PaymentPlanItemDto;
import com.saas.backend.service.InvoiceDocumentParser;

import lombok.extern.slf4j.Slf4j;

/**
 * Gemini AI Implementation of InvoiceDocumentParser.
 * Connects directly to Google Gemini API (gemini-2.5-flash) using Spring RestClient.
 * 
 * Supports:
 * - Native multimodal PDF parsing via base64 inline_data
 * - Native Excel (.xlsx, .xls) parsing via Apache POI tabular extraction
 * - Resilient fallback: returns structured failure metadata if API key is missing
 *   or file cannot be parsed, allowing manual user entry without data loss.
 */
@Slf4j
@Service
public class GeminiInvoiceDocumentParser implements InvoiceDocumentParser {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-3.5-flash-lite}")
    private String modelName;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiUrl;

    public GeminiInvoiceDocumentParser(ObjectMapper objectMapper) {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofSeconds(5));
        factory.setReadTimeout(java.time.Duration.ofSeconds(15));
        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String getParserName() {
        return "GEMINI_AI";
    }

    @Override
    public InvoiceExtractionResult parseInvoice(byte[] fileBytes, String fileName, String mimeType) {
        if (!isAvailable()) {
            log.warn("Gemini AI API key is not configured. Falling back to manual invoice entry.");
            return InvoiceExtractionResult.builder()
                    .success(false)
                    .errorMessage("Gemini API key is not configured in .env. Please configure GEMINI_API_KEY or enter details manually.")
                    .build();
        }

        if (fileBytes == null || fileBytes.length == 0) {
            return InvoiceExtractionResult.builder()
                    .success(false)
                    .errorMessage("Empty file provided for invoice parsing.")
                    .build();
        }

        try {
            boolean isExcel = isExcelFile(fileName, mimeType);
            boolean isPdf = isPdfFile(fileName, mimeType);

            if (!isPdf && !isExcel) {
                return InvoiceExtractionResult.builder()
                        .success(false)
                        .errorMessage("Unsupported file type (" + fileName + "). Please provide a PDF or Excel document.")
                        .build();
            }

            Map<String, Object> requestBody;
            if (isPdf) {
                requestBody = buildPdfGeminiRequest(fileBytes);
            } else {
                String excelText = extractTextFromExcel(fileBytes);
                requestBody = buildExcelGeminiRequest(excelText);
            }

            String responseString = callGeminiWithFallback(requestBody);
            return parseGeminiResponse(responseString);

        } catch (Exception e) {
            log.error("Failed to parse invoice using Gemini AI for file {}: {}", fileName, e.getMessage(), e);
            return InvoiceExtractionResult.builder()
                    .success(false)
                    .errorMessage("AI extraction encountered an error: " + e.getMessage() + ". You can enter the invoice details manually.")
                    .build();
        }
    }

    private boolean isPdfFile(String fileName, String mimeType) {
        if (mimeType != null && mimeType.toLowerCase().contains("pdf")) return true;
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }

    private boolean isExcelFile(String fileName, String mimeType) {
        if (mimeType != null && (mimeType.toLowerCase().contains("spreadsheet") || mimeType.toLowerCase().contains("excel"))) {
            return true;
        }
        if (fileName != null) {
            String lower = fileName.toLowerCase();
            return lower.endsWith(".xlsx") || lower.endsWith(".xls");
        }
        return false;
    }

    private String extractTextFromExcel(byte[] fileBytes) {
        return ExcelDocumentReader.extractText(fileBytes);
    }

    private String callGeminiWithFallback(Map<String, Object> requestBody) throws Exception {
        List<String> modelsToTry = new ArrayList<>();
        if (modelName != null && !modelName.isBlank()) {
            modelsToTry.add(modelName.trim());
        }
        for (String fallback : List.of("gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-3.8-flash", "gemini-3.7-flash")) {
            if (!modelsToTry.contains(fallback)) {
                modelsToTry.add(fallback);
            }
        }

        Exception lastException = null;
        for (String candidateModel : modelsToTry) {
            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    String url = String.format("%s/%s:generateContent?key=%s", apiUrl, candidateModel, apiKey);
                    String responseString = restClient.post()
                            .uri(URI.create(url))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .body(requestBody)
                            .retrieve()
                            .body(String.class);

                    if (responseString != null && !responseString.isBlank()) {
                        log.info("Successfully received invoice parsing response using model {}", candidateModel);
                        return responseString;
                    }
                } catch (Exception ex) {
                    lastException = ex;
                    log.warn("Gemini model {} attempt {} failed: {}", candidateModel, attempt, ex.getMessage());
                    if (ex.getMessage() != null && ex.getMessage().contains("503") && attempt == 1) {
                        try {
                            Thread.sleep(1200);
                        } catch (InterruptedException ignored) {}
                    }
                }
            }
        }
        throw lastException != null ? lastException : new RuntimeException("All Gemini models failed to generate content.");
    }

    private Map<String, Object> buildPdfGeminiRequest(byte[] pdfBytes) {
        String base64Data = Base64.getEncoder().encodeToString(pdfBytes);

        Map<String, Object> inlineData = Map.of(
                "mime_type", "application/pdf",
                "data", base64Data
        );

        Map<String, Object> part1 = Map.of("inline_data", inlineData);
        Map<String, Object> part2 = Map.of("text", getSystemExtractionPrompt());

        Map<String, Object> content = Map.of("parts", List.of(part1, part2));
        Map<String, Object> generationConfig = Map.of(
                "response_mime_type", "application/json",
                "temperature", 0.1
        );

        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(content));
        request.put("generationConfig", generationConfig);
        return request;
    }

    private Map<String, Object> buildExcelGeminiRequest(String excelText) {
        String prompt = "Here is the spreadsheet content of an accommodation/lodge invoice:\n\n"
                + excelText + "\n\n"
                + getSystemExtractionPrompt();

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> generationConfig = Map.of(
                "response_mime_type", "application/json",
                "temperature", 0.1
        );

        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(content));
        request.put("generationConfig", generationConfig);
        return request;
    }

    private String getSystemExtractionPrompt() {
        return """
You are an expert financial auditor and accounts payable document reader specializing in the East African and African safari hospitality sector.
Carefully inspect this accommodation supplier invoice / proforma / bill and extract all key payment coordinates into JSON adhering strictly to this schema:

{
  "invoiceNumber": "string (The official invoice number or reference, e.g. 'INV-2024-001', or null)",
  "amount": number (Total gross amount due, e.g. 2500.00, or null),
  "currency": "string (3-letter code, e.g. 'USD', 'TZS', 'KES', 'EUR', or null)",
  "dueDate": "string (YYYY-MM-DD or null)",
  "bankDetails": {
    "bankName": "string (e.g. 'Stanbic Bank Tanzania', 'CRDB', 'Standard Chartered')",
    "accountName": "string (Beneficiary account name, e.g. 'Serengeti Serena Safari Lodge Ltd')",
    "accountNumber": "string (Exact bank account number, clean of spaces)",
    "currency": "string (Account currency, e.g. 'USD')",
    "swiftCode": "string (SWIFT/BIC code, e.g. 'SBICETZX')",
    "branch": "string (Branch name, e.g. 'Arusha Branch')",
    "iban": "string or null",
    "country": "string (e.g. 'Tanzania')",
    "intermediaryBankName": "string or null",
    "intermediarySwiftCode": "string or null",
    "lipaNamba": "string (M-Pesa / Till / Paybill number if present, else null)",
    "notes": "string (Any mandatory payment reference instructions)"
  },
  "paymentPlan": [
    {
      "milestone": "string (e.g. 'Deposit (30%)' or 'Balance (70%)' or 'Full Payment')",
      "percentage": number (e.g. 30.0),
      "amount": number (e.g. 750.00),
      "dueDate": "string (YYYY-MM-DD or null)",
      "notes": "string or null"
    }
  ],
  "confidenceScore": number (Between 0.0 and 1.0 indicating clarity of extracted banking details),
  "extractionNotes": "string (Summary of detected items or any warnings)"
}

Rules:
1. Ensure the account number, bank name, and account name are exact and not truncated.
2. If multiple bank accounts exist for different currencies (e.g. USD vs TZS), select the one matching the invoice currency (typically USD for safaris).
3. If no payment plan is divided into installments, provide 1 item representing 100% of the total amount.
4. Output valid JSON only.
""";
    }

    private InvoiceExtractionResult parseGeminiResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
                return InvoiceExtractionResult.builder()
                        .success(false)
                        .errorMessage("No response candidates returned by Gemini.")
                        .build();
            }

            JsonNode firstCandidate = candidates.get(0);
            JsonNode content = firstCandidate.path("content");
            JsonNode parts = content.path("parts");
            if (parts.isMissingNode() || !parts.isArray() || parts.isEmpty()) {
                return InvoiceExtractionResult.builder()
                        .success(false)
                        .errorMessage("Gemini response did not contain content parts.")
                        .build();
            }

            String jsonText = parts.get(0).path("text").asText().trim();
            int firstBrace = jsonText.indexOf('{');
            int lastBrace = jsonText.lastIndexOf('}');
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                jsonText = jsonText.substring(firstBrace, lastBrace + 1);
            } else {
                if (jsonText.startsWith("```json")) {
                    jsonText = jsonText.substring(7);
                } else if (jsonText.startsWith("```")) {
                    jsonText = jsonText.substring(3);
                }
                if (jsonText.endsWith("```")) {
                    jsonText = jsonText.substring(0, jsonText.length() - 3);
                }
                jsonText = jsonText.trim();
            }

            JsonNode dataNode = objectMapper.readTree(jsonText);

            String invoiceNumber = dataNode.path("invoiceNumber").asText(null);
            BigDecimal amount = dataNode.hasNonNull("amount") ? BigDecimal.valueOf(dataNode.path("amount").asDouble()) : null;
            String currency = dataNode.path("currency").asText(null);

            LocalDate dueDate = null;
            if (dataNode.hasNonNull("dueDate") && !dataNode.path("dueDate").asText().isBlank()) {
                try {
                    dueDate = LocalDate.parse(dataNode.path("dueDate").asText(), DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception ignored) {}
            }

            BankDetailsDto bankDetails = null;
            if (dataNode.has("bankDetails") && !dataNode.path("bankDetails").isNull()) {
                JsonNode b = dataNode.path("bankDetails");
                bankDetails = BankDetailsDto.builder()
                        .bankName(b.path("bankName").asText(null))
                        .accountName(b.path("accountName").asText(null))
                        .accountNumber(b.path("accountNumber").asText(null))
                        .currency(b.path("currency").asText(null))
                        .swiftCode(b.path("swiftCode").asText(null))
                        .branch(b.path("branch").asText(null))
                        .iban(b.path("iban").asText(null))
                        .country(b.path("country").asText(null))
                        .intermediaryBankName(b.path("intermediaryBankName").asText(null))
                        .intermediarySwiftCode(b.path("intermediarySwiftCode").asText(null))
                        .lipaNamba(b.path("lipaNamba").asText(null))
                        .notes(b.path("notes").asText(null))
                        .build();
            }

            List<PaymentPlanItemDto> paymentPlan = new ArrayList<>();
            if (dataNode.has("paymentPlan") && dataNode.path("paymentPlan").isArray()) {
                for (JsonNode p : dataNode.path("paymentPlan")) {
                    BigDecimal planPct = p.hasNonNull("percentage") ? BigDecimal.valueOf(p.path("percentage").asDouble()) : null;
                    BigDecimal planAmt = p.hasNonNull("amount") ? BigDecimal.valueOf(p.path("amount").asDouble()) : null;
                    LocalDate planDueDate = null;
                    if (p.hasNonNull("dueDate") && !p.path("dueDate").asText().isBlank()) {
                        try {
                            planDueDate = LocalDate.parse(p.path("dueDate").asText(), DateTimeFormatter.ISO_LOCAL_DATE);
                        } catch (Exception ignored) {}
                    }
                    paymentPlan.add(PaymentPlanItemDto.builder()
                            .milestone(p.path("milestone").asText("Installment"))
                            .percentage(planPct)
                            .amount(planAmt)
                            .dueDate(planDueDate)
                            .status("PENDING")
                            .notes(p.path("notes").asText(null))
                            .build());
                }
            }

            Double confidenceScore = dataNode.hasNonNull("confidenceScore")
                    ? dataNode.path("confidenceScore").asDouble()
                    : 0.90;
            String extractionNotes = dataNode.path("extractionNotes").asText(null);

            return InvoiceExtractionResult.builder()
                    .success(true)
                    .invoiceNumber(invoiceNumber)
                    .amount(amount)
                    .currency(currency)
                    .dueDate(dueDate)
                    .bankDetails(bankDetails)
                    .paymentPlan(paymentPlan)
                    .confidenceScore(confidenceScore)
                    .extractionNotes(extractionNotes)
                    .rawResponse(jsonText)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse Gemini JSON output: {}", e.getMessage(), e);
            return InvoiceExtractionResult.builder()
                    .success(false)
                    .errorMessage("Failed to deserialize Gemini extraction: " + e.getMessage())
                    .build();
        }
    }
}
