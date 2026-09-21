package com.marcos.conversordemoedas.exception;

public class ExternalRateApiException extends ConversionException {

    public ExternalRateApiException(String message) {
        super(message);
    }

    public ExternalRateApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
