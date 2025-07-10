package com.example.libraryproject.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(name = "ProfileViewServlet", urlPatterns = {"/user/*"})
public class ProfileViewServlet extends HttpServlet {
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
        System.out.println("PathInfo: " + pathInfo); // Debug log
        
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Split the path into segments
        String[] segments = pathInfo.split("/");
        System.out.println("Number of segments: " + segments.length); // Debug log
        for (int i = 0; i < segments.length; i++) {
            System.out.println("Segment " + i + ": " + segments[i]); // Debug log
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
            System.out.println("Forwarding to my-books.jsp");
            req.getRequestDispatcher("/my-books.jsp").forward(req, resp);
            return;
        } 
        
        // This is a profile request
        System.out.println("Forwarding to profile view"); // Debug log
        String view = req.getParameter("view");
        if ("grid".equals(view)) {
            req.getRequestDispatcher("/my-books.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("/profile.html").forward(req, resp);
        }
    }
}