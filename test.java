@ExtendWith(MockitoExtension.class)
class EvaluateFonctionnaliteUseCaseTest {

    @Mock
    private Validator validator;

    @Mock
    private FonctionnaliteRepository fonctionnaliteRepository;

    @Mock
    private FonctionnaliteEnvironnementRepository fonctionnaliteEnvironnementRepository;

    @InjectMocks
    private EvaluateFonctionnaliteUseCase useCase;

    private EvaluateFonctionnaliteCommand buildCommand() {
        var command = mock(EvaluateFonctionnaliteCommand.class);
        when(command.getIdFonctionnalite()).thenReturn(1L);
        when(command.getCodeEnvironnement()).thenReturn("PROD");
        return command;
    }

    // --- Violation de contrainte ---

    @Test
    void execute_validationFails_throwsConstraintViolationException() {
        var command = buildCommand();
        var violation = mock(ConstraintViolation.class);

        when(validator.validate(command)).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(ConstraintViolationException.class);

        verifyNoInteractions(fonctionnaliteRepository);
    }

    // --- Fonctionnalite introuvable ---

    @Test
    void execute_fonctionnaliteNotFound_returnsFalse() {
        var command = buildCommand();

        when(validator.validate(command)).thenReturn(Set.of());
        when(fonctionnaliteRepository.getFonctionnalite(1L))
            .thenReturn(Optional.empty());

        var result = useCase.execute(command);

        assertThat(result).isFalse();
    }

    // --- FonctionnaliteEnvironnement introuvable ---

    @Test
    void execute_environnementNotFound_returnsFalse() {
        var command = buildCommand();
        var fonctionnalite = mock(Fonctionnalite.class);

        when(validator.validate(command)).thenReturn(Set.of());
        when(fonctionnaliteRepository.getFonctionnalite(1L))
            .thenReturn(Optional.of(fonctionnalite));
        when(fonctionnaliteEnvironnementRepository
            .findByFonctionnaliteIdAndEnvironnementCode(1L, "PROD"))
            .thenReturn(Optional.empty());

        var result = useCase.execute(command);

        assertThat(result).isFalse();
    }

    // --- Fonctionnalite désactivée ---

    @Test
    void execute_fonctionnaliteDesactivee_returnsFalse() {
        var command = buildCommand();
        var fonctionnalite = mock(Fonctionnalite.class);
        var fonctionnaliteEnv = mock(FonctionnaliteEnvironnement.class);

        when(validator.validate(command)).thenReturn(Set.of());
        when(fonctionnaliteRepository.getFonctionnalite(1L))
            .thenReturn(Optional.of(fonctionnalite));
        when(fonctionnaliteEnvironnementRepository
            .findByFonctionnaliteIdAndEnvironnementCode(1L, "PROD"))
            .thenReturn(Optional.of(fonctionnaliteEnv));
        when(fonctionnaliteEnv.getActif()).thenReturn(Boolean.FALSE);

        var result = useCase.execute(command);

        assertThat(result).isFalse();
    }

    // --- Fonctionnalite active ---

    @Test
    void execute_fonctionnaliteActive_returnsTrue() {
        var command = buildCommand();
        var fonctionnalite = mock(Fonctionnalite.class);
        var fonctionnaliteEnv = mock(FonctionnaliteEnvironnement.class);

        when(validator.validate(command)).thenReturn(Set.of());
        when(fonctionnaliteRepository.getFonctionnalite(1L))
            .thenReturn(Optional.of(fonctionnalite));
        when(fonctionnaliteEnvironnementRepository
            .findByFonctionnaliteIdAndEnvironnementCode(1L, "PROD"))
            .thenReturn(Optional.of(fonctionnaliteEnv));
        when(fonctionnaliteEnv.getActif()).thenReturn(Boolean.TRUE);

        var result = useCase.execute(command);

        assertThat(result).isTrue();
    }
}
