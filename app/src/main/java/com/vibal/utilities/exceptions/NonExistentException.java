package com.vibal.utilities.exceptions;

public class NonExistentException extends UtilAppException {

    public NonExistentException() {
        super(UtilAppException.ERROR_MSG + ": refresh the app");
    }
}
