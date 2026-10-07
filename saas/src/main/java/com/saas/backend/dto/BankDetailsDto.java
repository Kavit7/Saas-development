package com.saas.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsDto {
    private String bankName;
    private String accountName;
    private String accountNumber;
    private String currency;
    private String swiftCode;
    private String branch;
    private String iban;
    private String country;
    private String intermediaryBankName;
    private String intermediarySwiftCode;
    private String lipaNamba;
    private String notes;
}
