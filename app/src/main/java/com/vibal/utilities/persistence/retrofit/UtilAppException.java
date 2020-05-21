package com.vibal.utilities.persistence.retrofit;

import java.io.IOException;

public class UtilAppException extends IOException {
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

    public static class NoConnectivityException extends UtilAppException {
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
}
