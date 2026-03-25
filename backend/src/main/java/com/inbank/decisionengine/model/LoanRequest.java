package com.inbank.decisionengine.model;

import lombok.Data;

@Data
public class LoanRequest {
    private String personalCode;
    private int loanAmount;
    private int loanPeriod;
}