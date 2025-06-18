package com.example.libraryproject.servlet;

import com.example.libraryproject.service.BookService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.example.libraryproject.configuration.ApplicationProperties.BOOK_SERVICE_ATTRIBUTE_NAME;

@WebServlet(name = "BookServlet",urlPatterns = {"/api/books/*"})
public class BookServlet extends HttpServlet {

    private BookService bookService;


//    @Override
//    public void init() throws ServletException {
//        super.init();
//        bookService = (BookService) getServletContext().getAttribute(BOOK_SERVICE_ATTRIBUTE_NAME);
//    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        bookService = (BookService) getServletContext().getAttribute(BOOK_SERVICE_ATTRIBUTE_NAME);
        String[] pathParts = request.getPathInfo().substring(1).split("/");
        String path = pathParts[0];
        switch (path) {
            case "all":
                response.getWriter().write(bookService.getAllBooks().toString());
                break;
            case "details":
                response.getWriter().write(bookService.getBookDetails(pathParts[1]).toString());
                break;
            case "available":
                response.getWriter().write(bookService.getAvailableBooks().toString());
                break;
            case "get-books-by-genre":
                response.getWriter().write(bookService.getBooksByGenre(pathParts[1]).toString());
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

}