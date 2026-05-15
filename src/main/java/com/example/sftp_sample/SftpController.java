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

@RestController
public class SftpController {

    private final MessageChannel sftpOutboundChannel;

    public SftpController(@Qualifier("sftpOutboundChannel") MessageChannel sftpOutboundChannel) {
        this.sftpOutboundChannel = sftpOutboundChannel;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("sftp-", "-" + file.getOriginalFilename());
        try {
            file.transferTo(tempFile);
            sftpOutboundChannel.send(
                MessageBuilder.withPayload(tempFile)
                    .setHeader(FileHeaders.FILENAME, file.getOriginalFilename())
                    .build()
            );
        } finally {
            tempFile.delete();
        }
        return ResponseEntity.ok("Uploaded: " + file.getOriginalFilename());
    }
}
