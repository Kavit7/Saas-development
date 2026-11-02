package com.saas.backend.serviceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.saas.backend.service.FileStorageService;

import lombok.extern.slf4j.Slf4j;

/**
 * LocalStorageServiceImpl
 * Default file storage provider for local development.
 * Persists files directly to the local directory (e.g. ./uploads).
 */
@Slf4j
public class LocalStorageServiceImpl implements FileStorageService {

    private final String uploadDir;

    public LocalStorageServiceImpl(String uploadDir) {
        this.uploadDir = (uploadDir != null && !uploadDir.isBlank()) ? uploadDir : "uploads";
        initDirectory();
    }

    private void initDirectory() {
        try {
            Path root = Paths.get(uploadDir);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
                log.info("Initialized local file storage directory at: {}", root.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to initialize local storage directory '{}': {}", uploadDir, e.getMessage());
        }
    }

    @Override
    public String storeFile(String key, byte[] content, String contentType) {
        try {
            Path target = resolvePath(key);
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.write(target, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("Saved file locally at: {}", target.toAbsolutePath());
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Failed to store file locally for key '{}': {}", key, e.getMessage(), e);
            throw new RuntimeException("Could not store file locally: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] loadFile(String pathOrKey) {
        try {
            Path target = resolvePath(pathOrKey);
            if (!Files.exists(target)) {
                throw new RuntimeException("File not found at: " + pathOrKey);
            }
            return Files.readAllBytes(target);
        } catch (IOException e) {
            log.error("Failed to read local file '{}': {}", pathOrKey, e.getMessage(), e);
            throw new RuntimeException("Could not load local file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String pathOrKey) {
        if (pathOrKey == null || pathOrKey.isBlank()) {
            return false;
        }
        return Files.exists(resolvePath(pathOrKey));
    }

    @Override
    public void deleteFile(String pathOrKey) {
        if (pathOrKey == null || pathOrKey.isBlank()) {
            return;
        }
        try {
            Path target = resolvePath(pathOrKey);
            Files.deleteIfExists(target);
            log.debug("Deleted local file: {}", target.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not delete local file '{}': {}", pathOrKey, e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String pathOrKey) {
        return resolvePath(pathOrKey).toAbsolutePath().toString();
    }

    @Override
    public String getStorageType() {
        return "LOCAL";
    }

    private Path resolvePath(String pathOrKey) {
        if (pathOrKey == null || pathOrKey.isBlank()) {
            throw new IllegalArgumentException("File key/path cannot be blank.");
        }
        Path direct = Paths.get(pathOrKey);
        if (direct.isAbsolute()) {
            return direct;
        }
        return Paths.get(uploadDir).resolve(pathOrKey).normalize();
    }
}
