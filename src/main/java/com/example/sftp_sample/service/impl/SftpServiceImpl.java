package com.example.sftp_sample.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.sftp_sample.dto.UploadResponse;
import com.example.sftp_sample.exception.SftpTransferException;
import com.example.sftp_sample.service.SftpService;

/**
 * Spring Integration-backed implementation of {@link SftpService}.
 */
@Slf4j
@Service
public class SftpServiceImpl implements SftpService {

    private final MessageChannel sftpOutboundChannel;

    /** Wires the outbound SFTP channel via constructor injection. */
    public SftpServiceImpl(
            @Qualifier("sftpOutboundChannel") MessageChannel sftpOutboundChannel) {
        this.sftpOutboundChannel = sftpOutboundChannel;
    }

    /**
     * Sanitizes the filename to prevent path traversal, then streams the file
     * through the Spring Integration outbound channel to the remote SFTP server.
     */
    @Override
    @PreAuthorize("hasRole('USER')")
    public UploadResponse upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }
        // JSch / Spring Integration passes the full client path; extract only the
        // last component to prevent path traversal on the remote SFTP server
        String safeName = Path.of(originalFilename).getFileName().toString();

        log.debug("Uploading file={}", safeName);

        try {
            File tempFile = File.createTempFile("sftp-", "-" + safeName);
            try {
                file.transferTo(tempFile);
                sftpOutboundChannel.send(
                    MessageBuilder.withPayload(tempFile)
                        .setHeader(FileHeaders.FILENAME, safeName)
                        .build()
                );
                log.info("Archivo subido correctamente filename={}", safeName);
            } finally {
                Files.deleteIfExists(tempFile.toPath());
            }
        } catch (IOException e) {
            log.error("Error al subir archivo filename={}", safeName, e);
            throw new SftpTransferException("Failed to upload file: " + safeName, e);
        }

        return new UploadResponse(safeName, "File uploaded successfully");
    }
}
