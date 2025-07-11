package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.dto.OrderDTO;
import com.example.libraryproject.model.dto.ReviewDTO;
import com.example.libraryproject.model.dto.UserDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Order;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.OrderStatus;
import com.example.libraryproject.model.enums.ReservationResponse;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.OrderRepository;
import com.example.libraryproject.repository.ReviewRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.MailService;
import com.example.libraryproject.service.UserService;
import com.example.libraryproject.utilities.Mappers;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final MailService mailService;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * Calculates the  average rating for a book based on all its reviews
     *
     * @param bookPublicId The public ID of the book
     * @return The average rating, or 0.0 if no reviews exist
     */
    private double calculateAverageRating(String bookPublicId) {
        Set<Review> reviews = reviewRepository.findReviewsByBookPublicId(bookPublicId);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Round to 1 decimal place for cleaner display
        return Math.round(average * 10.0) / 10.0;
    }

    public void reviewBook(String username, String publicId, int rating, String comment) {
        if (username == null) {
            logger.info("Review attempt failed: no user logged in");
            throw new IllegalStateException("User not logged in");
        }

        User user;
        Book book;
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", publicId);
            throw new IllegalStateException("Book not found");
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            throw new IllegalStateException("User not found");
        }
        book = optionalBook.get();
        user = optionalUser.get();

        if (user.getStatus() == com.example.libraryproject.model.enums.UserStatus.BANNED) {
            logger.warn("Banned user {} attempted to write review for book {}", username, publicId);
            throw new IllegalStateException("Your account is banned and cannot write reviews");
        }

        // Check if user has either borrowed or read the book
        boolean hasBorrowed = user.getBorrowedBooks().contains(book);
        boolean hasRead = user.getReadBooks().contains(book);

        if (!hasBorrowed && !hasRead) {
            logger.info("User {} attempted to review book {} without borrowing or reading it first", username, publicId);
            throw new IllegalStateException("You can only review books you have borrowed or read");
        }

        Set<Review> reviews = user.getReviews();

        if(!reviews.stream().filter(r -> r.getBook().equals(book)).toList().isEmpty()) {
            logger.info("User {} attempted duplicate review for book {}", username, publicId);
            throw new IllegalStateException("You have already reviewed this book");
        }

        Review review = new Review();
        review.setUser(user);
        review.setPublicId(UUID.randomUUID());
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment);

        // Save the review to the repository
        reviewRepository.save(review);

        Set<Review> updatedReviews = new HashSet<>(user.getReviews());
        updatedReviews.add(review);
        user.setReviews(updatedReviews);

        userRepository.update(user);

        double newAverageRating = calculateAverageRating(publicId);
        book.setRating(newAverageRating);
        bookRepository.update(book);
        
        logger.info("User {} reviewed book {} with rating {} and comment '{}'. Book rating updated to {}", 
                   username, publicId, rating, comment, newAverageRating);
    }

    public void changeBio(String username, String bio) {
        User user;
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("bio change attempt failed for non-existing user: {}", username);
            throw new IllegalArgumentException("user doesn't exist");
        }
        user = optionalUser.get();

        user.setBio(bio);
        userRepository.update(user);
        logger.info("User {} changed bio successfully", username);
    }

    public ReservationResponse reserveBook(String username, String bookPublicId, Long durationInDays){
        Optional<Book> optionalBook = bookRepository.findByPublicId(bookPublicId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", bookPublicId);
            throw new IllegalArgumentException("Book not found");
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            throw new IllegalArgumentException("User not found");
        }
        Book book = optionalBook.get();
        User user = optionalUser.get();

        if (user.getStatus() == com.example.libraryproject.model.enums.UserStatus.BANNED) {
            logger.info("Banned user {} attempted to reserve book {}", username, bookPublicId);
            throw new IllegalStateException("Your account is banned and cannot reserve books");
        }
        OrderStatus orderStatus;
        if (book.getCurrentAmount() <= 0) {
            orderStatus = OrderStatus.WAITING;
            logger.info("Book with publicId {} is not available for reservation, putting user in the waitlist", bookPublicId);
        } else orderStatus = OrderStatus.RESERVED;

        Set<Order> userOrders = orderRepository.findOrdersByUserId(user.getId());
        boolean hasActiveOrder = userOrders.stream().anyMatch(order -> order.getBook().getPublicId().equals(bookPublicId) &&
                (order.getStatus() == OrderStatus.RESERVED ||
                        order.getStatus() == OrderStatus.BORROWED)
        );

        if (hasActiveOrder) {
            logger.info("User {} already has an active order for book {}", username, bookPublicId);
            throw new IllegalStateException("You already have this book reserved or borrowed");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime borrowDate = orderStatus == OrderStatus.RESERVED ? now.plusDays(1) : null;
        LocalDateTime dueDate = orderStatus == OrderStatus.RESERVED ? now.plusDays(1).plusDays(durationInDays) : null;

        Order order = Order.builder()
                .publicId(UUID.randomUUID())
                .createDate(now)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .requestedDurationInDays(durationInDays)
                .status(orderStatus)
                .user(user)
                .book(book)
                .build();

        if (orderStatus == OrderStatus.RESERVED) {
            book.setCurrentAmount(book.getCurrentAmount() - 1);
            bookRepository.update(book);
            logger.info("User {} reserved book {} with order ID {}", username, bookPublicId, order.getPublicId());
        }
        else {
            logger.info("User {} added to waitlist for book {} with order ID {}", username, bookPublicId, order.getPublicId());
        }
        try {
            mailService.sendEmail(
                    List.of(user.getMail()),
                    "Book Reservation Confirmation",
                    String.format("""
                                    Dear %s,
                                    
                                    Your reservation for the book '%s' has been %s.
                                    Reservation ID: %s
                                    Borrow Date: %s
                                    Due Date: %s
                                    %s
                                    
                                    Thank you for using our library service!
                                    Best regards,
                                    Library Team""",
                            user.getUsername(),
                            book.getName(),
                            orderStatus == OrderStatus.RESERVED ? "confirmed" : "added to waitlist",
                            order.getPublicId(),
                            borrowDate != null ? borrowDate.toLocalDate() : "N/A",
                            dueDate != null ? dueDate.toLocalDate() : "N/A",
                            orderStatus == OrderStatus.RESERVED ? "Please pick up the book within 24 hours, otherwise reservation will be cancelled"
                                    : "You will be notified when the book is available.")
            );
        }  catch (Exception e) {
            logger.error("Failed to send confirmation email to {}: {}", user.getMail(), e.getMessage());
        }
        orderRepository.save(order);
        return orderStatus == OrderStatus.RESERVED ? ReservationResponse.RESERVED : ReservationResponse.WAITLISTED;
    }

    public void cancelReservation(String username, String publicId) {
        Optional<Book> optionalBook = bookRepository.findByPublicId(publicId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", publicId);
            throw new IllegalStateException("Book not found");
        }
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            throw new IllegalStateException("User not found");
        }
        Book book = optionalBook.get();
        User user = optionalUser.get();

        Set<Order> userOrders = orderRepository.findOrdersByUserId(user.getId());
        Optional<Order> reservation = userOrders.stream()
                .filter(order ->
                        order.getBook().getPublicId().equals(publicId) &&
                                order.getStatus() == OrderStatus.RESERVED
                )
                .findFirst();

        if (reservation.isEmpty()) {
            logger.info("User {} does not have book {} reserved", username, publicId);
            throw new IllegalStateException("You don't have this book reserved");
        }

        if (book.getCurrentAmount() == 0) {
            Optional<Order> orderOptional = orderRepository.findFirstWaitingOrderByBookId(book.getId());
            if (orderOptional.isPresent()) {
                Order waitingOrder = orderOptional.get();
                waitingOrder.setStatus(OrderStatus.RESERVED);
                waitingOrder.setBorrowDate(LocalDateTime.now().plusDays(3));
                waitingOrder.setDueDate(LocalDateTime.now().plusDays(3).plusDays(waitingOrder.getRequestedDurationInDays()));

                orderRepository.update(waitingOrder);

                logger.info("User {} canceled reservation for book {}, next user in waitlist has been reserved", username, publicId);
                try {
                    mailService.sendEmail(
                            List.of(waitingOrder.getUser().getMail()),
                            "Book Reservation Confirmation",
                            String.format("""
                                            Dear %s,
                                            
                                            Your reservation for the book '%s' has been confirmed.
                                            Reservation ID: %s
                                            Borrow Date: %s
                                            Due Date: %s
                                            
                                            Please pick up the book within 72 hours, otherwise reservation will be cancelled.
                                            
                                            Thank you for using our library service!
                                            Best regards,
                                            Library Team""",
                                    waitingOrder.getUser().getUsername(),
                                    book.getName(),
                                    waitingOrder.getPublicId(),
                                    waitingOrder.getBorrowDate().toLocalDate(),
                                    waitingOrder.getDueDate().toLocalDate())
                    );
                } catch (Exception e) {
                    logger.error("Failed to send confirmation email to {}: {}", user.getMail(), e.getMessage());
                }
            } else {
                book.setCurrentAmount(book.getCurrentAmount() + 1);
                bookRepository.update(book);

                logger.info("User {} cancelled reservation for book {}, no users in waitlist", username, publicId);
            }
        } else {
            book.setCurrentAmount(book.getCurrentAmount() + 1);
            bookRepository.update(book);
        }

        Order order = reservation.get();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.update(order);

        logger.info("User {} canceled reservation for book {}", username, publicId);

    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user;
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("Password change attempt failed for non-existing user: {}", username);
            throw new IllegalArgumentException("user doesn't exist");
        }
        user = optionalUser.get();

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            logger.info("Password change attempt failed for user {}: incorrect old password", username);
            throw new IllegalArgumentException("password is incorrect");
        }

        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.update(user);
        logger.info("User {} changed password successfully", username);
    }

    public UserDTO getUserInfo(String username) {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            logger.warn("User not found in database: {}", username);
            throw new IllegalArgumentException("user doesn't exist");
        }

        User foundUser = user.get();
        logger.info("Found user: {} with role: {} and status: {}",
                foundUser.getUsername(), foundUser.getRole(), foundUser.getStatus());

        // Get user's orders
        Set<Order> userOrders = orderRepository.findOrdersByUserId(foundUser.getId());
        List<OrderDTO> orderDTOs = userOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.RESERVED)
                .map(Mappers::mapOrderToDTO)
                .collect(Collectors.toList());

        // Get currently reading books
        List<BookDTO> currentlyReadingDTOs = foundUser.getBorrowedBooks().stream()
                .map(Mappers::mapBookToDTO)
                .collect(Collectors.toList());

        // Get read books, excluding currently borrowed or reserved books
        Set<String> activeBookIds = new HashSet<>();
        // Add borrowed book IDs
        activeBookIds.addAll(foundUser.getBorrowedBooks().stream()
                .map(Book::getPublicId)
                .collect(Collectors.toSet()));
        // Add reserved book IDs
        activeBookIds.addAll(userOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.RESERVED)
                .map(order -> order.getBook().getPublicId())
                .collect(Collectors.toSet()));

        // Filter read books to exclude active books
        List<BookDTO> readBookDTOs = foundUser.getReadBooks().stream()
                .filter(book -> !activeBookIds.contains(book.getPublicId()))
                .map(Mappers::mapBookToDTO)
                .collect(Collectors.toList());

        // Get reviews
        List<ReviewDTO> reviewDTOs = foundUser.getReviews().stream()
                .map(Mappers::mapReviewToDTO)
                .collect(Collectors.toList());

        return new UserDTO(
                foundUser.getUsername(),
                foundUser.getBio(),
                readBookDTOs.size(),  // Changed from foundUser.getReadBooks().size()
                reviewDTOs.size(),
                reviewDTOs,
                currentlyReadingDTOs,
                orderDTOs,
                readBookDTOs,
                foundUser.getStatus().name(),
                foundUser.getMail()
        );
    }

    @Override
    public boolean hasUserReservedBook(String username, String bookId) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("User with username {} not found", username);
            return false;
        }

        Optional<Book> optionalBook = bookRepository.findByPublicId(bookId);
        if (optionalBook.isEmpty()) {
            logger.info("Book with publicId {} not found", bookId);
            return false;
        }

        return orderRepository.hasReservation(optionalUser.get().getId(), optionalBook.get().getId());

    }

}
