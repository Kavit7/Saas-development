package com.saas.backend.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.saas.backend.service.FileStorageService;
import com.saas.backend.serviceImpl.LocalStorageServiceImpl;
import com.saas.backend.serviceImpl.R2StorageServiceImpl;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * FileStorageConfig
 * Dynamically switches between Local Disk Storage (local dev) and
 * Cloudflare R2 Object Storage (production hosting) based on application configuration.
 */
@Configuration
@Slf4j
public class FileStorageConfig {

    @Value("${app.storage.type:local}")
    private String storageType;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.storage.r2.account-id:}")
    private String r2AccountId;

    @Value("${app.storage.r2.bucket-name:}")
    private String r2BucketName;

    @Value("${app.storage.r2.access-key-id:}")
    private String r2AccessKeyId;

    @Value("${app.storage.r2.secret-access-key:}")
    private String r2SecretAccessKey;

    @Value("${app.storage.r2.endpoint:}")
    private String r2CustomEndpoint;

    @Value("${app.storage.r2.public-url:}")
    private String r2PublicUrl;

    @Bean
    public FileStorageService fileStorageService() {
        if ("r2".equalsIgnoreCase(storageType) || "s3".equalsIgnoreCase(storageType)) {
            boolean hasEndpointOrAccount = (!r2AccountId.isBlank() || !r2CustomEndpoint.isBlank());
            boolean hasCredentials = (!r2AccessKeyId.isBlank() && !r2SecretAccessKey.isBlank() && !r2BucketName.isBlank());

            if (!hasEndpointOrAccount || !hasCredentials) {
                log.warn("[STORAGE WARNING] 'app.storage.type={}' is configured, but Cloudflare R2 parameters (R2_ACCOUNT_ID, R2_BUCKET_NAME, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY) are incomplete. Falling back safely to Local Disk Storage.", storageType);
                return new LocalStorageServiceImpl(uploadDir);
            }

            try {
                String endpoint = (!r2CustomEndpoint.isBlank())
                        ? r2CustomEndpoint.trim()
                        : String.format("https://%s.r2.cloudflarestorage.com", r2AccountId.trim());

                S3Client s3Client = S3Client.builder()
                        .endpointOverride(URI.create(endpoint))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(r2AccessKeyId.trim(), r2SecretAccessKey.trim())
                        ))
                        .region(Region.of("auto"))
                        .serviceConfiguration(S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build())
                        .build();

                log.info("[STORAGE] Cloudflare R2 Object Storage enabled successfully. Bucket: '{}', Endpoint: '{}'", r2BucketName, endpoint);
                return new R2StorageServiceImpl(s3Client, r2BucketName.trim(), r2PublicUrl, uploadDir);

            } catch (Exception e) {
                log.error("[STORAGE ERROR] Failed to initialize Cloudflare R2 client: {}. Falling back safely to Local Storage.", e.getMessage(), e);
                return new LocalStorageServiceImpl(uploadDir);
            }
        }

        log.info("[STORAGE] Local Disk Storage enabled. Upload directory: '{}'", uploadDir);
        return new LocalStorageServiceImpl(uploadDir);
    }
}
