package com.lms.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    
    String uploadFile(MultipartFile file, String subdirectory) throws Exception;
    
    void deleteFile(String fileUrl) throws Exception;
    
    String getFileUrl(String filePath);
    
    boolean fileExists(String fileUrl);
}
