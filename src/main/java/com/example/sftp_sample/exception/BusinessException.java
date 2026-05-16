package com.example.sftp_sample.exception;

/**
 * Base exception for all domain-level business errors in this service.
 */
public class BusinessException extends RuntimeException {

    /** Creates a business exception with a descriptive message. */
    public BusinessException(String message) {
        super(message);
    }

    /** Creates a business exception wrapping a lower-level cause. */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
