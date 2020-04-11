package com.vibal.utilities.persistence.retrofit;

public class UtilAppException extends Exception {
    private static final String ERROR_MSG = "An unexpected error occurred";

    public UtilAppException(String message, Throwable cause) {
        super(message, cause);
    }

    public UtilAppException(String message) {
        super(message);
    }

    public UtilAppException() {
        this(ERROR_MSG);
    }
}
