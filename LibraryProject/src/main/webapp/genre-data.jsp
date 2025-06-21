<%--
  Created by IntelliJ IDEA.
  User: elenejobava
  Date: 6/21/25
  Time: 21:38
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%
    Map<String, Map<String, String>> genreData = new HashMap<>();

    Map<String, String> fiction = new HashMap<>();
    fiction.put("name", "Fiction");
    fiction.put("description", "Discover imaginative stories, novels, and literary works that transport you to different worlds and explore the depths of human experience.");
    genreData.put("fiction", fiction);

    Map<String, String> nonFiction = new HashMap<>();
    nonFiction.put("name", "Non-Fiction");
    nonFiction.put("description", "Explore factual books, memoirs, essays, and educational content that inform, inspire, and expand your understanding of the real world.");
    genreData.put("non-fiction", nonFiction);

    Map<String, String> mystery = new HashMap<>();
    mystery.put("name", "Mystery & Thriller");
    mystery.put("description", "Dive into suspenseful tales, crime novels, and psychological thrillers that will keep you on the edge of your seat until the very last page.");
    genreData.put("mystery", mystery);

    Map<String, String> romance = new HashMap<>();
    romance.put("name", "Romance");
    romance.put("description", "Experience heartwarming love stories, passionate relationships, and emotional journeys that celebrate the power of human connection.");
    genreData.put("romance", romance);

    Map<String, String> scienceFiction = new HashMap<>();
    scienceFiction.put("name", "Science Fiction");
    scienceFiction.put("description", "Journey into the future with speculative fiction, space adventures, and technological wonders that push the boundaries of imagination.");
    genreData.put("science-fiction", scienceFiction);

    Map<String, String> fantasy = new HashMap<>();
    fantasy.put("name", "Fantasy");
    fantasy.put("description", "Enter magical realms filled with mythical creatures, epic quests, and extraordinary powers in these enchanting fantasy tales.");
    genreData.put("fantasy", fantasy);

    Map<String, String> biography = new HashMap<>();
    biography.put("name", "Biography");
    biography.put("description", "Learn from the lives of remarkable individuals through detailed accounts of their achievements, struggles, and contributions to society.");
    genreData.put("biography", biography);

    Map<String, String> history = new HashMap<>();
    history.put("name", "History");
    history.put("description", "Explore past civilizations, significant events, and historical figures that shaped our world and continue to influence our present.");
    genreData.put("history", history);

    Map<String, String> classics = new HashMap<>();
    classics.put("name", "Classics");
    classics.put("description", "A classic has a certain universal appeal. Great works of literature touch us to our very core beings--partly because they integrate themes that are understood by readers from a wide range of backgrounds and levels of experience.");
    genreData.put("classics", classics);

    Map<String, String> children = new HashMap<>();
    children.put("name", "Children's Books");
    children.put("description", "Delightful stories, educational content, and colorful adventures designed to entertain, educate, and inspire young readers.");
    genreData.put("children", children);

    Map<String, String> selfHelp = new HashMap<>();
    selfHelp.put("name", "Self-Help");
    selfHelp.put("description", "Practical guides and motivational content to help you improve various aspects of your life, career, and personal development.");
    genreData.put("self-help", selfHelp);

    request.setAttribute("genreData", genreData);
%>
