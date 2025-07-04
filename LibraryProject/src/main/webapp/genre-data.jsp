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

    Map<String, String> classics = new HashMap<>();
    classics.put("name", "Classics");
    classics.put("description", "A classic has a certain universal appeal. Great works of literature touch us to our very core beings--partly because they integrate themes that are understood by readers from a wide range of backgrounds and levels of experience.");
    genreData.put("classics", classics);

    Map<String, String> fiction = new HashMap<>();
    fiction.put("name", "Fiction");
    fiction.put("description", "Fiction is the art of storytelling based on imagination rather than fact. " +
            "While it may include real people, places, or events, its core lies in creative narrative. " +
            "Often seen as a form of art or entertainment, fiction reflects a fundamental part of human culture — " +
            "our ability to imagine, create, and share stories.");
    genreData.put("fiction", fiction);

    Map<String, String> crime = new HashMap<>();
    crime.put("name", "Crime");
    crime.put("description", "Crime fiction is a gripping exploration of justice, morality, " +
            "and the darker sides of human nature. These stories pull readers into a world of lawbreakers, " +
            "investigators, and blurred ethical lines — where motives are murky, truth is elusive, and every detail matters. " +
            "Whether centered on detectives, criminals, or ordinary people caught in extraordinary circumstances, " +
            "crime fiction challenges us to uncover the truth and question what justice really means.");
    genreData.put("crime", crime);

    Map<String, String> mystery = new HashMap<>();
    mystery.put("name", "Mystery");
    mystery.put("description", "Mystery is a genre of fiction centered around uncovering secrets—most " +
            "often involving a crime like a murder or disappearance. " +
            "These stories invite readers to play detective, piecing together clues to solve the puzzle. " +
            "Whether led by a seasoned investigator or an unlikely amateur, mystery novels are driven by suspense, twists, and the thrill of discovering the truth hidden beneath the surface.");
    genreData.put("mystery", mystery);

    Map<String, String> romance = new HashMap<>();
    romance.put("name", "Romance");
    romance.put("description", "Romance is a genre focused on the complexities of human relationships, " +
            "often exploring how love develops, endures, or falters. " +
            "These stories highlight emotional connection, personal growth, " +
            "and the choices people make in pursuit of companionship. " +
            "Romance can unfold in any setting — from everyday life to extraordinary circumstances — " +
            "but at its core, it examines the ways people come together and what that connection means.");
    genreData.put("romance", romance);

    Map<String, String> memoir = new HashMap<>();
    memoir.put("name", "Memoir");
    memoir.put("description", "Memoir is a form of storytelling rooted in truth, " +
            "where authors reflect on pivotal moments in their lives to explore identity, memory, and meaning. " +
            "More than a record of events, a memoir offers insight into how personal experiences shape one’s view of the world. " +
            "Through honesty and introspection, memoirs invite readers to witness a life as it was lived — not just what happened, but what it felt like.");
    genreData.put("memoir", memoir);

    Map<String, String> fantasy = new HashMap<>();
    fantasy.put("name", "Fantasy");
    fantasy.put("description", "Fantasy is a genre that builds worlds beyond the boundaries of reality, where magic, " +
            "mythical creatures, and imagined realms shape the rules of existence. At its core, fantasy explores timeless themes—power, " +
            "destiny, courage, and transformation—through stories set in landscapes both epic and intimate. " +
            "Whether rooted in folklore or entirely invented, fantasy invites readers to escape the ordinary and confront the extraordinary.");
    genreData.put("fantasy", fantasy);

    Map<String, String> horror = new HashMap<>();
    horror.put("name", "Horror");
    horror.put("description", "Horror is a genre designed to evoke fear, unease, and psychological tension. " +
            "It explores the darker corners of human experience—confronting the unknown, the supernatural, or the monstrous within. " +
            "Whether grounded in reality or steeped in the surreal, horror stories tap into our deepest anxieties, " +
            "using suspense and dread to challenge the boundary between safety and danger, sanity and chaos.");
    genreData.put("horror", horror);

    Map<String, String> history = new HashMap<>();
    history.put("name", "History");
    history.put("description", "History is a genre dedicated to exploring the people, events, and forces that have shaped our world. " +
            "Through detailed narratives and careful research, historical works bring the past to life—revealing not only what happened, " +
            "but why it mattered. Whether focused on grand empires or untold personal stories, history helps us understand the present by " +
            "examining the paths that led us here.");
    genreData.put("history", history);

    Map<String, String> poetry = new HashMap<>();
    poetry.put("name", "Poetry");
    poetry.put("description", "Poetry distills human emotion, experience, and thought into rhythmic, expressive language. Through verse, poets capture beauty, pain, love, and truth in ways that challenge, inspire, and move readers.");
    genreData.put("poetry", poetry);

    Map<String, String> adventure = new HashMap<>();
    adventure.put("name", "Adventure");
    adventure.put("description", "Adventure stories are driven by action, risk, and excitement. Whether through treacherous journeys, uncharted lands, or daring missions, this genre celebrates courage, survival, and the thrill of the unknown.");
    genreData.put("adventure", adventure);

    Map<String, String> comedy = new HashMap<>();
    comedy.put("name", "Comedy");
    comedy.put("description", "Comedy is a genre crafted to entertain and provoke laughter. It highlights the absurdities of life and human behavior, often using wit, satire, and clever dialogue to explore serious topics through humor.");
    genreData.put("comedy", comedy);

    Map<String, String> philosophy = new HashMap<>();
    philosophy.put("name", "Philosophy");
    philosophy.put("description", "Philosophy delves into fundamental questions about existence, knowledge, ethics, and meaning. These works encourage critical thinking, reflection, and exploration of ideas that shape human understanding.");
    genreData.put("philosophy", philosophy);

    Map<String, String> psychology = new HashMap<>();
    psychology.put("name", "Psychology");
    psychology.put("description", "Psychology examines the human mind and behavior. This genre explores topics such as cognition, emotion, development, and mental health, providing insights into what drives thought and action.");
    genreData.put("psychology", psychology);

    Map<String, String> art = new HashMap<>();
    art.put("name", "Art");
    art.put("description", "Art books explore creativity, expression, and aesthetics through visual mediums. Covering everything from technique to history and theory, they celebrate human imagination across cultures and eras.");
    genreData.put("art", art);

    Map<String, String> religion = new HashMap<>();
    religion.put("name", "Religion");
    religion.put("description", "Religion books reflect humanity’s search for meaning, connection, and transcendence. They explore beliefs, traditions, sacred texts, and spiritual practices across diverse faiths and cultures.");
    genreData.put("religion", religion);

    Map<String, String> politics = new HashMap<>();
    politics.put("name", "Politics");
    politics.put("description", "Politics as a genre engages with power, governance, ideologies, and public life. These works investigate systems, policies, leaders, and social change, shaping how we understand and influence the world.");
    genreData.put("politics", politics);


    request.setAttribute("genreData", genreData);
%>
