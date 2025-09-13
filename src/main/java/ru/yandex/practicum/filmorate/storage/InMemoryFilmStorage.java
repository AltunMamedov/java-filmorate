package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Film addFilm(Film film) {

        long id = idGenerator.incrementAndGet();
        film.setId(id);
        films.put(id, film);
        log.info("Добавлен фильм '{}' с id={}", film.getName(), id);
        return film;
    }

    @Override
    public Optional<Film> updateFilm(Film film) {

        films.put(film.getId(), film);
        log.info("Фильм обновлён id={}", film.getId());
        return Optional.ofNullable(film);
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }


}
