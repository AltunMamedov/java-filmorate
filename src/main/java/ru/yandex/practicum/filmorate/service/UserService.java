package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friendsMap = new HashMap<>();

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        log.info("Добавление пользователя: {}", user);
        validateUser(user);
        User created = userStorage.addUser(user);
        friendsMap.putIfAbsent(created.getId(), new HashSet<>());
        log.debug("Пользователь добавлен с id={}", created.getId());
        return created;
    }

    public User updateUser(User user) {
        log.info("Обновление пользователя: {}", user);

        requireUserExists(user.getId());
        validateUser(user);

        User updated = userStorage.updateUser(user);

        friendsMap.putIfAbsent(updated.getId(), new HashSet<>());
        log.debug("Пользователь обновлён: id={}", updated.getId());

        return updated;
    }


    public Collection<User> getAllUsers() {
        log.info("Запрос списка всех пользователей");
        return userStorage.getAllUsers();
    }

    public User getUserById(Long id) {
        log.info("Запрос пользователя по id={}", id);
        return requireUserExists(id);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить в друзья сам себя");
        }
        log.info("Добавление в друзья: userId={} friendId={}", userId, friendId);

        requireUserExists(userId);
        requireUserExists(friendId);

        friendsMap.putIfAbsent(userId, new HashSet<>());
        friendsMap.putIfAbsent(friendId, new HashSet<>());

        friendsMap.get(userId).add(friendId);
        friendsMap.get(friendId).add(userId);

        log.debug("Теперь пользователи {} и {} друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }
        log.info("Удаление из друзей: userId={} friendId={}", userId, friendId);

        requireUserExists(userId);
        requireUserExists(friendId);

        friendsMap.getOrDefault(userId, new HashSet<>()).remove(friendId);
        friendsMap.getOrDefault(friendId, new HashSet<>()).remove(userId);

        log.debug("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public Set<User> getFriends(Long userId) {
        log.info("Запрос списка друзей пользователя {}", userId);
        requireUserExists(userId);

        Set<Long> ids = friendsMap.getOrDefault(userId, Collections.emptySet());

        Set<User> result = ids.stream()
                .map(this::requireUserExists)
                .collect(Collectors.toSet());

        log.debug("У пользователя {} {} друзей: {}", userId, result.size(),
                result.stream().map(User::getId).toList());

        return result;
    }

    public Set<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Запрос общих друзей пользователей {} и {}", userId, otherId);

        requireUserExists(userId);
        requireUserExists(otherId);

        Set<Long> userFriends = friendsMap.getOrDefault(userId, Collections.emptySet());
        Set<Long> otherFriends = friendsMap.getOrDefault(otherId, Collections.emptySet());

        Set<Long> commonIds = new HashSet<>(userFriends);
        commonIds.retainAll(otherFriends);

        Set<User> result = commonIds.stream()
                .map(this::requireUserExists)
                .collect(Collectors.toSet());

        log.debug("Общие друзья пользователей {} и {} ({}): {}", userId, otherId, result.size(),
                result.stream().map(User::getId).toList());

        return result;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный логин");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private User requireUserExists(Long userId) {
        return userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }
}
