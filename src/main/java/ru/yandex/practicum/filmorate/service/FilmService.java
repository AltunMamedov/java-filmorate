package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;

    private final Map<Long, Set<Long>> likesMap = new HashMap<>();

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film addFilm(Film film) {
        Film savedFilm = filmStorage.addFilm(film);
        log.info("Фильм добавлен: {}", savedFilm);
        return savedFilm;
    }

    public Film updateFilm(Film film) {
        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Фильм обновлён: {}", updatedFilm);
        return updatedFilm;
    }

    public Film getFilmById(Long id) {
        Film film = filmStorage.getFilmById(id);
        log.info("Получен фильм с id {}: {}", id, film);
        return film;
    }

    public Collection<Film> getAllFilms() {
        Collection<Film> films = filmStorage.getAllFilms();
        log.info("Получено {} фильмов", films.size());
        return films;
    }


    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId);

        likesMap.putIfAbsent(filmId, new HashSet<>());
        likesMap.get(filmId).add(userId);

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmById(filmId);

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
            return likes2 - likes1;
        });

        List<Film> popular = films.subList(0, Math.min(count, films.size()));
        log.info("Возвращено {} популярных фильмов", popular.size());
        return popular;
    }
}
