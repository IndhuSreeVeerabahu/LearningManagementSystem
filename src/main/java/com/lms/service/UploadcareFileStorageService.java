package com.lms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * Uploadcare implementation of FileStorageService.
 * This integrates with the Uploadcare REST API to store files in the cloud.
 */
@Service
public class UploadcareFileStorageService implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(UploadcareFileStorageService.class);

    @Value("${uploadcare.public-key}")
    private String publicKey;

    @Value("${uploadcare.secret-key}")
    private String secretKey;

    @Value("${uploadcare.cdn-url}")
    private String cdnUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String UPLOADCARE_UPLOAD_URL = "https://upload.uploadcare.com/base/";
    private static final String UPLOADCARE_API_URL = "https://api.uploadcare.com/files/";

    @Override
    public String uploadFile(MultipartFile file, String subdirectory) throws Exception {
        return uploadFile(file, subdirectory, false);
    }
    
    /**
     * Upload file to Uploadcare with optional filename in URL
     * @param file The file to upload
     * @param subdirectory The subdirectory (not used by Uploadcare, kept for compatibility)
     * @param includeFilename Whether to include the original filename in the URL
     * @return The Uploadcare file URL
     */
    public String uploadFile(MultipartFile file, String subdirectory, boolean includeFilename) throws Exception {
        try {
            logger.info("Starting file upload to Uploadcare: {} (size: {} bytes)", 
                       file.getOriginalFilename(), file.getSize());
            
            // Validate public key
            if (publicKey == null || publicKey.trim().isEmpty()) {
                throw new Exception("Uploadcare public key is not configured");
            }
            
            // Prepare the upload request
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("UPLOADCARE_PUB_KEY", publicKey);
            body.add("file", file.getResource());
            
            logger.debug("Uploading with public key: {}", publicKey);
            logger.debug("File name: {}", file.getOriginalFilename());
            logger.debug("File size: {} bytes", file.getSize());
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Upload file to Uploadcare
            ResponseEntity<String> response = restTemplate.postForEntity(
                UPLOADCARE_UPLOAD_URL, requestEntity, String.class);
            
            logger.debug("Upload response status: {}", response.getStatusCode());
            logger.debug("Upload response body: {}", response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody().trim();
                
                // Parse the JSON response to extract file ID
                String fileId = extractFileIdFromJsonResponse(responseBody);
                
                if (fileId == null) {
                    throw new Exception("Could not extract file ID from response: " + responseBody);
                }
                
                logger.info("Extracted file ID: {}", fileId);
                
                // Construct the file URL using the configured CDN URL
                String baseUrl = cdnUrl.endsWith("/") ? cdnUrl.substring(0, cdnUrl.length() - 1) : cdnUrl;
                String fileUrl;
                
                if (includeFilename && file.getOriginalFilename() != null) {
                    // Include filename in URL for better SEO and user experience
                    String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());
                    fileUrl = baseUrl + "/" + fileId + "/-/inline/" + sanitizedFilename;
                } else {
                    // Simple UUID-based URL
                    fileUrl = baseUrl + "/" + fileId + "/";
                }
                
                logger.info("Constructed file URL: {}", fileUrl);
                
                logger.info("File uploaded successfully to Uploadcare: {}", fileUrl);
                
                return fileUrl;
            } else {
                throw new Exception("Upload failed with status: " + response.getStatusCode() + ", body: " + response.getBody());
            }
            
        } catch (IOException e) {
            logger.error("IO error during file upload: {}", file.getOriginalFilename(), e);
            throw new Exception("IO error during file upload: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error uploading file: {}", file.getOriginalFilename(), e);
            throw new Exception("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        try {
            logger.info("Deleting file from Uploadcare: {}", fileUrl);
            
            // Extract file ID from URL
            String fileId = extractFileIdFromUrl(fileUrl);
            
            if (fileId != null) {
                // Create authorization header
                String auth = publicKey + ":" + secretKey;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Basic " + encodedAuth);
                
                HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                
                // Delete file from Uploadcare
                restTemplate.delete(UPLOADCARE_API_URL + fileId + "/", requestEntity);
                
                logger.info("File deleted successfully from Uploadcare: {}", fileId);
            } else {
                logger.warn("Could not extract file ID from URL: {}", fileUrl);
            }
            
        } catch (Exception e) {
            logger.error("Failed to delete file from Uploadcare: {}", fileUrl, e);
            throw new Exception("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        // If it's already a full URL, return as is
        if (filePath.startsWith("http")) {
            return filePath;
        }
        
        // If it's a file ID, construct the Uploadcare URL
        if (filePath.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
            String baseUrl = cdnUrl.endsWith("/") ? cdnUrl.substring(0, cdnUrl.length() - 1) : cdnUrl;
            return baseUrl + "/" + filePath + "/";
        }
        
        // Otherwise, return as is (might be a relative path)
        return filePath;
    }
    
    /**
     * Normalize Uploadcare URL to use the configured CDN URL
     */
    public String normalizeFileUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("http")) {
            return fileUrl;
        }
        
        // Extract file ID from any Uploadcare URL format
        String fileId = extractFileIdFromUrl(fileUrl);
        if (fileId != null) {
            // Reconstruct URL using the configured CDN URL
            String baseUrl = cdnUrl.endsWith("/") ? cdnUrl.substring(0, cdnUrl.length() - 1) : cdnUrl;
            return baseUrl + "/" + fileId + "/";
        }
        
        return fileUrl;
    }

    @Override
    public boolean fileExists(String fileUrl) {
        try {
            // Extract file ID from URL
            String fileId = extractFileIdFromUrl(fileUrl);
            
            if (fileId != null) {
                // Create authorization header
                String auth = publicKey + ":" + secretKey;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Basic " + encodedAuth);
                
                HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                
                // Try to get file info from Uploadcare
                ResponseEntity<String> response = restTemplate.getForEntity(
                    UPLOADCARE_API_URL + fileId + "/", String.class, requestEntity);
                
                return response.getStatusCode().is2xxSuccessful();
            }
            return false;
        } catch (Exception e) {
            logger.warn("Error checking file existence: {}", fileUrl, e);
            return false;
        }
    }
    
    /**
     * Extract file ID from Uploadcare JSON response.
     */
    private String extractFileIdFromJsonResponse(String jsonResponse) {
        try {
            logger.info("Parsing JSON response: {}", jsonResponse);
            
            // Parse JSON response like: {"file":"b5874cde-efde-43ad-bb29-9d688ccf68af"}
            if (jsonResponse.contains("\"file\"")) {
                int startIndex = jsonResponse.indexOf("\"file\":\"") + 8;
                int endIndex = jsonResponse.indexOf("\"", startIndex);
                logger.info("Start index: {}, End index: {}", startIndex, endIndex);
                
                if (startIndex > 7 && endIndex > startIndex) {
                    String fileId = jsonResponse.substring(startIndex, endIndex);
                    logger.info("Extracted file ID: {}", fileId);
                    return fileId;
                }
            }
            logger.warn("Could not find file ID in JSON response: {}", jsonResponse);
            return null;
        } catch (Exception e) {
            logger.warn("Error parsing JSON response: {}", jsonResponse, e);
            return null;
        }
    }
    
    /**
     * Sanitize filename for URL usage
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) return "";
        
        // Replace spaces with %20 and other special characters
        return filename.replace(" ", "%20")
                      .replace("(", "%28")
                      .replace(")", "%29")
                      .replace("[", "%5B")
                      .replace("]", "%5D")
                      .replace("{", "%7B")
                      .replace("}", "%7D");
    }
    
    /**
     * Extract file ID from Uploadcare URL.
     */
    private String extractFileIdFromUrl(String fileUrl) {
        if (fileUrl == null) {
            return null;
        }
        
        // Handle various Uploadcare URL formats:
        // https://ucarecdn.com/uuid/filename
        // https://2v4g02ejp8.ucarecd.net/uuid/filename
        // https://ucarecdn.com/uuid/
        // https://2v4g02ejp8.ucarecd.net/uuid/
        
        if (fileUrl.contains("ucarecdn.com") || fileUrl.contains("ucarecd.net")) {
            // Extract the path after the domain
            String path;
            if (fileUrl.contains("ucarecdn.com")) {
                path = fileUrl.substring(fileUrl.indexOf("ucarecdn.com") + 12);
            } else {
                path = fileUrl.substring(fileUrl.indexOf("ucarecd.net") + 11);
            }
            
            // Remove leading slash if present
            path = path.startsWith("/") ? path.substring(1) : path;
            
            // Extract the UUID part (first part before the next slash)
            String[] parts = path.split("/");
            if (parts.length > 0) {
                String fileId = parts[0];
                // Validate it's a UUID format
                if (fileId.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
                    return fileId;
                }
            }
        }
        
        return null;
    }
}