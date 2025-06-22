package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.ApiException;
import com.ecommerce.project.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    ProductRepository productRepository;

    @Value("${project.imagesPath")
    private String imagePath;

    public String uploadImage(MultipartFile image) {
        String originalFileName = image.getOriginalFilename();

        // Create the file name with a unique identifier to avoid conflicts
        String fileName;
        do {
            String randomId = UUID.randomUUID().toString().substring(0, 8);
            fileName = randomId + "_" + originalFileName;
        } while (productRepository.existsByImage((fileName)));

        // Create the directory if it does not exist
        File folder = new File(imagePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Create the path with the image name
        String filePath = imagePath + File.separator + fileName;

        // Save the image to the specified path
        try {
            Files.copy(image.getInputStream(), Paths.get(filePath));
        } catch (Exception e) {
            throw new ApiException("Error uploading image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return filePath;
    }

    public void deleteImage(String path) {
        if (path.equals("default.png")) {
            return; // Do not delete the default image
        }

        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception e) {
            throw new ApiException("Error deleting image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
