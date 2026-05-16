package com.example.sftp_sample.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.sftp_sample.dto.UploadResponse;

/**
 * Contract for SFTP file transfer operations.
 */
public interface SftpService {

    /**
     * Transfers the given multipart file to the remote SFTP server.
     */
    UploadResponse upload(MultipartFile file);
}
