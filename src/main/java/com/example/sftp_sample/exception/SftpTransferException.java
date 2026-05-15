package com.example.sftp_sample.exception;

public class SftpTransferException extends RuntimeException {

    public SftpTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
