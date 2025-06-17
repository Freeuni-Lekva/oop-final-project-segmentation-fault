//package com.example.libraryproject.service;
//
//import com.example.libraryproject.model.entity.Book;
//import com.example.libraryproject.model.entity.User;
//import com.example.libraryproject.repository.BookKeeperRepository;
//import com.example.libraryproject.repository.BookRepository;
//import com.example.libraryproject.repository.ReviewRepository;
//import com.example.libraryproject.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDate;
//import java.util.HashSet;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import static org.mockito.Mockito.mock;
//
//public class UserServiceTest {
//    private UserRepository userRepository;
//    private BookKeeperRepository bookKeeperRepository;
//    private ReviewRepository reviewRepository;
//    private UserService userService;
//    private BookRepository bookRepository;
//    private User user;
//
//    private Book book1,book2,book3;
//
//    @BeforeEach
//    void setup() {
//        userRepository = mock(UserRepository.class);
//        bookKeeperRepository = mock(BookKeeperRepository.class);
//        reviewRepository = mock(ReviewRepository.class);
//        userService = new UserService(userRepository,bookRepository,reviewRepository);
//
//        book1 = new Book(
//                "Shadow Realms",
//                "Fantasy",
//                "Author A",
//                LocalDate.of(2020, 5, 10),
//                "A gripping tale of magic and destiny.",
//                1L, 1L, 4L, ""
//        );
//
//        book2 = new Book(
//                "Shadow Realms II",
//                "Fantasy",
//                "Author A",
//                LocalDate.of(2021, 6, 15),
//                "The saga continues with greater peril.",
//                2L, 0L, 5L, ""
//        );
//
//        book3 = new Book(
//                "Echoes of Power",
//                "Fantasy",
//                "Author A",
//                LocalDate.of(2019, 4, 20),
//                "Ancient secrets resurface to test the realm.",
//                3L, 3L, 3L, ""
//        );
//
//        bookRepository.save(book1);
//        bookRepository.save(book2);
//        bookRepository.save(book3);
//
//        user = new User("rezi","1234");
//        userRepository.save(user);
//    }
//
//    @Test
//    public void testReserve(){
//        assertTrue(userService.reserveBook(user,book1));
//        assertFalse(userService.reserveBook(user,book1));
//
//        assertTrue(userService.cancelReservation(user,book1));
//        assertTrue(userService.reserveBook(user,book1));
//        assertFalse(userService.cancelReservation(user,book1));
//
//        assertFalse(userService.reserveBook(user,book2));
//    }
//
//    @Test
//    public void testChangePassword(){
//        assertFalse(userService.changePassword(user,"wrongPassword", "newPassword"));
//        assertTrue(userService.changePassword(user,"1234","newPassword"));
//        assertEquals("newPassword", user.getPassword());
//    }
//
//    @Test
//    public void testReview(){
//        Set<Book> updatedReadBooks = new HashSet<>(user.getReadBooks());
//        updatedReadBooks.add(book1);
//        user.setReadBooks(updatedReadBooks);
//
//        assertFalse(userService.reviewBook(user,book2,4,"good"));
//        assertTrue(userService.reviewBook(user,book1,4,"good"));
//        assertTrue(
//                book1.getReviews().stream()
//                        .anyMatch(review -> review.getUser().equals(user))
//        );
//    }
//}
