package com.example.sftp_sample.exception;

/**
 * Signals a failure during SFTP file transfer, wrapping the underlying I/O cause.
 */
public class SftpTransferException extends BusinessException {

    /** Creates a transfer exception with a message and the root I/O cause. */
    public SftpTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
