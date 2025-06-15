package com.example.libraryproject.service;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class BookKeeperService {

    public static final String ATTRIBUTE_NAME = "BookKeeperService";
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public void addBook(Book book) {
        Optional<Book> existingBook = bookRepository.findByTitle(book.getName());
        if (existingBook.isPresent()) {
            Book bookInLibrary = existingBook.get();
            bookInLibrary.setAmountInLib(bookInLibrary.getAmountInLib() + 1);
            bookRepository.update(bookInLibrary);
        } else {
            book.setAmountInLib((long) 1);
            bookRepository.save(book);
        }
    }

    public void deleteBook(Book book) {
        Optional<Book> existingBook = bookRepository.findByTitle(book.getName());
        if (existingBook.isPresent()) {
            Book bookInLibrary = existingBook.get();
            long amount = bookInLibrary.getAmountInLib();
            if (amount > 1) {
                bookInLibrary.setAmountInLib(amount - 1);
                bookRepository.update(bookInLibrary);
            } else {
                bookRepository.delete(bookInLibrary);
            }
        } else {
            throw new IllegalArgumentException("Book not found. Try again.");
        }
    }

    public void tookBook(Order order) {
        order.setStatus(OrderStatus.BORROWED);
        orderRepository.update(order);
    }

    public void banUser(Order order) {
        User user = order.getUser();
        user.setStatus(UserStatus.BANNED);
        userRepository.update(user);
    }

}
