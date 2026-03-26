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
    void shouldRejectImmediatelyWhenApplicantHasDebt() {
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010965");
        request.setLoanAmount(2000);
        request.setLoanPeriod(12);

        LoanResponse response = decisionService.decide(request);

        assertFalse(response.isApproved());
    }

    @Test
    void shouldReturnMaximumAmountRegardlessOfRequestedPeriod() {
        // creditModifier=100, requested period=25 → naive impl would return €2000 at 25 months
        // correct impl: 100*60=6000 → return €6000 at 60 months
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010976");
        request.setLoanAmount(2000);
        request.setLoanPeriod(25);

        LoanResponse response = decisionService.decide(request);

        assertTrue(response.isApproved());
        assertEquals(6000, response.getApprovedAmount());
        assertEquals(60, response.getApprovedPeriod());
    }

    @Test
    void shouldCapAtMaxAmountAndKeepRequestedPeriod() {
        // creditModifier=300, 300*60=18000 > 10000 → cap at 10000
        // keep requested period (12) since max sum is already found
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010987");
        request.setLoanAmount(2000);
        request.setLoanPeriod(12);

        LoanResponse response = decisionService.decide(request);

        assertTrue(response.isApproved());
        assertEquals(10000, response.getApprovedAmount());
        assertEquals(12, response.getApprovedPeriod());
    }

    @Test
    void shouldCapAtMaxAmountAndPreserveCustomerPreferredPeriod() {
        // creditModifier=1000, 1000*60=60000 > 10000 → cap at 10000
        // customer requested 36 months → keep 36 months
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010998");
        request.setLoanAmount(2000);
        request.setLoanPeriod(36);

        LoanResponse response = decisionService.decide(request);

        assertTrue(response.isApproved());
        assertEquals(10000, response.getApprovedAmount());
        assertEquals(36, response.getApprovedPeriod());
    }

    @Test
    void shouldRejectWhenMaxPeriodCannotReachMinimumAmount() {
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010965");
        request.setLoanAmount(2000);
        request.setLoanPeriod(60);

        LoanResponse response = decisionService.decide(request);

        assertFalse(response.isApproved());
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
    void shouldThrowExceptionForInvalidPeriod() {
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010976");
        request.setLoanAmount(4000);
        request.setLoanPeriod(6);

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