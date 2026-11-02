package com.saas.backend.service;

/**
 * FileStorageService
 * Unified file storage abstraction supporting:
 * - Local filesystem storage (Local development)
 * - Cloudflare R2 / S3-compatible Object Storage (Production hosting)
 */
public interface FileStorageService {

    /**
     * Stores file bytes under the specified key/path.
     *
     * @param key         Relative storage key (e.g. "invoices/booking-id/file.pdf")
     * @param content     Raw bytes of the file
     * @param contentType MIME type (e.g. "application/pdf")
     * @return Canonical path, URL, or key reference of the saved file
     */
    String storeFile(String key, byte[] content, String contentType);

    /**
     * Loads the raw byte content of a file given its storage path or key.
     *
     * @param pathOrKey Canonical storage path, URL, or key
     * @return Raw bytes of the file
     */
    byte[] loadFile(String pathOrKey);

    /**
     * Checks if a file exists in the active storage provider.
     *
     * @param pathOrKey Canonical storage path, URL, or key
     * @return true if the file exists
     */
    boolean fileExists(String pathOrKey);

    /**
     * Deletes a file from the active storage provider if present.
     *
     * @param pathOrKey Canonical storage path, URL, or key
     */
    void deleteFile(String pathOrKey);

    /**
     * Retrieves the publicly accessible URL for the file if configured,
     * or a direct reference key/path.
     *
     * @param pathOrKey Canonical storage path, URL, or key
     * @return Public URL or reference path
     */
    String getFileUrl(String pathOrKey);

    /**
     * Returns the name of the active storage provider (e.g. "LOCAL" or "CLOUDFLARE_R2").
     */
    String getStorageType();
}
