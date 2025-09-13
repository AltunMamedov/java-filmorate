package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;


public interface UserStorage {
    User addUser(User user);

    Optional<User> updateUser(User user);

    Collection<User> getAllUsers();

    Optional<User> getUserById(Long id);
}
