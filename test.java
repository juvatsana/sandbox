@ExtendWith(MockitoExtension.class)
class UpdateFonctionnaliteUseCaseTest {

    @Mock
    private FonctionnaliteRepository fonctionnaliteRepository;

    @Mock
    private HistoriqueRepository historiqueRepository;

    @InjectMocks
    private UpdateFonctionnaliteUseCase useCase;

    private UpdateFonctionnaliteCommand buildCommand() {
        var fonctionnalite = mock(Fonctionnalite.class);
        when(fonctionnalite.getId()).thenReturn(1L);
        when(fonctionnalite.getNom()).thenReturn("Nouveau nom");

        var command = mock(UpdateFonctionnaliteCommand.class);
        when(command.fonctionnalite()).thenReturn(fonctionnalite);
        when(command.utilisateurId()).thenReturn(10L);
        when(command.commentaire()).thenReturn("Mise à jour");
        return command;
    }

    // --- Cas nominal ---

    @Test
    void execute_found_updatesAndSavesHistorique() {
        var command = buildCommand();

        var existing = mock(Fonctionnalite.class);
        when(existing.getId()).thenReturn(1L);
        when(existing.getProduitId()).thenReturn(2L);
        when(existing.getApplication()).thenReturn("APP");
        when(existing.getCreePar()).thenReturn("admin");
        when(existing.getCreeLe()).thenReturn(LocalDateTime.of(2024, 1, 1, 0, 0));

        when(fonctionnaliteRepository.getFonctionnalite(1L))
            .thenReturn(Optional.of(existing));

        var saved = mock(Fonctionnalite.class);
        when(fonctionnaliteRepository.saveFonctionnalite(any())).thenReturn(saved);

        var result = useCase.execute(command);

        assertThat(result).isEqualTo(saved);

        // Vérifie que l'historique a bien été sauvegardé
        verify(historiqueRepository).save(any(Historique.class));

        // Vérifie que saveFonctionnalite a été appelé avec le bon nom
        verify(fonctionnaliteRepository).saveFonctionnalite(
            argThat(f -> f.getNom().equals("Nouveau nom"))
        );
    }

    // --- Cas not found ---

    @Test
    void execute_notFound_throwsIllegalArgumentException() {
        var command = buildCommand();

        when(fonctionnaliteRepository.getFonctionnalite(1L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Fonctionnalite introuvable");

        // Rien ne doit être sauvegardé
        verifyNoInteractions(historiqueRepository);
        verify(fonctionnaliteRepository, never()).saveFonctionnalite(any());
    }
}
