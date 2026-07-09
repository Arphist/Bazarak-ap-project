package com.bazarak.service;

import com.bazarak.entity.Image;
import com.bazarak.exception.image.*;
import com.bazarak.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    /*
    @Value	                        Spring annotation to inject values
    ${app.upload.dir:uploads}	    Read property app.upload.dir from application.properties
    :                              	Default value separator
    uploads                        	Default value if property is not found
     */
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // UPLOAD IMAGE

    /**
     * Upload and save an image for an ad
     */
    @Transactional
    public Image uploadImage(MultipartFile file, Long adId, boolean isPrimary) throws IOException {
        // 1. Validate File
        validateFile(file);

        // 2. Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 3. Generate unique file name
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
        String filePath = uploadDir + File.separator + uniqueFileName;

        // 4. Save file to disk
        Path filePathObj = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePathObj, StandardCopyOption.REPLACE_EXISTING);

        // 5. Create Image entity
        Image image = new Image(originalFileName, filePath, file.getContentType(), file.getSize());
        image.setPrimary(isPrimary);
        image.setDisplayOrder(imageRepository.countByAdvertisementId(adId));

        return imageRepository.save(image);
    }

    // GET IMAGE

    /**
     * Get all images for an ad
     */
    public List<Image> getImagesByAdId(Long adId) {
        return imageRepository.findByAdvertisementIdOrderByDisplayOrderAsc(adId);
    }

    /**
     * Get primary image for an ad
     */
    public Image getPrimaryImage(Long adId) {
        return imageRepository.findPrimaryImageByAdId(adId);
    }

    /**
     * Get image by ID
     */
    public Image getImageById(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new InvalidImageOperationException("Image not found with id: " + imageId));
    }

    // DELETE IMAGES

    /**
     * Delete an image by ID
     */
    @Transactional
    public void deleteImage(Long imageId) throws IOException {
        Image image = getImageById(imageId);

        // Delete file from disk
        Path filePath = Paths.get(image.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Delete from database
        imageRepository.delete(image);
    }
    /**
     * Delete all images for an ad
     */
    @Transactional
    public void deleteAllImages(Long adId) throws IOException{
        List<Image> images = getImagesByAdId(adId);

        // Delete files from disk
        for (Image image: images){
            Path filePath = Paths.get(image.getFilePath());
            if (Files.exists(filePath)){
                Files.delete(filePath);
            }
        }

        // Delete from database
        imageRepository.deleteByAdvertisementId(adId);
    }

    // SET PRIMARY IMAGE
    /**
     * Set an image as primary for its ad
     */
    @Transactional
    public Image setPrimaryImage(Long imageId){
        Image image = getImageById(imageId);
        Long adId = image.getAdvertisement().getId();

        // Set all images for this ad to not-primary
        List<Image> images = getImagesByAdId(adId);
        for (Image img : images){
            img.setPrimary(false);
            imageRepository.save(img);
        }

        // Set this image as primary
        image.setPrimary(true);
        return imageRepository.save(image);
    }

    // HELPER METHOD
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileIsEmptyException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileIsEmptyException("File must be an image");
        }

        long maxSize = 10 * 1024 * 1024; //10Mb
        if (file.getSize() > maxSize) {
            throw new InvalidImageOperationException("File size exceeds 10MB limit");
        }

        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String extension = getFileExtension(fileName.toLowerCase());
            String[] allowedExtensions = {"jpg", "jpeg", "png", "gif", "webp"};
            boolean isValid = false;
            for (String ext : allowedExtensions) {
                if (ext.equals(extension)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                throw new InvalidImageOperationException("Invalid file type. Allowed: JPG, JPEG, PNG, GIF, WEBP");
            }
        }
    }

    /**
     * Get file extension from file name
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
