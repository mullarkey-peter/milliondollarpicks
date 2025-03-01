package com.glizzy.milliondollarpicks.authservice.grpc;

import com.glizzy.milliondollarpicks.authservice.service.AuthService;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(AuthGrpcService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthGrpcService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void validateToken(TokenValidationRequest request, StreamObserver<TokenValidationResponse> responseObserver) {
        log.debug("gRPC validateToken called with token length: {}",
                request.getToken() != null ? request.getToken().length() : 0);

        boolean isValid = authService.validateToken(request.getToken());

        TokenValidationResponse response = TokenValidationResponse.newBuilder()
                .setValid(isValid)
                .setMessage(isValid ? "Token is valid" : "Token is invalid or expired")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        log.debug("gRPC validateToken response: {}", response.getValid());
    }

    @Override
    public void getUserInfo(TokenValidationRequest request, StreamObserver<UserInfoResponse> responseObserver) {
        log.debug("gRPC getUserInfo called with token length: {}",
                request.getToken() != null ? request.getToken().length() : 0);

        String token = request.getToken();
        UserInfoResponse.Builder responseBuilder = UserInfoResponse.newBuilder();

        try {
            if (authService.validateToken(token)) {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtSecret.getBytes())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                Object userId = claims.get("userId");
                String username = claims.get("username", String.class);

                responseBuilder.setUserId(String.valueOf(userId))
                        .setUsername(username)
                        .setSuccess(true)
                        .setMessage("User info retrieved successfully");
            } else {
                responseBuilder.setSuccess(false)
                        .setMessage("Invalid or expired token");
            }
        } catch (Exception e) {
            log.error("Error extracting user info from token: {}", e.getMessage(), e);
            responseBuilder.setSuccess(false)
                    .setMessage("Error processing token: " + e.getMessage());
        }

        UserInfoResponse response = responseBuilder.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        log.debug("gRPC getUserInfo response success: {}", response.getSuccess());
    }
}