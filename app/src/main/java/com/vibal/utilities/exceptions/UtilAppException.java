package com.vibal.utilities.exceptions;

import java.io.IOException;

public class UtilAppException extends IOException {
    public static final String ERROR_MSG = "An unexpected error occurred";

    public UtilAppException(String message, Throwable cause) {
        super(message.isEmpty() ? ERROR_MSG : message, cause);
    }

    public UtilAppException(String message) {
        super(message.isEmpty() ? ERROR_MSG : message);
    }

    public UtilAppException() {
        this(ERROR_MSG);
    }
}
