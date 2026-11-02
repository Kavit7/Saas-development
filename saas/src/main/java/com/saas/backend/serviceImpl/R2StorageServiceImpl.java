package com.saas.backend.serviceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.saas.backend.service.FileStorageService;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * R2StorageServiceImpl
 * Production-grade Object Storage implementation backed by Cloudflare R2
 * (using standard S3-compatible API). Prevents file loss on ephemeral hosts.
 */
@Slf4j
public class R2StorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String publicUrl;
    private final String localFallbackDir;

    public R2StorageServiceImpl(S3Client s3Client, String bucketName, String publicUrl, String localFallbackDir) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.publicUrl = (publicUrl != null && !publicUrl.isBlank())
                ? (publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl)
                : null;
        this.localFallbackDir = (localFallbackDir != null && !localFallbackDir.isBlank()) ? localFallbackDir : "uploads";
        log.info("Initialized Cloudflare R2 Storage provider. Bucket: '{}', Public URL: '{}'", bucketName, this.publicUrl);
    }

    @Override
    public String storeFile(String key, byte[] content, String contentType) {
        String cleanKey = sanitizeKey(key);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(cleanKey)
                    .contentType(contentType != null && !contentType.isBlank() ? contentType : "application/octet-stream")
                    .contentLength((long) content.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(content));
            log.info("Successfully uploaded object to Cloudflare R2: bucket='{}', key='{}' ({} bytes)", bucketName, cleanKey, content.length);

            if (publicUrl != null) {
                return publicUrl + "/" + cleanKey;
            }
            return cleanKey;

        } catch (Exception e) {
            log.error("Failed to upload object to Cloudflare R2 (bucket='{}', key='{}'): {}", bucketName, cleanKey, e.getMessage(), e);
            throw new RuntimeException("Cloudflare R2 upload error: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] loadFile(String pathOrKey) {
        if (pathOrKey == null || pathOrKey.isBlank()) {
            throw new IllegalArgumentException("Cannot load file with null or empty key/path.");
        }

        // Backward compatibility: If the file exists on the local fallback disk, load it
        try {
            Path localPath = Paths.get(pathOrKey);
            if (Files.exists(localPath) && Files.isRegularFile(localPath)) {
                log.debug("Found local file fallback for '{}'", pathOrKey);
                return Files.readAllBytes(localPath);
            }
        } catch (Exception ignored) {}

        String cleanKey = extractKey(pathOrKey);
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(cleanKey)
                    .build();

            return s3Client.getObjectAsBytes(request).asByteArray();

        } catch (NoSuchKeyException e) {
            log.warn("Object not found in Cloudflare R2: bucket='{}', key='{}'", bucketName, cleanKey);
            throw new RuntimeException("File not found in storage: " + cleanKey, e);
        } catch (Exception e) {
            log.error("Failed to load object from Cloudflare R2 (key='{}'): {}", cleanKey, e.getMessage(), e);
            throw new RuntimeException("Cloudflare R2 download error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String pathOrKey) {
        if (pathOrKey == null || pathOrKey.isBlank()) {
            return false;
        }

        try {
            Path localPath = Paths.get(pathOrKey);
            if (Files.exists(localPath)) {
                return true;
            }
        } catch (Exception ignored) {}

        String cleanKey = extractKey(pathOrKey);
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(cleanKey)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.warn("Error checking file existence in Cloudflare R2 for key '{}': {}", cleanKey, e.getMessage());
            return false;
        }
    }

    @Override
    public void deleteFile(String pathOrKey) {
        if (pathOrKey == null || pathOrKey.isBlank()) {
            return;
        }

        try {
            Path localPath = Paths.get(pathOrKey);
            Files.deleteIfExists(localPath);
        } catch (Exception ignored) {}

        String cleanKey = extractKey(pathOrKey);
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(cleanKey)
                    .build();
            s3Client.deleteObject(request);
            log.info("Deleted object from Cloudflare R2: key='{}'", cleanKey);
        } catch (Exception e) {
            log.warn("Failed to delete object from Cloudflare R2 (key='{}'): {}", cleanKey, e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String pathOrKey) {
        String cleanKey = extractKey(pathOrKey);
        if (publicUrl != null) {
            return publicUrl + "/" + cleanKey;
        }
        return cleanKey;
    }

    @Override
    public String getStorageType() {
        return "CLOUDFLARE_R2";
    }

    private String sanitizeKey(String key) {
        if (key == null) return "";
        String trimmed = key.trim().replace('\\', '/');
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed;
    }

    private String extractKey(String pathOrKey) {
        String sanitized = sanitizeKey(pathOrKey);
        if (publicUrl != null && sanitized.startsWith(publicUrl)) {
            sanitized = sanitized.substring(publicUrl.length());
            sanitized = sanitizeKey(sanitized);
        }
        if (sanitized.startsWith("r2://" + bucketName + "/")) {
            sanitized = sanitized.substring(("r2://" + bucketName + "/").length());
        }
        return sanitized;
    }
}
