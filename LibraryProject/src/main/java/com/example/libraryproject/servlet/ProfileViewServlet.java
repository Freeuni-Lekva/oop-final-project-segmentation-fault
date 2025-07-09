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
        
        // User is authenticated, proceed to profile page
        req.getRequestDispatcher("/profile.html").forward(req, resp);
    }
}