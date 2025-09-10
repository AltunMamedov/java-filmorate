package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

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
        User created = userStorage.addUser(user);
        friendsMap.putIfAbsent(created.getId(), new HashSet<>());
        return created;
    }

    public User updateUser(User user) {
        User updated = userStorage.updateUser(user);
        friendsMap.putIfAbsent(updated.getId(), new HashSet<>());
        return updated;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(Long id) {
        return Optional.ofNullable(userStorage.getUserById(id))
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);

        friendsMap.putIfAbsent(userId, new HashSet<>());
        friendsMap.putIfAbsent(friendId, new HashSet<>());

        friendsMap.get(userId).add(friendId);
        friendsMap.get(friendId).add(userId);

        log.info("Пользователь {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);

        Set<Long> userFriends = friendsMap.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }

        Set<Long> friendFriends = friendsMap.get(friendId);
        if (friendFriends != null) {
            friendFriends.remove(userId);
        }

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public Set<User> getFriends(Long userId) {
        getUserById(userId);
        Set<Long> ids = friendsMap.getOrDefault(userId, new HashSet<>());
        return ids.stream().map(userStorage::getUserById).collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Long userId, Long otherId) {
        getUserById(userId);
        getUserById(otherId);

        Set<Long> userFriends = friendsMap.getOrDefault(userId, new HashSet<>());
        Set<Long> otherFriends = friendsMap.getOrDefault(otherId, new HashSet<>());

        Set<Long> commonIds = new HashSet<>(userFriends);
        commonIds.retainAll(otherFriends);

        return commonIds.stream().map(userStorage::getUserById).collect(Collectors.toSet());
    }
}
