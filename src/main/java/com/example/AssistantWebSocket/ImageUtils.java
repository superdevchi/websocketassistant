package com.example.AssistantWebSocket;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

public class ImageUtils {

    public String urlToBase64(String imageUrl, float quality, int maxWidth) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage originalImage = ImageIO.read(url);
        if (originalImage == null) {
            throw new IOException("Failed to read image from URL: " + imageUrl);
        }

        // Resize image
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        if (width > maxWidth) {
            height = (int) (height * ((float) maxWidth / width));
            width = maxWidth;
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();
            originalImage = resizedImage;
        }

        // Compress to JPEG
        ByteArrayOutputStream jpegOutput = new ByteArrayOutputStream();
        javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        writer.setOutput(ImageIO.createImageOutputStream(jpegOutput));
        writer.write(null, new javax.imageio.IIOImage(originalImage, null, null), param);
        writer.dispose();
        byte[] jpegBytes = jpegOutput.toByteArray();

        // Save for validation
        try (FileOutputStream fos = new FileOutputStream("test_image.jpg")) {
            fos.write(jpegBytes);
            System.out.println("Saved JPEG to test_image.jpg, size: " + jpegBytes.length + " bytes");
        }

        String base64Image = Base64.getEncoder().encodeToString(jpegBytes);
        System.out.println("Base64 size: " + base64Image.length() + " characters");
        return base64Image;
    }

    public String urlToCompressedBase64(String imageUrl, float quality, int maxWidth) throws IOException {
        // Download image
        URL url = new URL(imageUrl);
        BufferedImage originalImage = ImageIO.read(url);

        // Resize image if needed
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        if (width > maxWidth) {
            height = (int) (height * ((float) maxWidth / width));
            width = maxWidth;
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();
            originalImage = resizedImage;
        }

        // Compress to JPEG with lower quality
        ByteArrayOutputStream jpegOutput = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpeg", jpegOutput);
        byte[] jpegBytes = jpegOutput.toByteArray();

        // GZIP compression
        try (ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutput = new GZIPOutputStream(byteOutput)) {
            gzipOutput.write(jpegBytes);
            gzipOutput.finish();
            byte[] compressedBytes = byteOutput.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(compressedBytes);
            System.out.println("Compressed base64 size: " + base64Image.length() + " characters");
            return base64Image;
        }
    }

}