package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;


public interface FilmStorage {
    Film addFilm(Film film);

    Optional<Film> updateFilm(Film film);

    Collection<Film> getAllFilms();

    Optional<Film> getFilmById(Long id);

}