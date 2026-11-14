package com.junia.library.exception;

/**
 * Unchecked on purpose: a broken data file is not something the caller can recover from
 * in the middle of a stream pipeline (lambdas cannot throw checked exceptions).
 */
public class DataLoadException extends RuntimeException {
    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
