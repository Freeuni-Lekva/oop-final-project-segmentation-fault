package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.UserDTO;

public interface UserService {

    boolean reviewBook(String username, String publicId, int rating, String comment);

    boolean reserveBook(String username, String publicId);

    boolean cancelReservation(String username, String publicId);

    void changePassword(String username, String oldPassword, String newPassword);

    void changeBio(String username, String bio);

    UserDTO getUserInfo(String username);

    boolean hasUserReservedBook(String username, String bookId);
}
