package com.vibal.utilities.exceptions;

public class NoConnectivityException extends UtilAppException {
    private static final String NO_INTERNET_ERROR = "No internet connection";

    public NoConnectivityException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoConnectivityException(String message) {
        super(message);
    }

    public NoConnectivityException() {
        this(NO_INTERNET_ERROR);
    }
}
