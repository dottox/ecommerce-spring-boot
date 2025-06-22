package com.ecommerce.project.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadImage(MultipartFile image);
    void deleteImage(String path);
}
