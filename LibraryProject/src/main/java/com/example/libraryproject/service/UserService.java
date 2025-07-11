package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.model.enums.ReservationResponse;

public interface UserService {

    void reviewBook(String username, String publicId, int rating, String comment);

    ReservationResponse reserveBook(String username, String publicId, Long durationInDays) ;

    void cancelReservation(String username, String publicId) ;

    void changePassword(String username, String oldPassword, String newPassword);

    void changeBio(String username, String bio);

    UserDTO getUserInfo(String username);

    boolean hasUserReservedBook(String username, String bookId);
}
