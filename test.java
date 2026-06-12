@ExtendWith(MockitoExtension.class)
class UtilisateurControllerTest {

    @Mock
    private FindAllUtilisateursUseCase findAllUtilisateursUseCase;

    @Mock
    private FindUtilisateurByIdUseCase findUtilisateurByIdUseCase;

    @Mock
    private FindUtilisateurByEmailUseCase findUtilisateurByEmailUseCase;

    @Mock
    private UtilisateurDtoMapper utilisateurDtoMapper;

    @InjectMocks
    private UtilisateurController controller;

    // --- findAllUtilisateurs ---

    @Test
    void findAllUtilisateurs_returnsOk() {
        var response = mock(UtilisateurResponse.class);
        var utilisateur = mock(Utilisateur.class); // ou le type retourné par le use case

        when(findAllUtilisateursUseCase.execute(null)).thenReturn(List.of(utilisateur));
        when(utilisateurDtoMapper.toResponse(utilisateur)).thenReturn(response);

        ResponseEntity<List<UtilisateurResponse>> result = controller.findAllUtilisateurs();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(response);
    }

    // --- findUtilisateurById ---

    @Test
    void findUtilisateurById_found_returnsOk() {
        var response = mock(UtilisateurResponse.class);

        when(findUtilisateurByIdUseCase.execute(new FindUtilisateurByIdCommand(1L)))
            .thenReturn(Optional.of(mock(Utilisateur.class)));
        when(utilisateurDtoMapper.toResponse(any())).thenReturn(response);

        ResponseEntity<UtilisateurResponse> result = controller.findUtilisateurById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void findUtilisateurById_notFound_returns404() {
        when(findUtilisateurByIdUseCase.execute(any())).thenReturn(Optional.empty());

        ResponseEntity<UtilisateurResponse> result = controller.findUtilisateurById(99L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- findUtilisateurByEmail ---

    @Test
    void findUtilisateurByEmail_found_returnsOk() {
        var response = mock(UtilisateurResponse.class);

        when(findUtilisateurByEmailUseCase.execute(new FindUtilisateurByEmailCommand("a@b.com")))
            .thenReturn(Optional.of(mock(Utilisateur.class)));
        when(utilisateurDtoMapper.toResponse(any())).thenReturn(response);

        ResponseEntity<UtilisateurResponse> result = controller.findUtilisateurByEmail("a@b.com");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void findUtilisateurByEmail_notFound_returns404() {
        when(findUtilisateurByEmailUseCase.execute(any())).thenReturn(Optional.empty());

        ResponseEntity<UtilisateurResponse> result = controller.findUtilisateurByEmail("inconnu@b.com");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
