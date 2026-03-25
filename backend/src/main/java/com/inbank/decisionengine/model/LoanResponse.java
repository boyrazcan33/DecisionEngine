package com.inbank.decisionengine.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class LoanResponse {
    private boolean approved;
    private int approvedAmount;
    private int approvedPeriod;
    private String message;
}