package com.example.sftp_sample;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "sftp")
@Validated
public class SftpProperties {

    @NotBlank private String host;
    private int port = 22;
    @NotBlank private String user;
    @NotBlank private String password;
    private String remoteDir = "/";
    @NotBlank private String localDir;
    private boolean allowUnknownKeys = false;
    private String knownHostsFile;

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRemoteDir() { return remoteDir; }
    public void setRemoteDir(String remoteDir) { this.remoteDir = remoteDir; }

    public String getLocalDir() { return localDir; }
    public void setLocalDir(String localDir) { this.localDir = localDir; }

    public boolean isAllowUnknownKeys() { return allowUnknownKeys; }
    public void setAllowUnknownKeys(boolean allowUnknownKeys) { this.allowUnknownKeys = allowUnknownKeys; }

    public String getKnownHostsFile() { return knownHostsFile; }
    public void setKnownHostsFile(String knownHostsFile) { this.knownHostsFile = knownHostsFile; }
}
