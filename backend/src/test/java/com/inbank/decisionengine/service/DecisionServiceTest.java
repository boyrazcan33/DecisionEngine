package com.inbank.decisionengine.service;

import com.inbank.decisionengine.exception.InvalidLoanRequestException;
import com.inbank.decisionengine.model.LoanRequest;
import com.inbank.decisionengine.model.LoanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DecisionServiceTest {

    private DecisionService decisionService;

    @BeforeEach
    void setUp() {
        decisionService = new DecisionService();
    }

    @Test
    void shouldRejectLoanForPersonWithDebt() {
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010965");
        request.setLoanAmount(4000);
        request.setLoanPeriod(12);

        LoanResponse response = decisionService.decide(request);

        assertFalse(response.isApproved());
    }

    @Test
    void shouldApproveLoanForSegment1() {
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010976");
        request.setLoanAmount(4000);
        request.setLoanPeriod(24);

        LoanResponse response = decisionService.decide(request);

        // creditModifier=100, period=24 → max=2400 >= 2000
        assertTrue(response.isApproved());
        assertEquals(2400, response.getApprovedAmount());
    }

    @Test
    void shouldReturnMaxAmountForSegment3() {
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010998");
        request.setLoanAmount(2000);
        request.setLoanPeriod(12);

        LoanResponse response = decisionService.decide(request);

        // creditModifier=1000, period=12 → max=10000
        assertTrue(response.isApproved());
        assertEquals(10000, response.getApprovedAmount());
    }

    @Test
    void shouldExtendPeriodWhenAmountNotFound() {
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010976");
        request.setLoanAmount(4000);
        request.setLoanPeriod(12);

        LoanResponse response = decisionService.decide(request);

        // creditModifier=100, period=12 → max=1200 < 2000, should extend period
        // period=20 → max=2000 >= 2000
        assertTrue(response.isApproved());
        assertTrue(response.getApprovedPeriod() > 12);
    }

    @Test
    void shouldThrowExceptionForInvalidAmount() {
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010976");
        request.setLoanAmount(500);
        request.setLoanPeriod(12);

        assertThrows(InvalidLoanRequestException.class, () -> decisionService.decide(request));
    }

    @Test
    void shouldThrowExceptionForUnknownPersonalCode() {
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("00000000000");
        request.setLoanAmount(4000);
        request.setLoanPeriod(12);

        assertThrows(InvalidLoanRequestException.class, () -> decisionService.decide(request));
    }
}