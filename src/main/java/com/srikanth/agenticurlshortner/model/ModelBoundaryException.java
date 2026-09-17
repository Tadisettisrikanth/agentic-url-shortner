package com.srikanth.agenticurlshortner.model;

public final class ModelBoundaryException extends RuntimeException {
    public ModelBoundaryException(String message) {
        super(message);
    }

    public ModelBoundaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
