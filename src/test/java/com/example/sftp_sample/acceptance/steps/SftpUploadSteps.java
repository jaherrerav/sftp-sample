package com.example.sftp_sample.acceptance.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SftpUploadSteps {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMultipartFile archivo;
    private ResultActions resultado;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
    }

    @Given("que tengo un archivo {string} con contenido {string}")
    public void tengoUnArchivo(String nombre, String contenido) {
        this.archivo = new MockMultipartFile("file", nombre, "text/plain", contenido.getBytes());
    }

    @Given("que tengo un archivo sin nombre")
    public void tengoUnArchivoSinNombre() {
        this.archivo = new MockMultipartFile("file", "", "text/plain", new byte[0]);
    }

    @When("envío el archivo al endpoint de subida")
    public void envioElArchivo() throws Exception {
        this.resultado = mockMvc().perform(
            multipart("/api/v1/sftp/upload")
                .file(archivo)
                .with(user("admin").roles("USER"))
        );
    }

    @Then("el sistema responde con código {int}")
    public void elSistemaRespondeConCodigo(int codigoEsperado) throws Exception {
        resultado.andExpect(status().is(codigoEsperado));
    }

    @And("la respuesta contiene el filename {string}")
    public void laRespuestaContieneFilename(String filename) throws Exception {
        resultado.andExpect(jsonPath("$.filename").value(filename));
    }

    @And("el mensaje de respuesta es {string}")
    public void elMensajeDeRespuestaEs(String mensaje) throws Exception {
        resultado.andExpect(jsonPath("$.message").value(mensaje));
    }

    @And("el título del error es {string}")
    public void elTituloDelErrorEs(String titulo) throws Exception {
        resultado.andExpect(jsonPath("$.title").value(titulo));
    }
}
