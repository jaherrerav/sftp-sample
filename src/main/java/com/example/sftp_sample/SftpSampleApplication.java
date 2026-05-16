/**
 * Spring Boot entry point for the SFTP integration service.
 */
package com.example.sftp_sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SftpSampleApplication {

    /** Starts the Spring application context. */
    public static void main(String[] args) {
        SpringApplication.run(SftpSampleApplication.class, args);
    }
}
