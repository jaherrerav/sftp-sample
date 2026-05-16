# Comandos Frecuentes (Gradle)

```bash
# Build completo (compila + tests)
./gradlew build

# Ejecutar aplicación
./gradlew bootRun

# Tests unitarios e integración (Spock + Cucumber)
./gradlew test

# Solo compilar sin tests
./gradlew classes -x test

# Test específico por clase
./gradlew test --tests "com.example.sftp_sample.service.SftpServiceSpec"

# Test específico por método
./gradlew test --tests "com.example.sftp_sample.service.SftpServiceSpec.debería subir archivo*"

# Tests de rendimiento (requiere JMeter instalado)
jmeter -n -t src/test/jmeter/sftp_upload_load_test.jmx \
  -Jhost=localhost -Jport=8080 \
  -l build/reports/jmeter/results.jtl -e -o build/reports/jmeter/html/
```