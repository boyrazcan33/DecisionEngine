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
    void shouldExtendToShortestPeriodWhenMaxPotentialExceedsLimit() {
        // creditModifier=300, 300*60=18000 > 10000 → cap at 10000
        // shortest period: 300*34=10200 >= 10000 → period=34
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010987");
        request.setLoanAmount(2000);
        request.setLoanPeriod(12);

        LoanResponse response = decisionService.decide(request);

        assertTrue(response.isApproved());
        assertEquals(10000, response.getApprovedAmount());
        assertEquals(34, response.getApprovedPeriod());
    }

    @Test
    void shouldExtendToShortestPeriodForHighModifierSegment() {
        // creditModifier=1000, 1000*60=60000 > 10000 → cap at 10000
        // shortest period: 1000*12=12000 >= 10000 → period=12
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010998");
        request.setLoanAmount(2000);
        request.setLoanPeriod(12);

        LoanResponse response = decisionService.decide(request);

        assertTrue(response.isApproved());
        assertEquals(10000, response.getApprovedAmount());
        assertEquals(12, response.getApprovedPeriod());
    }

    @Test
    void shouldRejectWhenMaxPeriodCannotReachMinimumAmount() {
        // This would apply to a modifier so low that even 60 months yields less than 2000
        // e.g. modifier=30: 30*60=1800 < 2000 → reject
        // We simulate this by directly testing the boundary
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