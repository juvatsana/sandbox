@ExtendWith(MockitoExtension.class)
class EnvironnementControllerTest {

    @Mock
    private FindAllEnvironnementsUseCase findAllEnvironnementsUseCase;

    @Mock
    private EnvironnementDtoMapper environnementDtoMapper;

    @InjectMocks
    private EnvironnementController controller;

    @Test
    void findAllEnvironnements_shouldReturnListOfResponses() {
        // GIVEN
        var env1 = new Object(); // remplace par le vrai type de domaine
        var env2 = new Object();
        var response1 = new EnvironnementResponse();
        var response2 = new EnvironnementResponse();

        when(findAllEnvironnementsUseCase.execute(null)).thenReturn(List.of(env1, env2));
        when(environnementDtoMapper.toResponse(env1)).thenReturn(response1);
        when(environnementDtoMapper.toResponse(env2)).thenReturn(response2);

        // WHEN
        ResponseEntity<List<EnvironnementResponse>> result = controller.findAllEnvironnements();

        // THEN
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).containsExactly(response1, response2);
        verify(findAllEnvironnementsUseCase).execute(null);
    }

    @Test
    void findAllEnvironnements_shouldReturnEmptyList_whenNoneExist() {
        // GIVEN
        when(findAllEnvironnementsUseCase.execute(null)).thenReturn(List.of());

        // WHEN
        ResponseEntity<List<EnvironnementResponse>> result = controller.findAllEnvironnements();

        // THEN
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEmpty();
    }

    @Test
    void findAllEnvironnements_shouldCallUseCaseExactlyOnce() {
        when(findAllEnvironnementsUseCase.execute(null)).thenReturn(List.of());

        controller.findAllEnvironnements();

        verify(findAllEnvironnementsUseCase, times(1)).execute(null);
        verifyNoMoreInteractions(findAllEnvironnementsUseCase);
    }
}
