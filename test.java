package com.example.controller;

import com.example.application.usecase.EvaluateFonctionnaliteUseCase;
import com.example.application.usecase.GetAllFonctionnalitesUseCase;
import com.example.application.usecase.GetFonctionnaliteByIdUseCase;
import com.example.application.command.GetFonctionnaliteByIdCommand;
import com.example.application.command.EvaluateFonctionnaliteCommand;
import com.example.dto.FonctionnaliteResponse;
import com.example.dto.FonctionnaliteEvaluationResponse;
import com.example.mapper.FonctionnaliteDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FonctionnaliteControllerTest {

    @Mock
    private GetAllFonctionnalitesUseCase getAllFonctionnalitesUseCase;

    @Mock
    private GetFonctionnaliteByIdUseCase getFonctionnaliteByIdUseCase;

    @Mock
    private EvaluateFonctionnaliteUseCase evaluateFonctionnaliteUseCase;

    @Mock
    private FonctionnaliteDtoMapper fonctionnaliteDtoMapper;

    @InjectMocks
    private FonctionnaliteController controller;

    private FonctionnaliteResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new FonctionnaliteResponse(); // adapte selon ton constructeur
    }

    // --- getFonctionnalites ---

    @Test
    void getFonctionnalites_shouldReturnListOfResponses() {
        // GIVEN
        var fonctionnalite = new Object(); // remplace par le vrai type de domaine
        when(getAllFonctionnalitesUseCase.execute(null)).thenReturn(List.of(fonctionnalite));
        when(fonctionnaliteDtoMapper.toResponse(fonctionnalite)).thenReturn(mockResponse);

        // WHEN
        ResponseEntity<List<FonctionnaliteResponse>> result = controller.getFonctionnalites();

        // THEN
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).hasSize(1).contains(mockResponse);
        verify(getAllFonctionnalitesUseCase).execute(null);
    }

    @Test
    void getFonctionnalites_shouldReturnEmptyList_whenNoneExist() {
        when(getAllFonctionnalitesUseCase.execute(null)).thenReturn(List.of());

        ResponseEntity<List<FonctionnaliteResponse>> result = controller.getFonctionnalites();

        assertThat(result.getBody()).isEmpty();
    }

    // --- getFonctionnaliteById ---

    @Test
    void getFonctionnaliteById_shouldReturnResponse_whenFound() {
        // GIVEN
        Long id = 42L;
        var fonctionnalite = new Object(); // remplace par le vrai type Optional<Fonctionnalite>
        when(getFonctionnaliteByIdUseCase.execute(any(GetFonctionnaliteByIdCommand.class)))
                .thenReturn(Optional.of(fonctionnalite));
        when(fonctionnaliteDtoMapper.toResponse(fonctionnalite)).thenReturn(mockResponse);

        // WHEN
        ResponseEntity<FonctionnaliteResponse> result = controller.getFonctionnaliteById(id);

        // THEN
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(mockResponse);
    }

    @Test
    void getFonctionnaliteById_shouldReturn404_whenNotFound() {
        when(getFonctionnaliteByIdUseCase.execute(any(GetFonctionnaliteByIdCommand.class)))
                .thenReturn(Optional.empty());

        ResponseEntity<FonctionnaliteResponse> result = controller.getFonctionnaliteById(99L);

        assertThat(result.getStatusCode().value()).isEqualTo(404);
    }

    // --- evaluateFonctionnalite ---

    @Test
    void evaluateFonctionnalite_shouldReturnTrue_whenActive() {
        when(evaluateFonctionnaliteUseCase.execute(any(EvaluateFonctionnaliteCommand.class)))
                .thenReturn(true);

        ResponseEntity<FonctionnaliteEvaluationResponse> result =
                controller.evaluateFonctionnalite(1L, "PROD_A", "ENV_PROD");

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().isActive()).isTrue();
    }

    @Test
    void evaluateFonctionnalite_shouldReturnFalse_whenInactive() {
        when(evaluateFonctionnaliteUseCase.execute(any(EvaluateFonctionnaliteCommand.class)))
                .thenReturn(false);

        ResponseEntity<FonctionnaliteEvaluationResponse> result =
                controller.evaluateFonctionnalite(2L, "PROD_B", "ENV_STAGING");

        assertThat(result.getBody().isActive()).isFalse();
    }

    @Test
    void evaluateFonctionnalite_shouldPassCorrectParameters() {
        when(evaluateFonctionnaliteUseCase.execute(any())).thenReturn(true);

        controller.evaluateFonctionnalite(5L, "MON_PRODUIT", "MON_ENV");

        verify(evaluateFonctionnaliteUseCase).execute(
            argThat(cmd -> cmd.id().equals(5L)
                         && cmd.codeProduit().equals("MON_PRODUIT")
                         && cmd.codeEnvironnement().equals("MON_ENV"))
        );
    }
}
