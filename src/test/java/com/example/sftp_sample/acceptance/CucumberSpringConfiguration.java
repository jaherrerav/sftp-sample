package com.example.sftp_sample.acceptance;

import io.cucumber.spring.CucumberContextConfiguration;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    static SshServer sshd;
    static Path sftpRoot;

    @DynamicPropertySource
    static void configureSftp(DynamicPropertyRegistry registry) {
        try {
            sftpRoot = Files.createTempDirectory("cucumber-sftp-root");
            Files.createDirectories(sftpRoot.resolve("upload"));

            sshd = SshServer.setUpDefaultServer();
            sshd.setPort(0);
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
            sshd.setPasswordAuthenticator((u, p, s) -> "demo".equals(u) && "demo".equals(p));
            sshd.setSubsystemFactories(List.of(new SftpSubsystemFactory()));
            sshd.setFileSystemFactory(new VirtualFileSystemFactory(sftpRoot));
            sshd.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start embedded SFTP server for Cucumber", e);
        }

        registry.add("sftp.host", () -> "localhost");
        registry.add("sftp.port", sshd::getPort);
        registry.add("sftp.user", () -> "demo");
        registry.add("sftp.password", () -> "demo");
        registry.add("sftp.remote-dir", () -> "/upload");
        registry.add("sftp.local-dir", () -> System.getProperty("java.io.tmpdir") + "/cucumber-sftp-local");
        registry.add("sftp.poll-interval", () -> "500");
        registry.add("sftp.allow-unknown-keys", () -> "true");
    }
}
