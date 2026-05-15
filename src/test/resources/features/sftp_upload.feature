Feature: Subida de archivos SFTP
  Como cliente del sistema
  Quiero subir archivos a través de la API
  Para que sean transferidos al servidor SFTP

  Scenario: Subir un archivo válido exitosamente
    Given que tengo un archivo "reporte.txt" con contenido "datos importantes"
    When envío el archivo al endpoint de subida
    Then el sistema responde con código 200
    And la respuesta contiene el filename "reporte.txt"
    And el mensaje de respuesta es "File uploaded successfully"

  Scenario: Rechazar solicitud sin nombre de archivo
    Given que tengo un archivo sin nombre
    When envío el archivo al endpoint de subida
    Then el sistema responde con código 400
    And el título del error es "Invalid Request"
