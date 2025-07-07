package com.example.libraryproject.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet(name = "ImageServlet", urlPatterns = "/images/*")
public class ImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = pathInfo.substring(1);
        String imageDir = System.getenv("IMAGE_DIR");
        if (imageDir != null && !imageDir.isBlank()) {
            Path externalPath = Paths.get(imageDir, fileName);
            File image = externalPath.toFile();
            if (image.exists()) {
                bookCoverImages(image, response);
                return;
            }
        }

        String webappImagePath = "/images/" + fileName;
        InputStream imageStream = getServletContext().getResourceAsStream(webappImagePath);
        if (imageStream != null) {
            backgroundImages(imageStream, fileName, response);
        } else {
            System.out.println("Image not found: " + fileName);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void bookCoverImages(File image, HttpServletResponse response) throws IOException {
        String contentType = getServletContext().getMimeType(image.getName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        response.setContentLengthLong(image.length());

        try (FileInputStream fileInputStream = new FileInputStream(image);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buff = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, bytesRead);
            }
        }
    }

    private void backgroundImages(InputStream imageStream, String fileName, HttpServletResponse response) throws IOException {
        String contentType = getServletContext().getMimeType(fileName);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        try (InputStream is = imageStream;
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buff = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buff)) != -1) {
                outputStream.write(buff, 0, bytesRead);
            }
        }
    }
}
