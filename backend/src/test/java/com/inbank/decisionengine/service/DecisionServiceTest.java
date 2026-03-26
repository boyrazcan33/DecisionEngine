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
        request.setLoanAmount(2000);
        request.setLoanPeriod(12);

        LoanResponse response = decisionService.decide(request);

        assertFalse(response.isApproved());
    }

    @Test
    void shouldExtendToMaxPeriodForSegment1() {
        // creditModifier=100, period=12 → 100*12=1200 < 2000
        // extend to MAX_PERIOD: 100*60=6000 >= 2000
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010976");
        request.setLoanAmount(2000);
        request.setLoanPeriod(12);

        LoanResponse response = decisionService.decide(request);

        assertTrue(response.isApproved());
        assertEquals(6000, response.getApprovedAmount());
        assertEquals(60, response.getApprovedPeriod());
    }

    @Test
    void shouldReturnMaxAmountForRequestedPeriodForSegment2() {
        // creditModifier=300, period=24 → 300*24=7200 < 10000
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010987");
        request.setLoanAmount(2000);
        request.setLoanPeriod(24);

        LoanResponse response = decisionService.decide(request);

        assertTrue(response.isApproved());
        assertEquals(7200, response.getApprovedAmount());
        assertEquals(24, response.getApprovedPeriod());
    }

    @Test
    void shouldReturnMaxAmountWithShortestPeriodForSegment3() {
        // creditModifier=1000, period=60 → 1000*60=60000 > 10000
        // shortest period: 1000*12=12000 > 10000 → period=12
        LoanRequest request = new LoanRequest();
        request.setPersonalCode("49002010998");
        request.setLoanAmount(2000);
        request.setLoanPeriod(60);

        LoanResponse response = decisionService.decide(request);

        assertTrue(response.isApproved());
        assertEquals(10000, response.getApprovedAmount());
        assertEquals(12, response.getApprovedPeriod());
    }

    @Test
    void shouldReturnMaxAmountWithShortestPeriodWhenRequestedPeriodExceedsCap() {
        // creditModifier=1000, period=12 → 1000*12=12000 > 10000
        // shortest period: 12
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