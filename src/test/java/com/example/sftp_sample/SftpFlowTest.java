package com.example.sftp_sample;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SftpFlowTest {

    private static SshServer sshd;
    private static Path sftpRoot;
    private static Path localDir;

    @Autowired
    private MockMvc mockMvc;

    // Started here (not @BeforeAll) so the port is available when Spring binds sftp.port.
    @DynamicPropertySource
    static void configureSftp(DynamicPropertyRegistry registry) {
        try {
            sftpRoot = Files.createTempDirectory("sftp-root");
            localDir = Files.createTempDirectory("sftp-local");
            Files.createDirectories(sftpRoot.resolve("upload"));

            sshd = SshServer.setUpDefaultServer();
            sshd.setPort(0);
            sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
            sshd.setPasswordAuthenticator((username, password, session) ->
                    "demo".equals(username) && "demo".equals(password));
            sshd.setSubsystemFactories(List.of(new SftpSubsystemFactory()));
            sshd.setFileSystemFactory(new VirtualFileSystemFactory(sftpRoot));
            sshd.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start embedded SFTP server", e);
        }

        registry.add("sftp.host", () -> "localhost");
        registry.add("sftp.port", sshd::getPort);
        registry.add("sftp.user", () -> "demo");
        registry.add("sftp.password", () -> "demo");
        registry.add("sftp.remote-dir", () -> "/upload");
        registry.add("sftp.local-dir", localDir::toString);
        registry.add("sftp.poll-interval", () -> "500");
    }

    @AfterAll
    static void stopSftpServer() throws IOException {
        if (sshd != null) {
            sshd.stop(true);
        }
    }

    @Test
    void upload_sendsFileToSftpServer() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "hello.txt", "text/plain", "hello sftp".getBytes());

        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Uploaded: hello.txt"));

        assertThat(sftpRoot.resolve("upload/hello.txt"))
                .exists()
                .hasContent("hello sftp");
    }

    @Test
    void fileOnSftpServer_isDownloadedToLocalDirectory() throws Exception {
        Files.writeString(sftpRoot.resolve("upload/incoming.txt"), "remote content");

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> localDir.resolve("incoming.txt").toFile().exists());

        assertThat(localDir.resolve("incoming.txt")).hasContent("remote content");
    }
}
