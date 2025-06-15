package com.example.libraryproject.service;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.BookRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BookRecommendationService {

    private static final int DEFAULT_RATING = 3;
    private static final int RECOMMENDED_COUNT = 20;
    private final BookRepository bookRepository;

    private static final Map<Integer, Integer> OFFSET_MAP;

    static {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, -3);
        map.put(2, -1);
        map.put(3, 0);
        map.put(4, 1);
        map.put(5, 3);
        OFFSET_MAP = Collections.unmodifiableMap(map);
    }

    private int getRating(Book book, Set<Review> reviews){
        int rating = DEFAULT_RATING;

        for (Review review : reviews){
            if (review.getBook().equals(book)){
                rating = review.getRating();
                break;
            }
        }

        return rating;
    }

    private Set<Book> getPreferredBooks(Set<Book> readBooks, Set<Review> reviews) {
        Map<String, Double> authorScores = new HashMap<>();
        Map<String, Double> genreScores = new HashMap<>();

        for (Book book : readBooks) {
            int rating = getRating(book, reviews);
            String author = book.getAuthor();
            String category = book.getGenre();
            double mappedRating = OFFSET_MAP.get(rating);

            authorScores.merge(author, mappedRating, Double::sum);
            genreScores.merge(category, mappedRating, Double::sum);
        }

        Set<String> topAuthorNames = authorScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<String> topGenreNames = genreScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        List<Book> allBooks = bookRepository.findAll();
        List<Book> filtered;

        if (topAuthorNames.isEmpty() && topGenreNames.isEmpty()) {
            filtered = new ArrayList<>(allBooks);
        } else {
            filtered = allBooks.stream()
                    .filter(book -> (topAuthorNames.contains(book.getAuthor()) || topGenreNames.contains(book.getGenre()))
                            && !readBooks.contains(book))
                    .collect(Collectors.toList());
        }

        Collections.shuffle(filtered);

        return filtered.stream()
                .limit(RECOMMENDED_COUNT)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Book> recommendBooks(User user){
        Set<Book> readBooks = user.getReadBooks();
        Set<Review> userReviews = user.getReviews();

        return getPreferredBooks(readBooks, userReviews);
    }
}
