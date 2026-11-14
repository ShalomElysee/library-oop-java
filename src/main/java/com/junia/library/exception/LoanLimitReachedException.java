package com.junia.library.exception;

public class LoanLimitReachedException extends LibraryException {
    public LoanLimitReachedException(String message) {
        super(message);
    }
}
