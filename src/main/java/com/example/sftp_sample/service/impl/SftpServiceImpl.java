package com.example.sftp_sample.service.impl;

import com.example.sftp_sample.dto.UploadResponse;
import com.example.sftp_sample.exception.SftpTransferException;
import com.example.sftp_sample.service.SftpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SftpServiceImpl implements SftpService {

    private static final Logger log = LoggerFactory.getLogger(SftpServiceImpl.class);

    private final MessageChannel sftpOutboundChannel;

    public SftpServiceImpl(@Qualifier("sftpOutboundChannel") MessageChannel sftpOutboundChannel) {
        this.sftpOutboundChannel = sftpOutboundChannel;
    }

    @Override
    public UploadResponse upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }
        // Strip path components to prevent path traversal on the remote SFTP server
        String safeName = Path.of(originalFilename).getFileName().toString();

        log.info("Uploading file={}", safeName);

        try {
            File tempFile = File.createTempFile("sftp-", "-" + safeName);
            try {
                file.transferTo(tempFile);
                sftpOutboundChannel.send(
                    MessageBuilder.withPayload(tempFile)
                        .setHeader(FileHeaders.FILENAME, safeName)
                        .build()
                );
                log.info("File uploaded successfully filename={}", safeName);
            } finally {
                Files.deleteIfExists(tempFile.toPath());
            }
        } catch (IOException e) {
            log.error("Failed to upload file={}", safeName, e);
            throw new SftpTransferException("Failed to upload file: " + safeName, e);
        }

        return new UploadResponse(safeName, "File uploaded successfully");
    }
}
