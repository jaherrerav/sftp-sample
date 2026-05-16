package com.example.sftp_sample.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Externalized configuration for SFTP connection and directory settings,
 * bound from the {@code sftp.*} prefix in application properties.
 */
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

    /** Returns the SFTP server hostname or IP address. */
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    /** Returns the SFTP port; defaults to 22. */
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    /** Returns the SFTP login username. */
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    /** Returns the SFTP login password. */
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /** Returns the remote directory used for both uploads and inbound polling. */
    public String getRemoteDir() { return remoteDir; }
    public void setRemoteDir(String remoteDir) { this.remoteDir = remoteDir; }

    /** Returns the local directory where inbound files are synchronized. */
    public String getLocalDir() { return localDir; }
    public void setLocalDir(String localDir) { this.localDir = localDir; }

    /**
     * Returns whether host key verification is disabled.
     * Must be {@code false} in production; only acceptable for local testing.
     */
    public boolean isAllowUnknownKeys() { return allowUnknownKeys; }
    public void setAllowUnknownKeys(boolean allowUnknownKeys) {
        this.allowUnknownKeys = allowUnknownKeys;
    }

    /**
     * Returns the path to a known_hosts file used to verify the server's host key.
     * Takes precedence over {@code allowUnknownKeys}.
     */
    public String getKnownHostsFile() { return knownHostsFile; }
    public void setKnownHostsFile(String knownHostsFile) {
        this.knownHostsFile = knownHostsFile;
    }
}
