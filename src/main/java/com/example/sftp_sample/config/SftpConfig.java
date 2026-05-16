package com.example.sftp_sample.config;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * Spring Integration wiring for bidirectional SFTP communication.
 */
@Slf4j
@Configuration
@EnableIntegration
@EnableConfigurationProperties(SftpProperties.class)
public class SftpConfig {

    /**
     * Creates the SFTP session factory, enforcing host key verification
     * unless explicitly disabled for local testing.
     */
    @Bean
    public DefaultSftpSessionFactory sftpSessionFactory(SftpProperties props) {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(props.getHost());
        factory.setPort(props.getPort());
        factory.setUser(props.getUser());
        factory.setPassword(props.getPassword());

        String knownHostsFile = props.getKnownHostsFile();
        if (knownHostsFile != null && !knownHostsFile.isBlank()) {
            factory.setKnownHostsResource(new FileSystemResource(knownHostsFile));
        } else if (props.isAllowUnknownKeys()) {
            log.warn("SFTP host key verification is disabled. Do not use in production.");
            factory.setAllowUnknownKeys(true);
        } else {
            throw new IllegalStateException(
                "Configure sftp.known-hosts-file for production, "
                + "or set sftp.allow-unknown-keys=true for local testing only.");
        }

        return factory;
    }

    /**
     * Configures periodic sync from the remote directory without deleting remote files.
     */
    @Bean
    public SftpInboundFileSynchronizer sftpInboundFileSynchronizer(
            DefaultSftpSessionFactory sessionFactory,
            SftpProperties props) {
        SftpInboundFileSynchronizer synchronizer =
            new SftpInboundFileSynchronizer(sessionFactory);
        synchronizer.setRemoteDirectory(props.getRemoteDir());
        synchronizer.setDeleteRemoteFiles(false);
        synchronizer.setFilter(new SftpSimplePatternFileListFilter("*"));
        return synchronizer;
    }

    /** Polls the remote directory and emits a message per downloaded file. */
    @Bean
    @InboundChannelAdapter(
        channel = "sftpInboundChannel",
        poller = @Poller(fixedDelay = "${sftp.poll-interval:5000}"))
    public MessageSource<File> sftpInboundMessageSource(
            SftpInboundFileSynchronizer synchronizer,
            SftpProperties props) {
        SftpInboundFileSynchronizingMessageSource source =
            new SftpInboundFileSynchronizingMessageSource(synchronizer);
        source.setLocalDirectory(new File(props.getLocalDir()));
        source.setAutoCreateLocalDirectory(true);
        return source;
    }

    /** Channel that carries inbound SFTP file messages. */
    @Bean
    public MessageChannel sftpInboundChannel() {
        return new DirectChannel();
    }

    /** Logs each file received from the remote SFTP server. */
    @Bean
    @ServiceActivator(inputChannel = "sftpInboundChannel")
    public MessageHandler inboundFileHandler() {
        return message ->
            log.info("Archivo descargado filename={}",
                ((File) message.getPayload()).getName());
    }

    /** Channel that accepts files to be uploaded to the remote SFTP server. */
    @Bean
    public MessageChannel sftpOutboundChannel() {
        return new DirectChannel();
    }

    /** Routes outbound messages to the configured remote directory. */
    @Bean
    @ServiceActivator(inputChannel = "sftpOutboundChannel")
    public MessageHandler sftpOutboundHandler(
            DefaultSftpSessionFactory sessionFactory,
            SftpProperties props) {
        SftpMessageHandler handler = new SftpMessageHandler(sessionFactory);
        handler.setRemoteDirectoryExpression(
            new LiteralExpression(props.getRemoteDir()));
        handler.setAutoCreateDirectory(true);
        return handler;
    }
}
