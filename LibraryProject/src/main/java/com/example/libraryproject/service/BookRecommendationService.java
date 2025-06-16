package com.example.libraryproject.service;

import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.BookRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.libraryproject.configuration.ApplicationProperties.*;

@RequiredArgsConstructor
public class BookRecommendationService {
    private static final Map<Integer, Integer> OFFSET_MAP;

    private final BookRepository bookRepository;

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
                .limit(TOP_AUTHORS_COUNT)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<String> topGenreNames = genreScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(TOP_GENRE_COUNT)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        List<Book> filtered = bookRepository.findByAuthorsAndGenres(topAuthorNames,topGenreNames,readBooks);
        List<Book> doubleFiltered = applyCoefficients(topAuthorNames,topGenreNames,authorScores,genreScores,filtered);

        Collections.shuffle(doubleFiltered);

        return doubleFiltered.stream()
                .limit(RECOMMENDED_COUNT)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<Book> applyCoefficients(Set<String> topAuthorNames,
                                  Set<String> topGenreNames,
                                  Map<String, Double> authorScores,
                                  Map<String, Double> genreScores,
                                  List<Book> candidateBooks){
        int totalCount = RECOMMENDED_COUNT;

        Map<String, Double> combinedScores = new HashMap<>();

        for (String author : topAuthorNames) {
            combinedScores.put("A:" + author, authorScores.getOrDefault(author, 0.0));
        }
        for (String genre : topGenreNames) {
            combinedScores.put("G:" + genre, genreScores.getOrDefault(genre, 0.0));
        }

        double totalScore = combinedScores.values().stream().mapToDouble(Double::doubleValue).sum();

        Map<String, Integer> allocations = new HashMap<>();
        for (var entry : combinedScores.entrySet()) {
            double fraction = entry.getValue() / totalScore;
            int count = (int) Math.round(fraction * totalCount);
            allocations.put(entry.getKey(), count);
        }

        Map<String, List<Book>> booksByAuthor = candidateBooks.stream()
                .filter(b -> topAuthorNames.contains(b.getAuthor()))
                .collect(Collectors.groupingBy(Book::getAuthor));

        Map<String, List<Book>> booksByGenre = candidateBooks.stream()
                .filter(b -> topGenreNames.contains(b.getGenre()))
                .collect(Collectors.groupingBy(Book::getGenre));

        Set<Book> result = new LinkedHashSet<>();

        for (String key : allocations.keySet()) {
            int count = allocations.get(key);
            List<Book> pool;

            if (key.startsWith("A:")) {
                String author = key.substring(2);
                pool = booksByAuthor.getOrDefault(author, Collections.emptyList());
            } else {
                String genre = key.substring(2);
                pool = booksByGenre.getOrDefault(genre, Collections.emptyList());
            }

            Collections.shuffle(pool);
            result.addAll(pool.stream().limit(count).toList());
        }

        if (result.size() < totalCount) {
            List<Book> remaining = new ArrayList<>(candidateBooks);
            remaining.removeAll(result);
            Collections.shuffle(remaining);
            result.addAll(remaining.stream().limit(totalCount - result.size()).toList());
        }

        return new ArrayList<>(result);
    }

    public Set<Book> recommendBooks(User user){
        Set<Book> readBooks = user.getReadBooks();
        Set<Review> userReviews = user.getReviews();

        return getPreferredBooks(readBooks, userReviews);
    }
}
