package com.saas.backend.serviceImpl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.backend.dto.EmailAnalysisResult;
import com.saas.backend.models.EmailResponseType;
import com.saas.backend.service.EmailResponseDetectionService;

import lombok.extern.slf4j.Slf4j;

/**
 * Intelligent Email Response Detection Service.
 * Combines Gemini AI understanding with resilient heuristic fallback to ensure
 * incoming booking emails are parsed reliably and never lost.
 */
@Slf4j
@Service
public class EmailResponseDetectionServiceImpl implements EmailResponseDetectionService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-3.5-flash-lite}")
    private String modelName;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiUrl;

    public EmailResponseDetectionServiceImpl(ObjectMapper objectMapper) {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofSeconds(4));
        factory.setReadTimeout(java.time.Duration.ofSeconds(7));
        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public EmailResponseType detectResponse(String subject, String body) {
        return analyzeEmailResponse(subject, body).getResponseType();
    }

    @Override
    public EmailAnalysisResult analyzeEmailResponse(String subject, String body) {
        // 1. Primary Strategy: Gemini AI Analysis
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                EmailAnalysisResult aiResult = callGeminiForEmailAnalysis(subject, body);
                if (aiResult != null && aiResult.getResponseType() != null && aiResult.getResponseType() != EmailResponseType.UNKNOWN) {
                    log.info("Gemini AI successfully classified email intent as {} (confNo={})",
                            aiResult.getResponseType(), aiResult.getConfirmationNumber());
                    return aiResult;
                }
            } catch (Exception ex) {
                log.warn("Gemini AI email analysis failed ({}). Gracefully falling back to rule-based keyword detection.", ex.getMessage());
            }
        } else {
            log.debug("Gemini API key is not configured. Falling back to rule-based keyword detection.");
        }

        // 2. Resilient Fallback Strategy: Rule-based Heuristic Keyword Matching
        return analyzeHeuristically(subject, body);
    }

    private EmailAnalysisResult callGeminiForEmailAnalysis(String subject, String body) throws Exception {
        String prompt = "You are an AI assistant for a safari tour operator operations system. " +
                "You analyze email replies from hotels and lodges regarding accommodation booking requests.\n" +
                "Analyze the following incoming email and classify the response:\n" +
                "- CONFIRMED: The lodge accepted, reserved, or confirmed the rooms or reservation request.\n" +
                "- DECLINED: The lodge declined, rejected, cancelled, or is fully booked / unavailable / has no vacancy for the requested dates.\n" +
                "- NEED_MORE_INFORMATION: The lodge asks for additional details, guest names, dates clarification, dietary requirements, or flight times before confirming.\n" +
                "- UNKNOWN: The email content is unrelated, ambiguous, automated autoreply, or not clear.\n\n" +
                "Also extract:\n" +
                "- confirmationNumber: Any booking confirmation reference, voucher number, or reservation code mentioned by the lodge (or null if none).\n" +
                "- bookingReference: Any booking reference code like ACC-BOOK-XXXX-XXXXXX found in the text (or null if none).\n" +
                "- summary: A 1-2 sentence concise operational summary of the lodge's reply.\n\n" +
                "EMAIL SUBJECT: " + (subject != null ? subject : "") + "\n" +
                "EMAIL BODY:\n" + (body != null ? body : "") + "\n\n" +
                "Return ONLY a valid JSON object in this exact schema without markdown formatting or surrounding code blocks:\n" +
                "{\n" +
                "  \"intent\": \"CONFIRMED | DECLINED | NEED_MORE_INFORMATION | UNKNOWN\",\n" +
                "  \"confirmationNumber\": \"string or null\",\n" +
                "  \"bookingReference\": \"string or null\",\n" +
                "  \"summary\": \"string\"\n" +
                "}";

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contentPart = Collections.singletonMap("text", prompt);
        Map<String, Object> content = Collections.singletonMap("parts", Collections.singletonList(contentPart));
        requestBody.put("contents", Collections.singletonList(content));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("temperature", 0.1);
        requestBody.put("generationConfig", generationConfig);

        String responseString = callGeminiWithFallback(requestBody);
        return parseGeminiResponse(responseString);
    }

    private String callGeminiWithFallback(Map<String, Object> requestBody) throws Exception {
        List<String> modelsToTry = new ArrayList<>();
        if (modelName != null && !modelName.isBlank()) {
            modelsToTry.add(modelName.trim());
        }
        for (String candidate : List.of("gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-3.8-flash", "gemini-3.7-flash")) {
            if (!modelsToTry.contains(candidate)) {
                modelsToTry.add(candidate);
            }
        }

        Exception lastException = null;
        for (String candidateModel : modelsToTry) {
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
                    return responseString;
                }
            } catch (Exception ex) {
                lastException = ex;
                log.warn("Gemini model {} failed ({}). Trying next candidate.", candidateModel, ex.getMessage());
            }
        }
        throw lastException != null ? lastException : new RuntimeException("All Gemini models failed to generate content.");
    }

    private EmailAnalysisResult parseGeminiResponse(String responseString) {
        try {
            JsonNode root = objectMapper.readTree(responseString);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return null;
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                return null;
            }

            String jsonText = parts.get(0).path("text").asText("").trim();
            if (jsonText.startsWith("```json")) {
                jsonText = jsonText.substring(7);
            }
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.substring(3);
            }
            if (jsonText.endsWith("```")) {
                jsonText = jsonText.substring(0, jsonText.length() - 3);
            }
            jsonText = jsonText.trim();

            JsonNode parsed = objectMapper.readTree(jsonText);
            String intentStr = parsed.path("intent").asText("UNKNOWN").toUpperCase().trim();
            EmailResponseType responseType;
            try {
                responseType = EmailResponseType.valueOf(intentStr);
            } catch (Exception e) {
                responseType = EmailResponseType.UNKNOWN;
            }

            String confNo = parsed.path("confirmationNumber").isNull() ? null : parsed.path("confirmationNumber").asText(null);
            String bookingRef = parsed.path("bookingReference").isNull() ? null : parsed.path("bookingReference").asText(null);
            String summary = parsed.path("summary").asText("");

            return EmailAnalysisResult.builder()
                    .responseType(responseType)
                    .confirmationNumber(confNo != null && !confNo.equalsIgnoreCase("null") && !confNo.isBlank() ? confNo.trim() : null)
                    .bookingReference(bookingRef != null && !bookingRef.equalsIgnoreCase("null") && !bookingRef.isBlank() ? bookingRef.trim() : null)
                    .notes(summary)
                    .aiProcessed(true)
                    .needsManualReview(responseType == EmailResponseType.UNKNOWN || responseType == EmailResponseType.NEED_MORE_INFORMATION)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse Gemini JSON output: {}", e.getMessage());
            return null;
        }
    }

    private EmailAnalysisResult analyzeHeuristically(String subject, String body) {
        String content = ((subject == null ? "" : subject) + " " + (body == null ? "" : body)).toLowerCase(Locale.ROOT);

        EmailResponseType type;
        String notes;
        boolean needsManualReview = false;

        if (containsDeclinedKeywords(content)) {
            type = EmailResponseType.DECLINED;
            notes = "Heuristic matched: Property reported no availability or declined reservation.";
        } else if (containsConfirmedKeywords(content)) {
            type = EmailResponseType.CONFIRMED;
            notes = "Heuristic matched: Property accepted and confirmed booking.";
        } else if (containsMoreInformationKeywords(content)) {
            type = EmailResponseType.NEED_MORE_INFORMATION;
            notes = "Heuristic matched: Property requested additional information.";
            needsManualReview = true;
        } else {
            type = EmailResponseType.UNKNOWN;
            notes = "Response content ambiguous. Manual review required.";
            needsManualReview = true;
        }

        String confNo = extractConfirmationNumberHeuristic(subject, body);

        return EmailAnalysisResult.builder()
                .responseType(type)
                .confirmationNumber(confNo)
                .notes(notes)
                .aiProcessed(false)
                .needsManualReview(needsManualReview)
                .build();
    }

    private boolean containsDeclinedKeywords(String content) {
        return content.contains("not available")
                || content.contains("unavailable")
                || content.contains("fully booked")
                || content.contains("no availability")
                || content.contains("no vacancy")
                || content.contains("full on those dates")
                || content.contains("unable to accommodate")
                || content.contains("cannot accommodate")
                || content.contains("cannot accept")
                || content.contains("reservation declined")
                || content.contains("request declined")
                || (content.contains("unfortunately") && !content.contains("available"));
    }

    private boolean containsConfirmedKeywords(String content) {
        return content.contains("accommodation confirmed")
                || content.contains("booking confirmed")
                || content.contains("request confirmed")
                || content.contains("reservation is confirmed")
                || content.contains("booking is confirmed")
                || content.contains("we confirm your reservation")
                || content.contains("reservation has been confirmed")
                || content.contains("booking has been confirmed")
                || content.contains("confirm availability")
                || content.contains("confirmed availability")
                || content.contains("pleased to confirm")
                || content.contains("happy to confirm")
                || content.contains("delighted to hold")
                || content.contains("rooms are held")
                || content.contains("rooms will be held")
                || content.contains("held until")
                || content.contains("reservation is secured")
                || content.contains("confirmed your booking")
                || content.contains("booking is accepted")
                || content.contains("booking accepted")
                || content.contains("accept the booking")
                || content.contains("accept reservation")
                || content.contains("acceptade")
                || (content.contains("confirmation number") && !content.contains("not confirmed"));
    }

    private boolean containsMoreInformationKeywords(String content) {
        return content.contains("please provide")
                || content.contains("more information")
                || content.contains("additional information")
                || content.contains("need more information")
                || content.contains("please confirm the following")
                || content.contains("kindly provide")
                || content.contains("guest names required");
    }

    private String extractConfirmationNumberHeuristic(String subject, String body) {
        String full = (body != null ? body : "") + "\n" + (subject != null ? subject : "");

        // 1. Explicit voucher or confirmation reference with colon/dash/hash separator
        Pattern explicitPattern = Pattern.compile(
                "(?i)(?:lodge\\s+)?(?:confirmation|conf|voucher|reservation|res)\\s*(?:#|no|number|num|code|ref)?\\s*[:\\-=#]\\s*([A-Za-z0-9\\-_]{3,35})"
        );
        Matcher matcher = explicitPattern.matcher(full);
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (!isBlacklistedCodeWord(candidate)) {
                return candidate;
            }
        }

        // 2. Generic confirmation label followed by code
        Pattern genericPattern = Pattern.compile(
                "(?i)(?:confirmation|conf|voucher)\\s+code\\s*[:\\-\\s]?\\s*([A-Za-z0-9\\-_]{3,35})"
        );
        Matcher m2 = genericPattern.matcher(full);
        while (m2.find()) {
            String candidate = m2.group(1).trim();
            if (!isBlacklistedCodeWord(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isBlacklistedCodeWord(String word) {
        if (word == null || word.length() < 3) return true;
        String lower = word.toLowerCase(Locale.ROOT);
        return lower.equals("request") || lower.equals("inquiry") || lower.equals("booking")
                || lower.equals("reservation") || lower.equals("details") || lower.equals("pending")
                || lower.equals("email") || lower.equals("form") || lower.equals("number")
                || lower.equals("code") || lower.equals("please") || lower.equals("thank")
                || lower.equals("dear");
    }
}