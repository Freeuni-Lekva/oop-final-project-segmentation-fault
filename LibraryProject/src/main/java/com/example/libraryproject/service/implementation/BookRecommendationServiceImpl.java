package com.example.libraryproject.service.implementation;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.BookDTO;
import com.example.libraryproject.model.entity.Book;
import com.example.libraryproject.model.entity.Review;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.repository.BookRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.BookRecommendationService;
import com.example.libraryproject.utilities.Mappers;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BookRecommendationServiceImpl implements BookRecommendationService {

    private static final int[] coefficients = {0, -3,-1,0,1,3};

    private static final int RECOMMENDED_COUNT = Integer.parseInt(ApplicationProperties.get("recommendation.recommended-count"));
    private static final int TOP_GENRE_COUNT = Integer.parseInt(ApplicationProperties.get("recommendation.top-genre-count"));
    private static final int TOP_AUTHORS_COUNT = Integer.parseInt(ApplicationProperties.get("recommendation.top-authors-count"));

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(BookRecommendationServiceImpl.class);

    public Set<BookDTO> recommendBooks(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Book> readBooks = user.getReadBooks();
        Set<Review> userReviews = user.getReviews();
        logger.info("Recommending books for user: {}", user.getUsername());

        Set<Book> recommendedBooks = getPreferredBooks(readBooks, userReviews);

        return recommendedBooks
                .stream()
                .map(Mappers::mapBookToDTO)
                .collect(Collectors.toSet());
    }

    private Set<Book> getPreferredBooks(Set<Book> readBooks, Set<Review> reviews) {

        Map<String, Double> authorScores = new HashMap<>();
        Map<String, Double> genreScores = new HashMap<>();

        Map<Book, Integer> bookRatings = reviews.stream()
                .collect(Collectors.toMap(Review::getBook, Review::getRating));

        for (Book book : readBooks) {
            int rating = bookRatings.get(book);
            String author = book.getAuthor();
            String category = book.getGenre();
            double mappedRating = coefficients[rating];

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

        List<Book> filteredBooks = bookRepository.findByAuthorsAndGenres(topAuthorNames,topGenreNames,readBooks);

        List<Book> chosenBooks = applyCoefficients(topAuthorNames,topGenreNames,authorScores,genreScores,filteredBooks);

        Collections.shuffle(chosenBooks);

        return chosenBooks.stream()
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

}
