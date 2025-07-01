package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.BookAdditionRequest;
import com.example.libraryproject.model.dto.UserDTO;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.Set;

public interface BookKeeperService {

    void addBook(BookAdditionRequest bookRequest);

    void deleteBook(String bookPublicId);

    void tookBook(String orderPublicId);

    void banUser(String username);

    void unbanUser(String username);

    Set<UserDTO> getUsers();

    String downloadImage(Part filePart) throws IOException;

}
