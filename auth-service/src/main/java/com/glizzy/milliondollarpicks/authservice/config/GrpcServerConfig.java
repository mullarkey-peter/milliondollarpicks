package com.glizzy.milliondollarpicks.authservice.config;

import com.glizzy.milliondollarpicks.authservice.grpc.AuthGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GrpcServerConfig {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerConfig.class);

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    private final AuthGrpcService authGrpcService;

    public GrpcServerConfig(AuthGrpcService authGrpcService) {
        this.authGrpcService = authGrpcService;
    }

    @Bean
    public Server grpcServer() throws IOException {
        log.info("Starting gRPC server on port {}", grpcPort);

        Server server = ServerBuilder.forPort(grpcPort)
                .addService(authGrpcService)
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server");
            if (server != null) {
                server.shutdown();
            }
        }));

        return server;
    }
}