@ExtendWith(MockitoExtension.class)
class ProduitControllerTest {

    @Mock
    private FindAllProduitsUseCase findAllProduitsUseCase;

    @Mock
    private FindProduitByIdUseCase findProduitByIdUseCase;

    @Mock
    private FindProduitByCodeUseCase findProduitByCodeUseCase;

    @Mock
    private ProduitDtoMapper produitDtoMapper;

    @InjectMocks
    private ProduitController controller;

    // --- findAllProduits ---

    @Test
    void findAllProduits_returnsOk() {
        var produit = mock(Produit.class);
        var response = mock(ProduitResponse.class);

        when(findAllProduitsUseCase.execute(null)).thenReturn(List.of(produit));
        when(produitDtoMapper.toResponse(produit)).thenReturn(response);

        var result = controller.findAllProduits();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(response);
    }

    // --- findProduitById ---

    @Test
    void findProduitById_found_returnsOk() {
        var produit = mock(Produit.class);
        var response = mock(ProduitResponse.class);

        when(findProduitByIdUseCase.execute(new FindProduitByIdCommand(1L)))
            .thenReturn(Optional.of(produit));
        when(produitDtoMapper.toResponse(produit)).thenReturn(response);

        var result = controller.findProduitById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void findProduitById_notFound_returns404() {
        when(findProduitByIdUseCase.execute(any())).thenReturn(Optional.empty());

        var result = controller.findProduitById(99L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- findProduitByCode ---

    @Test
    void findProduitByCode_found_returnsOk() {
        var produit = mock(Produit.class);
        var response = mock(ProduitResponse.class);

        when(findProduitByCodeUseCase.execute(new FindProduitByCodeCommand("ABC")))
            .thenReturn(Optional.of(produit));
        when(produitDtoMapper.toResponse(produit)).thenReturn(response);

        var result = controller.findProduitByCode("ABC");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void findProduitByCode_notFound_returns404() {
        when(findProduitByCodeUseCase.execute(any())).thenReturn(Optional.empty());

        var result = controller.findProduitByCode("INCONNU");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
