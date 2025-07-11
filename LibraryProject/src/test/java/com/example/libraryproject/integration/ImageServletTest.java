package com.example.libraryproject.integration;

import com.example.libraryproject.servlet.ImageServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageServletTest {

    private static final Logger logger = LoggerFactory.getLogger(ImageServletTest.class);
    private static Server server;
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    @BeforeAll
    public static void setUpServer() throws Exception {
        logger.info("Starting integration test setup...");

        try {
            // Create embedded Jetty server for testing
            logger.info("Creating Jetty server...");
            server = new Server(8080);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            
            // Set the resource base to include test resources
            String webappDir = "src/main/webapp";
            context.setBaseResource(Resource.newResource(webappDir));
            
            // Add the ImageServlet
            context.addServlet(new ServletHolder(new ImageServlet()), "/images/*");
            
            // Add DefaultServlet to serve static resources
            ServletHolder defaultServlet = new ServletHolder("default", DefaultServlet.class);
            defaultServlet.setInitParameter("dirAllowed", "false");
            context.addServlet(defaultServlet, "/");

            server.setHandler(context);

            logger.info("Starting server...");
            server.start();
            logger.info("Server started successfully on port 8080");

            // Wait a bit for server to fully start
            Thread.sleep(1000);

        } catch (Exception e) {
            logger.error("Failed to set up test server", e);
            throw e;
        }
    }

    @AfterAll
    public static void tearDownServer() throws Exception {
        logger.info("Shutting down test server...");

        try {
            if (server != null && server.isRunning()) {
                server.stop();
                logger.info("Server stopped successfully");
            }
        } catch (Exception e) {
            logger.error("Error stopping server", e);
        }
    }

    @Test
    @Order(1)
    public void testGetWebappImage() throws Exception {
        logger.info("Starting testGetWebappImage...");
        
        // Use an image that exists in the webapp directory
        String webappImageName = "circle_owl.png";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/images/" + webappImageName))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        logger.info("Get webapp image response status: {}", response.statusCode());
        assertEquals(200, response.statusCode(), "Getting webapp image should succeed");
        
        // Verify the content type
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.contains("image"), "Response should have an image content type");
        
        // Verify that we got some image data (non-empty response)
        assertTrue(response.body().length > 0, "Image data should not be empty");
    }
    
    @Test
    @Order(2)
    public void testImageNotFound() throws Exception {
        logger.info("Starting testImageNotFound...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/images/nonexistent-image.jpg"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get nonexistent image response status: {}", response.statusCode());
        assertEquals(404, response.statusCode(), "Should return 404 for nonexistent image");
    }
    
    @Test
    @Order(3)
    public void testNoImagePath() throws Exception {
        logger.info("Starting testNoImagePath...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/images/"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        logger.info("Get no image path response status: {}", response.statusCode());
        assertEquals(404, response.statusCode(), "Should return 404 for no image path");
    }
}
