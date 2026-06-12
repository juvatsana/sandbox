@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private LoginUseCase loginUseCase;

    @InjectMocks
    private AuthController controller;

    @Test
    void login_returnsOk() {
        var request = mock(LoginRequest.class);
        var response = mock(LoginResponse.class);

        when(request.email()).thenReturn("user@test.com");
        when(request.password()).thenReturn("secret");
        when(loginUseCase.execute(
            new LoginCommand("user@test.com", "secret")))
            .thenReturn(response);

        var result = controller.login(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }
}
