package fr.macifvie.api.ffms.provider.provider;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Value;
import fr.macifvie.api.ffms.provider.client.FonctionnaliteClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FFMSProviderTest {

    @InjectMocks
    private FFMSProvider ffmsProvider;

    @Mock
    private FonctionnaliteClient client;

    @Mock
    private EvaluationContext ctx;

    // --- getMetadata ---

    @Test
    void getMetadata_shouldReturnFfmsProviderName() {
        assertThat(ffmsProvider.getMetadata().getName()).isEqualTo("ffms-provider");
    }

    // --- getBooleanEvaluation ---

    @Test
    void getBooleanEvaluation_shouldReturnTrue_whenClientReturnsTrue() {
        // Given
        when(ctx.getValue("produit")).thenReturn(new Value("monProduit"));
        when(ctx.getValue("environnement")).thenReturn(new Value("PROD"));
        when(client.evaluer(42, "monProduit", "PROD")).thenReturn(true);

        // When
        ProviderEvaluation<Boolean> result = ffmsProvider.getBooleanEvaluation("42", false, ctx);

        // Then
        assertThat(result.getValue()).isTrue();
        verify(client).evaluer(42, "monProduit", "PROD");
    }

    @Test
    void getBooleanEvaluation_shouldReturnFalse_whenClientReturnsFalse() {
        // Given
        when(ctx.getValue("produit")).thenReturn(new Value("monProduit"));
        when(ctx.getValue("environnement")).thenReturn(new Value("PROD"));
        when(client.evaluer(42, "monProduit", "PROD")).thenReturn(false);

        // When
        ProviderEvaluation<Boolean> result = ffmsProvider.getBooleanEvaluation("42", true, ctx);

        // Then
        assertThat(result.getValue()).isFalse();
    }

    @Test
    void getBooleanEvaluation_shouldParseKeyAsIdFonctionnalite() {
        // Given
        when(ctx.getValue("produit")).thenReturn(new Value("autreApp"));
        when(ctx.getValue("environnement")).thenReturn(new Value("DEV"));
        when(client.evaluer(99, "autreApp", "DEV")).thenReturn(true);

        // When
        ffmsProvider.getBooleanEvaluation("99", false, ctx);

        // Then
        verify(client).evaluer(99, "autreApp", "DEV");
    }

    // --- getStringEvaluation ---

    @Test
    void getStringEvaluation_shouldReturnNull() {
        ProviderEvaluation<String> result = ffmsProvider.getStringEvaluation("key", "default", ctx);
        assertThat(result).isNull();
    }

    // --- getIntegerEvaluation ---

    @Test
    void getIntegerEvaluation_shouldReturnNull() {
        ProviderEvaluation<Integer> result = ffmsProvider.getIntegerEvaluation("key", 0, ctx);
        assertThat(result).isNull();
    }

    // --- getDoubleEvaluation ---

    @Test
    void getDoubleEvaluation_shouldReturnNull() {
        ProviderEvaluation<Double> result = ffmsProvider.getDoubleEvaluation("key", 0.0, ctx);
        assertThat(result).isNull();
    }

    // --- getObjectEvaluation ---

    @Test
    void getObjectEvaluation_shouldReturnNull() {
        ProviderEvaluation<Value> result = ffmsProvider.getObjectEvaluation("key", new Value(), ctx);
        assertThat(result).isNull();
    }
}
