package com.example.sftp_sample;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.io.File;
import java.util.logging.Logger;

@Configuration
@EnableIntegration
@EnableConfigurationProperties(SftpProperties.class)
public class SftpConfig {

    private static final Logger log = Logger.getLogger(SftpConfig.class.getName());

    @Bean
    public DefaultSftpSessionFactory sftpSessionFactory(SftpProperties props) {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(props.getHost());
        factory.setPort(props.getPort());
        factory.setUser(props.getUser());
        factory.setPassword(props.getPassword());
        factory.setAllowUnknownKeys(true);
        return factory;
    }

    // --- Inbound: poll remote SFTP directory and download new files ---

    @Bean
    public SftpInboundFileSynchronizer sftpInboundFileSynchronizer(DefaultSftpSessionFactory sessionFactory,
                                                                    SftpProperties props) {
        SftpInboundFileSynchronizer synchronizer = new SftpInboundFileSynchronizer(sessionFactory);
        synchronizer.setRemoteDirectory(props.getRemoteDir());
        synchronizer.setDeleteRemoteFiles(false);
        synchronizer.setFilter(new SftpSimplePatternFileListFilter("*"));
        return synchronizer;
    }

    @Bean
    @InboundChannelAdapter(channel = "sftpInboundChannel", poller = @Poller(fixedDelay = "${sftp.poll-interval:5000}"))
    public MessageSource<File> sftpInboundMessageSource(SftpInboundFileSynchronizer synchronizer,
                                                        SftpProperties props) {
        SftpInboundFileSynchronizingMessageSource source =
                new SftpInboundFileSynchronizingMessageSource(synchronizer);
        source.setLocalDirectory(new File(props.getLocalDir()));
        source.setAutoCreateLocalDirectory(true);
        return source;
    }

    @Bean
    public MessageChannel sftpInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "sftpInboundChannel")
    public MessageHandler inboundFileHandler() {
        return message -> log.info("Downloaded file: " + ((File) message.getPayload()).getName());
    }

    // --- Outbound: upload files to remote SFTP directory ---

    @Bean
    public MessageChannel sftpOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "sftpOutboundChannel")
    public MessageHandler sftpOutboundHandler(DefaultSftpSessionFactory sessionFactory, SftpProperties props) {
        SftpMessageHandler handler = new SftpMessageHandler(sessionFactory);
        handler.setRemoteDirectoryExpression(new LiteralExpression(props.getRemoteDir()));
        handler.setAutoCreateDirectory(true);
        return handler;
    }
}
