package com.example.sftp_sample.service;

import com.example.sftp_sample.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SftpService {

    UploadResponse upload(MultipartFile file);
}
