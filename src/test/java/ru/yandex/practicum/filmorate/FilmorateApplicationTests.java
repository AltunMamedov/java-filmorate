package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    @Autowired
    private FilmController filmController;

    @Autowired
    private UserController userController;

    // --- Тесты фильмов ---
    @Test
    void shouldCreateFilmWhenValid() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmController.createFilm(film);

        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        Film film = new Film();
        film.setName(" ");
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }


    // --- Тесты пользователей ---
    @Test
    void shouldCreateUserWhenValid() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);

        assertNotNull(created.getId());
        assertEquals("testlogin", created.getLogin());
    }

    @Test
    void shouldSetLoginAsNameWhenNameBlank() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login123");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);

        assertEquals("login123", created.getName());
    }

    @Test
    void shouldAddAndRemoveFriend() {
        final User user1 = userController.createUser(new User() {{
            setEmail("user1@mail.com");
            setLogin("user1");
            setBirthday(LocalDate.of(1990, 1, 1));
        }});

        final User user2 = userController.createUser(new User() {{
            setEmail("user2@mail.com");
            setLogin("user2");
            setBirthday(LocalDate.of(1990, 1, 1));
        }});

        userController.addFriend(user1.getId(), user2.getId());
        Set<User> friendsOfUser1 = userController.getFriends(user1.getId());
        Set<User> friendsOfUser2 = userController.getFriends(user2.getId());

        assertTrue(friendsOfUser1.stream().anyMatch(u -> u.getId() == user2.getId()));
        assertTrue(friendsOfUser2.stream().anyMatch(u -> u.getId() == user1.getId()));

        userController.removeFriend(user1.getId(), user2.getId());
        friendsOfUser1 = userController.getFriends(user1.getId());
        friendsOfUser2 = userController.getFriends(user2.getId());

        assertFalse(friendsOfUser1.stream().anyMatch(u -> u.getId() == user2.getId()));
        assertFalse(friendsOfUser2.stream().anyMatch(u -> u.getId() == user1.getId()));
    }

    @Test
    void shouldGetCommonFriends() {
        final User user1 = userController.createUser(new User() {{
            setEmail("a@mail.com");
            setLogin("a");
            setBirthday(LocalDate.of(1990, 1, 1));
        }});

        final User user2 = userController.createUser(new User() {{
            setEmail("b@mail.com");
            setLogin("b");
            setBirthday(LocalDate.of(1990, 1, 1));
        }});

        final User friend = userController.createUser(new User() {{
            setEmail("c@mail.com");
            setLogin("c");
            setBirthday(LocalDate.of(1990, 1, 1));
        }});

        userController.addFriend(user1.getId(), friend.getId());
        userController.addFriend(user2.getId(), friend.getId());

        Set<User> common = userController.getCommonFriends(user1.getId(), user2.getId());
        assertTrue(common.stream().anyMatch(u -> u.getId() == friend.getId()));
    }

}
