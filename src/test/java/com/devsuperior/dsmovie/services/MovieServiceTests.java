package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {

    @InjectMocks
    private MovieService service;

    @Mock
    private MovieRepository repository;

    private Long existingMovieId;
    private Long nonExistingMovieId;
    private Long dependentScoreId;
    private String title;
    private MovieEntity movie;
    private MovieDTO movieDTO;
    private PageImpl<MovieEntity> page;

    @BeforeEach
    void setUp() throws Exception {
        existingMovieId = 1L;
        nonExistingMovieId = 1000L;
        dependentScoreId = 1L;
        title = "Test Movie";
        movie = MovieFactory.createMovieEntity();
        movieDTO = new MovieDTO(movie);
        page = new PageImpl<>(List.of(movie));

        Mockito.when(repository.searchByTitle(any(), any())).thenReturn(page);
        Mockito.when(repository.save(any())).thenReturn(movie);

        Mockito.when(repository.findById(existingMovieId)).thenReturn(Optional.of(movie));
        Mockito.when(repository.findById(nonExistingMovieId)).thenReturn(Optional.empty());

        Mockito.when(repository.getReferenceById(existingMovieId)).thenReturn(movie);
        Mockito.when(repository.getReferenceById(nonExistingMovieId)).thenThrow(EntityNotFoundException.class);

        Mockito.when(repository.existsById(existingMovieId)).thenReturn(Boolean.TRUE);
        Mockito.when(repository.existsById(dependentScoreId)).thenReturn(Boolean.TRUE);
        Mockito.when(repository.existsById(nonExistingMovieId)).thenReturn(Boolean.FALSE);

        Mockito.doNothing().when(repository).deleteById(existingMovieId);
    }

    @Test
    void findAllShouldReturnPagedMovieDTO() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<MovieDTO> result = service.findAll(title, pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getSize(), 1);
        Assertions.assertEquals(result.iterator().next().getTitle(), title);
    }

    @Test
    void findByIdShouldReturnMovieDTOWhenIdExists() {
        MovieDTO result = service.findById(existingMovieId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingMovieId);
        Assertions.assertEquals(result.getTitle(), movie.getTitle());
    }

    @Test
    void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.findById(nonExistingMovieId));
    }

    @Test
    void insertShouldReturnMovieDTO() {
        MovieDTO result = service.insert(movieDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), movie.getId());
    }

    @Test
    void updateShouldReturnMovieDTOWhenIdExists() {
        MovieDTO result = service.update(existingMovieId, movieDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingMovieId);
        Assertions.assertEquals(result.getTitle(), movieDTO.getTitle());
    }

    @Test
    void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.update(nonExistingMovieId, movieDTO));
    }

    @Test
    void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> service.delete(existingMovieId));
    }

    @Test
    void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.delete(nonExistingMovieId));
    }

    @Test
    void deleteShouldThrowDatabaseExceptionWhenDependentId() {
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentScoreId);
        Assertions.assertThrows(DatabaseException.class, () -> service.delete(dependentScoreId));
    }
}
