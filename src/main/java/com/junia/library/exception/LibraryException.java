package com.junia.library.exception;

/** Base class of all business errors, so callers can catch them in one place if they want. */
public class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}
