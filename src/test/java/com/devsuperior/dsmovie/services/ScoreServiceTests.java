package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {

    @InjectMocks
    private ScoreService service;

    @Mock
    private UserService userService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScoreRepository repository;

    private Double existingScore, nonExistingScore;
    private Long existingMovieId, nonExistingMovieId;
    private ScoreDTO scoreDTO;
    private ScoreEntity score;
    private MovieEntity movie;
    private UserEntity user;
    private MovieDTO movieDTO;


    @BeforeEach
    void setUp() {
        existingScore = 4.5;
        nonExistingScore = 1000.0;
        existingMovieId = 1L;
        nonExistingMovieId = 1000L;

        score = ScoreFactory.createScoreEntity();
        scoreDTO = ScoreFactory.createScoreDTO();
        movieDTO = MovieFactory.createMovieDTO();

        movie = MovieFactory.createMovieEntity();
        movie.getScores().add(score);
        user = UserFactory.createUserEntity();

        when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movie));
        when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());
        when(userService.authenticated()).thenReturn(user);
        when(repository.saveAndFlush(any())).thenReturn(score);
        when(movieRepository.save(any())).thenReturn(movie);
    }

    @Test
    public void saveScoreShouldReturnMovieDTO() {
        MovieDTO result = service.saveScore(scoreDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(score.getValue(), existingScore);
    }

    @Test
    public void saveScoreShouldThrowsResourceNotFoundExceptionWhenNonExistingMovieId() {
        MovieRepository spyMovieRepository = Mockito.spy(movieRepository);
        doThrow(ResourceNotFoundException.class).when(spyMovieRepository).findById(nonExistingMovieId);
        assertThrows(ResourceNotFoundException.class, () -> service.saveScore(scoreDTO));
    }
}
