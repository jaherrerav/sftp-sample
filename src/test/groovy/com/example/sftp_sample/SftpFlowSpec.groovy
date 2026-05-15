package com.example.sftp_sample

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.sftp.server.SftpSubsystemFactory
import org.awaitility.Awaitility
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.util.FileSystemUtils
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SftpFlowSpec extends Specification {

    @Shared
    static SshServer sshd

    @Shared
    static Path sftpRoot

    @Shared
    static Path localDir

    @Autowired
    MockMvc mockMvc

    // Starts embedded SFTP server before Spring context so the port is available for binding
    @DynamicPropertySource
    static void configureSftp(DynamicPropertyRegistry registry) {
        sftpRoot = Files.createTempDirectory("sftp-root")
        localDir = Files.createTempDirectory("sftp-local")
        Files.createDirectories(sftpRoot.resolve("upload"))

        sshd = SshServer.setUpDefaultServer()
        sshd.port = 0
        sshd.keyPairProvider = new SimpleGeneratorHostKeyProvider()
        sshd.passwordAuthenticator = { username, password, session ->
            "demo" == username && "demo" == password
        }
        sshd.subsystemFactories = [new SftpSubsystemFactory()]
        sshd.fileSystemFactory = new VirtualFileSystemFactory(sftpRoot)
        sshd.start()

        registry.add("sftp.host", { "localhost" })
        registry.add("sftp.port", { sshd.port })
        registry.add("sftp.user", { "demo" })
        registry.add("sftp.password", { "demo" })
        registry.add("sftp.remote-dir", { "/upload" })
        registry.add("sftp.local-dir", { localDir.toString() })
        registry.add("sftp.poll-interval", { "500" })
        registry.add("sftp.allow-unknown-keys", { "true" })
    }

    def cleanupSpec() {
        sshd?.stop(true)
        FileSystemUtils.deleteRecursively(sftpRoot)
        FileSystemUtils.deleteRecursively(localDir)
    }

    def "upload envía el archivo al servidor SFTP y retorna UploadResponse"() {
        given:
        def file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello sftp".bytes)

        when:
        def result = mockMvc.perform(
            multipart("/api/v1/sftp/upload")
                .file(file)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("USER"))
        )

        then:
        result.andExpect(status().isOk())
            .andExpect(jsonPath('$.filename').value("hello.txt"))
            .andExpect(jsonPath('$.message').value("File uploaded successfully"))
        sftpRoot.resolve("upload/hello.txt").toFile().exists()
        sftpRoot.resolve("upload/hello.txt").toFile().text == "hello sftp"
    }

    def "archivo en servidor SFTP es descargado al directorio local por el poller"() {
        given:
        Files.writeString(sftpRoot.resolve("upload/incoming.txt"), "remote content")

        expect:
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .until { localDir.resolve("incoming.txt").toFile().exists() }
        localDir.resolve("incoming.txt").toFile().text == "remote content"
    }
}
