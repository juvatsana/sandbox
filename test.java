@ExtendWith(MockitoExtension.class)
class UtilisateurProduitRoleControllerTest {

    @Mock
    private AssignRoleToUtilisateurUseCase assignRoleToUtilisateurUseCase;

    @Mock
    private FindRolesByUtilisateurUseCase findRolesByUtilisateurUseCase;

    @Mock
    private FindRolesByProduitUseCase findRolesByProduitUseCase;

    @Mock
    private CheckCanUpdateFonctionnaliteUseCase checkCanUpdateFonctionnaliteUseCase;

    @Mock
    private UtilisateurProduitRoleDtoMapper utilisateurProduitRoleDtoMapper;

    @InjectMocks
    private UtilisateurProduitRoleController controller;

    // --- assignRole ---

    @Test
    void assignRole_returnsResponse() {
        var request = mock(UtilisateurProduitRoleRequest.class);
        var expected = mock(UtilisateurProduitRoleResponse.class);

        when(request.utilisateurId()).thenReturn(1L);
        when(request.produitId()).thenReturn(2L);
        when(request.role()).thenReturn("ADMIN");
        when(assignRoleToUtilisateurUseCase.execute(any())).thenReturn(mock(UtilisateurProduitRole.class));
        when(utilisateurProduitRoleDtoMapper.toResponse(any())).thenReturn(expected);

        var result = controller.assignRole(request);

        assertThat(result).isEqualTo(expected);
    }

    // --- findRolesByUtilisateur ---

    @Test
    void findRolesByUtilisateur_returnsList() {
        var role = mock(UtilisateurProduitRole.class);
        var response = mock(UtilisateurProduitRoleResponse.class);

        when(findRolesByUtilisateurUseCase.execute(new FindRolesByUtilisateurCommand(10L)))
            .thenReturn(List.of(role));
        when(utilisateurProduitRoleDtoMapper.toResponse(role)).thenReturn(response);

        var result = controller.findRolesByUtilisateur(10L);

        assertThat(result).containsExactly(response);
    }

    // --- findRolesByProduitId ---

    @Test
    void findRolesByProduitId_returnsList() {
        var role = mock(UtilisateurProduitRole.class);
        var response = mock(UtilisateurProduitRoleResponse.class);

        when(findRolesByProduitUseCase.execute(new FindRolesByProduitCommand(5L)))
            .thenReturn(List.of(role));
        when(utilisateurProduitRoleDtoMapper.toResponse(role)).thenReturn(response);

        var result = controller.findRolesByProduitId(5L);

        assertThat(result).containsExactly(response);
    }

    // --- canUpdateFeature ---

    @Test
    void canUpdateFeature_allowed_returnsTrue() {
        when(checkCanUpdateFonctionnaliteUseCase.execute(any())).thenReturn(true);

        var result = controller.canUpdateFeature(1L, 2L);

        assertThat(result.utilisateurId()).isEqualTo(1L);
        assertThat(result.produitId()).isEqualTo(2L);
        assertThat(result.allowed()).isTrue();
    }

    @Test
    void canUpdateFeature_notAllowed_returnsFalse() {
        when(checkCanUpdateFonctionnaliteUseCase.execute(any())).thenReturn(false);

        var result = controller.canUpdateFeature(1L, 2L);

        assertThat(result.allowed()).isFalse();
    }
}
