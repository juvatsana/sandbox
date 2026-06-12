@ExtendWith(MockitoExtension.class)
class HistoriqueControllerTest {

    @Mock
    private FindHistoriqueByUtilisateurUseCase findHistoriqueByUtilisateurUseCase;

    @Mock
    private HistoriqueDtoMapper historiqueDtoMapper;

    @InjectMocks
    private HistoriqueController controller;

    @Test
    void findHistoriqueByUtilisateurId_returnsOk() {
        var historique = mock(Historique.class);
        var response = mock(HistoriqueResponse.class);

        when(findHistoriqueByUtilisateurUseCase.execute(
            new FindHistoriqueByUtilisateurCommand(1L)))
            .thenReturn(List.of(historique));
        when(historiqueDtoMapper.toResponse(historique)).thenReturn(response);

        var result = controller.findHistoriqueByUtilisateurId(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(response);
    }
}
