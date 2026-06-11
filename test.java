package fr.macifvie.api.ffms.provider.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FonctionnaliteClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private FonctionnaliteClient fonctionnaliteClient;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        fonctionnaliteClient = new FonctionnaliteClient(webClientBuilder);
    }

    @Test
    void evaluer_shouldReturnTrue_whenApiReturnsActiveTrue() {
        // Given
        EvaluationResponse response = new EvaluationResponse();
        response.setActive(true);

        mockWebClientChain(response);

        // When
        boolean result = fonctionnaliteClient.evaluer(42, "monProduit", "PROD");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void evaluer_shouldReturnFalse_whenApiReturnsActiveFalse() {
        // Given
        EvaluationResponse response = new EvaluationResponse();
        response.setActive(false);

        mockWebClientChain(response);

        // When
        boolean result = fonctionnaliteClient.evaluer(42, "monProduit", "PROD");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void evaluer_shouldReturnFalse_whenApiReturnsNull() {
        // Given
        mockWebClientChainWithNull();

        // When
        boolean result = fonctionnaliteClient.evaluer(42, "monProduit", "PROD");

        // Then
        assertThat(result).isFalse();
    }

    // --- Helpers ---

    private void mockWebClientChain(EvaluationResponse response) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EvaluationResponse.class))
                .thenReturn(Mono.just(response));
    }

    private void mockWebClientChainWithNull() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(EvaluationResponse.class))
                .thenReturn(Mono.empty());
    }
}        
