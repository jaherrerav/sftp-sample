package com.example.sftp_sample.controller

import com.example.sftp_sample.dto.UploadResponse
import com.example.sftp_sample.service.SftpService
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

class SftpControllerSpec extends Specification {

    SftpService sftpService = Mock()
    SftpController controller = new SftpController(sftpService)

    def "debería retornar 200 con UploadResponse cuando el archivo es válido"() {
        given:
        def file = new MockMultipartFile("file", "test.txt", "text/plain", "content".bytes)
        def expected = new UploadResponse("test.txt", "File uploaded successfully")
        sftpService.upload(file) >> expected

        when:
        def result = controller.upload(file)

        then:
        result.statusCode == HttpStatus.OK
        result.body == expected
        result.body.filename() == "test.txt"
    }

    def "debería propagar la excepción cuando el servicio lanza IllegalArgumentException"() {
        given:
        def file = new MockMultipartFile("file", "", "text/plain", new byte[0])
        sftpService.upload(file) >> { throw new IllegalArgumentException("Filename is required") }

        when:
        controller.upload(file)

        then:
        thrown(IllegalArgumentException)
    }
}
