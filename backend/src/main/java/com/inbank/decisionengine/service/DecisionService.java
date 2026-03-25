package com.inbank.decisionengine.service;

import com.inbank.decisionengine.exception.InvalidLoanRequestException;
import com.inbank.decisionengine.model.LoanRequest;
import com.inbank.decisionengine.model.LoanResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DecisionService {

    private static final int MIN_AMOUNT = 2000;
    private static final int MAX_AMOUNT = 10000;
    private static final int MIN_PERIOD = 12;
    private static final int MAX_PERIOD = 60;

    private static final Map<String, Integer> CREDIT_MODIFIERS = Map.of(
            "49002010965", 0,
            "49002010976", 100,
            "49002010987", 300,
            "49002010998", 1000
    );

    public LoanResponse decide(LoanRequest request) {
        validateRequest(request);

        int creditModifier = getCreditModifier(request.getPersonalCode());

        if (creditModifier == 0) {
            return LoanResponse.builder()
                    .approved(false)
                    .approvedAmount(0)
                    .approvedPeriod(request.getLoanPeriod())
                    .message("Loan rejected due to existing debt.")
                    .build();
        }

        return findBestLoan(creditModifier, request.getLoanPeriod());
    }

    private LoanResponse findBestLoan(int creditModifier, int requestedPeriod) {
        // Try with requested period first, find max approvable amount
        int maxAmount = calculateMaxAmount(creditModifier, requestedPeriod);

        if (maxAmount >= MIN_AMOUNT) {
            return LoanResponse.builder()
                    .approved(true)
                    .approvedAmount(maxAmount)
                    .approvedPeriod(requestedPeriod)
                    .message("Loan approved.")
                    .build();
        }

        // If not found, try extending the period
        for (int period = requestedPeriod + 1; period <= MAX_PERIOD; period++) {
            maxAmount = calculateMaxAmount(creditModifier, period);
            if (maxAmount >= MIN_AMOUNT) {
                return LoanResponse.builder()
                        .approved(true)
                        .approvedAmount(maxAmount)
                        .approvedPeriod(period)
                        .message("Loan approved with adjusted period.")
                        .build();
            }
        }

        return LoanResponse.builder()
                .approved(false)
                .approvedAmount(0)
                .approvedPeriod(requestedPeriod)
                .message("No suitable loan found.")
                .build();
    }

    private int calculateMaxAmount(int creditModifier, int period) {
        // credit score = (creditModifier / amount) * period >= 1
        // solving for amount: amount <= creditModifier * period
        int maxAmount = creditModifier * period;
        return Math.min(maxAmount, MAX_AMOUNT);
    }

    private int getCreditModifier(String personalCode) {
        if (!CREDIT_MODIFIERS.containsKey(personalCode)) {
            throw new InvalidLoanRequestException("Unknown personal code.");
        }
        return CREDIT_MODIFIERS.get(personalCode);
    }

    private void validateRequest(LoanRequest request) {
        if (request.getLoanAmount() < MIN_AMOUNT || request.getLoanAmount() > MAX_AMOUNT) {
            throw new InvalidLoanRequestException(
                    "Loan amount must be between " + MIN_AMOUNT + " and " + MAX_AMOUNT + ".");
        }
        if (request.getLoanPeriod() < MIN_PERIOD || request.getLoanPeriod() > MAX_PERIOD) {
            throw new InvalidLoanRequestException(
                    "Loan period must be between " + MIN_PERIOD + " and " + MAX_PERIOD + " months.");
        }
    }
}