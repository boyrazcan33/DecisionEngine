package com.inbank.decisionengine.exception;

public class InvalidLoanRequestException extends RuntimeException {
    public InvalidLoanRequestException(String message) {
        super(message);
    }
}