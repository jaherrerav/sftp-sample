package com.example.sftp_sample.service

import com.example.sftp_sample.dto.UploadResponse
import com.example.sftp_sample.exception.SftpTransferException
import com.example.sftp_sample.service.impl.SftpServiceImpl
import org.springframework.messaging.MessageChannel
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

class SftpServiceSpec extends Specification {

    MessageChannel sftpOutboundChannel = Mock()
    SftpService sftpService = new SftpServiceImpl(sftpOutboundChannel)

    def "debería subir archivo y retornar respuesta con nombre del archivo"() {
        given:
        def mockFile = Mock(MultipartFile)
        mockFile.getOriginalFilename() >> "report.pdf"

        when:
        def result = sftpService.upload(mockFile)

        then:
        1 * sftpOutboundChannel.send(_)
        result instanceof UploadResponse
        result.filename() == "report.pdf"
        result.message() == "File uploaded successfully"
    }

    def "debería lanzar IllegalArgumentException cuando el nombre del archivo es nulo o vacío"() {
        given:
        def mockFile = Mock(MultipartFile)
        mockFile.getOriginalFilename() >> filename

        when:
        sftpService.upload(mockFile)

        then:
        thrown(IllegalArgumentException)

        where:
        filename << [null, "", "   "]
    }

    def "debería sanitizar el nombre eliminando componentes de ruta para prevenir path traversal"() {
        given:
        def mockFile = Mock(MultipartFile)
        mockFile.getOriginalFilename() >> "../../../etc/passwd"

        when:
        def result = sftpService.upload(mockFile)

        then:
        1 * sftpOutboundChannel.send(_)
        result.filename() == "passwd"
    }

    def "debería lanzar SftpTransferException cuando falla la transferencia de archivo"() {
        given:
        def mockFile = Mock(MultipartFile)
        mockFile.getOriginalFilename() >> "file.txt"
        mockFile.transferTo(_ as File) >> { throw new IOException("Disk full") }

        when:
        sftpService.upload(mockFile)

        then:
        thrown(SftpTransferException)
    }
}
