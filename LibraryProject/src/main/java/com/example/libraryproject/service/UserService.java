package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.UserDTO;

public interface UserService {

    void reviewBook(String username, String publicId, int rating, String comment);

    void reserveBook(String username, String publicId);

    void cancelReservation(String username, String publicId);

    void changePassword(String username, String oldPassword, String newPassword);

    UserDTO getUserInfo(String username);
}
