package com.example.libraryproject.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(name = "ProfileViewServlet", urlPatterns = {"/user/*"})
public class ProfileViewServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProfileViewServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // Check if user is authenticated
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            // User is not logged in, redirect to login page
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        // Get the path info (everything after /user/)
        String pathInfo = req.getPathInfo();
        logger.debug("PathInfo: {}", pathInfo);
        
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Split the path into segments
        String[] segments = pathInfo.split("/");
        logger.debug("Number of segments: {}", segments.length);
        for (int i = 0; i < segments.length; i++) {
            logger.debug("Segment {}: {}", i, segments[i]);
        }

        if (segments.length < 2) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String username = segments[1];
        // Verify the user is accessing their own profile/books
        String sessionUsername = (String) session.getAttribute("username");
        if (!username.equals(sessionUsername)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Check if this is a my-books request
        if (segments.length >= 3 && "my-books".equals(segments[2])) {
            logger.debug("Forwarding to my-books.jsp");
            req.getRequestDispatcher("/my-books.jsp").forward(req, resp);
            return;
        } 
        
        // This is a profile request
        logger.debug("Forwarding to profile view");
        String view = req.getParameter("view");
        if ("grid".equals(view)) {
            req.getRequestDispatcher("/my-books.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("/profile.html").forward(req, resp);
        }
    }
}