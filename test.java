@ParameterizedTest
@EnumSource(value = FonctionnaliteStatus.class)
void execute_shouldCallCorrectRepository_forEachStatus(FonctionnaliteStatus status) {
    // chaque status appelle une méthode différente — on vérifie juste qu'aucune exception
    findFonctionnalitesByStatusUseCase.execute(
        new FindFonctionnalitesByStatusCommand(status, environnementId));

    verifyNoMoreInteractions(/* aucun appel inattendu */);
}
