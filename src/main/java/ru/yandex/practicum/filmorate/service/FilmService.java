package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
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
        log.info("Добавление фильма: {}", film);
        validateFilm(film);
        Film created = filmStorage.addFilm(film);
        likesMap.putIfAbsent(created.getId(), new HashSet<>());
        log.debug("Фильм добавлен с id={}", created.getId());
        return created;
    }

    public Film updateFilm(Film film) {
        log.info("Обновление фильма: {}", film);

        requireFilmExists(film.getId());
        validateFilm(film);

        Film updated = filmStorage.updateFilm(film);

        likesMap.putIfAbsent(updated.getId(), new HashSet<>());
        log.debug("Фильм обновлён: id={}", updated.getId());

        return updated;
    }


    public Collection<Film> getAllFilms() {
        log.info("Запрос списка всех фильмов");
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        log.info("Запрос фильма по id={}", id);
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", id);
                    return new NotFoundException("Фильм с id " + id + " не найден");
                });
    }


    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму {} от пользователя {}", filmId, userId);

        requireFilmExists(filmId);
        requireUserExists(userId);

        likesMap.putIfAbsent(filmId, new HashSet<>());
        likesMap.get(filmId).add(userId);

        log.debug("У фильма {} теперь {} лайков", filmId, likesMap.get(filmId).size());
    }


    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма {} от пользователя {}", filmId, userId);

        requireFilmExists(filmId);
        requireUserExists(userId);

        Set<Long> likes = likesMap.get(filmId);
        if (likes != null) {
            likes.remove(userId);
        }

        log.debug("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }


    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }

        log.info("Запрос топ-{} популярных фильмов", count);

        List<Film> films = new ArrayList<>(filmStorage.getAllFilms());
        films.sort((f1, f2) -> {
            int likes1 = likesMap.getOrDefault(f1.getId(), Collections.emptySet()).size();
            int likes2 = likesMap.getOrDefault(f2.getId(), Collections.emptySet()).size();
            return Integer.compare(likes2, likes1);
        });

        List<Film> result = films.stream().limit(count).collect(Collectors.toList());
        log.debug("Сформирован список популярных фильмов: {}",
                result.stream().map(Film::getId).toList());
        return result;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate() == null ||
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }

    private Film requireFilmExists(Long filmId) {
        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
    }

    private void requireUserExists(Long userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }


}
