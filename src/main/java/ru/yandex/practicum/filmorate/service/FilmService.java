package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> likesMap = new HashMap<>();

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        Film created = filmStorage.addFilm(film);
        likesMap.putIfAbsent(created.getId(), new HashSet<>());
        return created;
    }

    public Film updateFilm(Film film) {
        Film updated = filmStorage.updateFilm(film);
        likesMap.putIfAbsent(updated.getId(), new HashSet<>());
        return updated;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        return Optional.ofNullable(filmStorage.getFilmById(id))
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId);
        userStorage.getUserById(userId); // will throw if not found

        likesMap.putIfAbsent(filmId, new HashSet<>());
        likesMap.get(filmId).add(userId);

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmById(filmId);
        userStorage.getUserById(userId);

        Set<Long> likes = likesMap.get(filmId);
        if (likes != null) {
            likes.remove(userId);
        }

        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> films = new ArrayList<>(filmStorage.getAllFilms());
        films.sort((f1, f2) -> {
            int likes1 = likesMap.getOrDefault(f1.getId(), Collections.emptySet()).size();
            int likes2 = likesMap.getOrDefault(f2.getId(), Collections.emptySet()).size();
            return Integer.compare(likes2, likes1);
        });
        return films.stream().limit(count).collect(Collectors.toList());
    }
}
