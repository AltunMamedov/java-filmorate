package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friendsMap = new HashMap<>();

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (!friendsMap.containsKey(userId)) {
            friendsMap.put(userId, new HashSet<>());
        }
        friendsMap.get(userId).add(friendId);


        if (!friendsMap.containsKey(friendId)) {
            friendsMap.put(friendId, new HashSet<>());
        }
        friendsMap.get(friendId).add(userId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

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
        User user = getUserById(userId);

        Set<Long> friendIds = friendsMap.getOrDefault(userId, new HashSet<>());
        Set<User> friends = new HashSet<>();
        for (Long id : friendIds) {
            friends.add(getUserById(id));
        }

        log.info("Пользователь {} имеет {} друзей", userId, friends.size());
        return friends;
    }

    public Set<User> getCommonFriends(Long userId, Long otherId) {
        User user = getUserById(userId);
        User other = getUserById(otherId);

        Set<Long> userFriends = friendsMap.getOrDefault(userId, new HashSet<>());
        Set<Long> otherFriends = friendsMap.getOrDefault(otherId, new HashSet<>());

        Set<Long> commonIds = new HashSet<>(userFriends);
        commonIds.retainAll(otherFriends);

        Set<User> commonFriends = new HashSet<>();
        for (Long id : commonIds) {
            commonFriends.add(getUserById(id));
        }

        log.info("Пользователи {} и {} имеют {} общих друзей", userId, otherId, commonFriends.size());
        return commonFriends;
    }
}
