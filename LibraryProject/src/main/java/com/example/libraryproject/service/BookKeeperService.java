package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.BookAdditionRequest;
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

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public void addBook(BookAdditionRequest bookRequest) {
        Optional<Book> existingBook = bookRepository.findByTitle(bookRequest.title());

        if (existingBook.isPresent()) {
            Book bookInLibrary = existingBook.get();
            bookInLibrary.setAmountInLib(bookInLibrary.getAmountInLib() + 1);
            bookRepository.update(bookInLibrary);
        } else {
            Book book = new Book();
            book.setName(bookRequest.title());
            book.setAuthor(bookRequest.author());
            book.setDescription(bookRequest.description());
            book.setGenre(bookRequest.genre());
            book.setPublicId(bookRequest.title().replaceAll("[^a-zA-Z0-9.\\-]", "_"));

            book.setAmountInLib(1L);
            bookRepository.save(book);
        }
    }

    public void deleteBook(String bookPublicId) {
        Optional<Book> bookOptional = bookRepository.findByPublicId(bookPublicId);
        if (bookOptional.isEmpty()) {
            throw new IllegalArgumentException("Book not found");
        }
        Book book = bookOptional.get();
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

    public void tookBook(String orderPublicId) {
        Optional<Order> orderOptional = orderRepository.findByPublicId(orderPublicId);
        if (orderOptional.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }
        Order order = orderOptional.get();
        order.setStatus(OrderStatus.BORROWED);
        orderRepository.update(order);
    }

    public void banUser(Order order) {
        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        if (orderOptional.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }
        order = orderOptional.get();
        User user = order.getUser();
        user.setStatus(UserStatus.BANNED);
        userRepository.update(user);
    }

}
