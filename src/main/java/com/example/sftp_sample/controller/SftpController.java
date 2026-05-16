package com.example.sftp_sample.controller;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.sftp_sample.dto.UploadResponse;
import com.example.sftp_sample.service.SftpService;

/**
 * REST endpoint for SFTP file operations.
 */
@Tag(name = "SFTP", description = "Operaciones de transferencia de archivos SFTP")
@RestController
@RequestMapping("/api/v1/sftp")
public class SftpController {

    private final SftpService sftpService;

    /** Wires the SFTP service via constructor injection. */
    public SftpController(SftpService sftpService) {
        this.sftpService = sftpService;
    }

    /**
     * Delegates the upload to the service layer and returns the transfer result.
     */
    @Operation(summary = "Subir archivo al servidor SFTP")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Archivo subido correctamente"),
        @ApiResponse(responseCode = "400", description = "Nombre de archivo inválido",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "413", description = "Archivo demasiado grande",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Error al transferir el archivo",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(sftpService.upload(file));
    }
}
