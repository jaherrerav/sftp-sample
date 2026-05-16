package com.example.sftp_sample.exception;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Translates domain and infrastructure exceptions to RFC 7807 ProblemDetail responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Maps SFTP I/O failures to 500 responses. */
    @ExceptionHandler(SftpTransferException.class)
    public ProblemDetail handleSftpTransfer(SftpTransferException ex) {
        log.error("Error en transferencia SFTP: {}", ex.getMessage(), ex);
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problem.setTitle("SFTP Transfer Error");
        return problem;
    }

    /** Maps invalid request arguments to 400 responses. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Request");
        return problem;
    }

    /** Maps oversized uploads to 413 responses. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("Archivo supera el tamaño máximo permitido: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatusCode.valueOf(413),
            "File size exceeds the maximum allowed limit");
        problem.setTitle("File Too Large");
        return problem;
    }
}
