package com.example.sftp_sample.dto;

/**
 * Immutable response carrying the result of a single SFTP file upload.
 */
public record UploadResponse(String filename, String message) {}
