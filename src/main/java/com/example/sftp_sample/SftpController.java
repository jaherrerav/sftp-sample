package com.example.sftp_sample;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class SftpController {

    private final MessageChannel sftpOutboundChannel;

    public SftpController(@Qualifier("sftpOutboundChannel") MessageChannel sftpOutboundChannel) {
        this.sftpOutboundChannel = sftpOutboundChannel;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return ResponseEntity.badRequest().body("Filename is required");
        }
        // Strip any path components to prevent path traversal on the remote SFTP server
        String safeName = Path.of(originalFilename).getFileName().toString();

        File tempFile = File.createTempFile("sftp-", "-" + safeName);
        try {
            file.transferTo(tempFile);
            sftpOutboundChannel.send(
                MessageBuilder.withPayload(tempFile)
                    .setHeader(FileHeaders.FILENAME, safeName)
                    .build()
            );
        } finally {
            try {
                Files.deleteIfExists(tempFile.toPath());
            } catch (IOException ignored) {
            }
        }
        return ResponseEntity.ok("Uploaded: " + safeName);
    }
}
