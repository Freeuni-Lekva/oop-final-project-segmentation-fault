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
        
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Split the path into segments
        String[] segments = pathInfo.split("/");
        
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

        if (segments.length == 2) {
            // Just /user/username - forward to profile view
            req.getRequestDispatcher("/profile.html").forward(req, resp);
        } else if (segments.length >= 3 && "my-books".equals(segments[2])) {
            // /user/username/my-books - forward to my-books view
            req.getRequestDispatcher("/my-books.jsp").forward(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}