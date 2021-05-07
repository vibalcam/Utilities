package com.vibal.utilities.exceptions;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UtilAppException extends IOException {
    public static final int NON_EXISTENT_WARNING = -2;
    public static final int NOT_ALLOWED = -3;

    private static final String ERROR_MSG = "An unexpected error occurred";
    private static final Map<Integer, String> ERROR_CODES = getErrorCodes();

    private int errorCode = 0;

    public static String getErrorMsg(Throwable throwable) {
        return throwable instanceof UtilAppException ? throwable.getLocalizedMessage() :
                ERROR_MSG;
    }

    @NonNull
    private static Map<Integer, String> getErrorCodes() {
        Map<Integer, String> codes = new HashMap<>();
        codes.put(NON_EXISTENT_WARNING, "Non-existent error occurred");  // NON_EXISTENT_WARNING
        codes.put(NOT_ALLOWED, "Not allowed error occurred");  // NOT_ALLOWED
        return codes;
    }

    @NonNull
    public static UtilAppException getException(long errorValue) {
        if ((int) errorValue == NON_EXISTENT_WARNING) {
            return new NonExistentException();
        }
        return new UtilAppException(errorValue);
    }

    private UtilAppException(long errorValue) {
        this(ERROR_CODES.getOrDefault(errorValue, ERROR_MSG));
        this.errorCode = (int) errorValue;
    }

    public UtilAppException(@NonNull String message, Throwable cause) {
        super(message.isEmpty() ? ERROR_MSG : message, cause);
    }

    public UtilAppException(@NonNull String message) {
        super(message.isEmpty() ? ERROR_MSG : message);
    }

    public UtilAppException() {
        this(ERROR_MSG);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static class NonExistentException extends UtilAppException {
        private NonExistentException() {
            super(NON_EXISTENT_WARNING);
        }
    }
//
//    public static class NotAllowedException extends UtilAppException {
//        private NonExistentException() {
//            super("Non-existent error occurred");
//        }
//    }
}
