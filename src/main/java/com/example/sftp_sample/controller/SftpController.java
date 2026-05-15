package com.example.sftp_sample.controller;

import com.example.sftp_sample.dto.UploadResponse;
import com.example.sftp_sample.service.SftpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/sftp")
public class SftpController {

    private final SftpService sftpService;

    public SftpController(SftpService sftpService) {
        this.sftpService = sftpService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(sftpService.upload(file));
    }
}
