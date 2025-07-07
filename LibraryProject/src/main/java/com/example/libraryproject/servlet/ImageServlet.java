package com.example.libraryproject.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
        if (imageDir == null || imageDir.isBlank()) {
            System.out.println("IMAGE_DIR environment variable not set");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path externalPath = Paths.get(imageDir, fileName);
        File image = externalPath.toFile();
        if (!image.exists()) {
            System.out.println("Image not found: " + fileName);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = getServletContext().getMimeType(fileName);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        response.setContentLengthLong(image.length());


        try (FileInputStream fileInputStrim = new FileInputStream(image);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buff = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInputStrim.read(buff)) != -1) {
                outputStream.write(buff, 0, bytesRead);
            }
        }

    }
}
