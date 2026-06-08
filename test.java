@Override
public Boolean execute(final EvaluateFonctionnaliteCommand command) {

    Set<ConstraintViolation<EvaluateFonctionnaliteCommand>> violations = 
        validator.validate(command);
    if (!violations.isEmpty()) {
        throw new ConstraintViolationException(violations);
    }

    return fonctionnaliteRepository
        .getFonctionnalite(command.getIdFonctionnalite())
        .flatMap(fonctionnalite -> 
            fonctionnaliteEnvironnementRepository
                .findByFonctionnaliteIdAndEnvironnementCode(
                    command.getIdFonctionnalite(),
                    command.getCodeEnvironnement()
                )
        )
        .map(config -> evaluerConfig(config, command))
        .orElseGet(() -> {
            log.debug("Fonctionnalité ou config introuvable : {}", 
                command.getIdFonctionnalite());
            return false;
        });
}

private Boolean evaluerConfig(
        FonctionnaliteEnvironnement config,
        EvaluateFonctionnaliteCommand command) {

    if (Boolean.FALSE.equals(config.getActif())) {
        log.debug("Fonctionnalité désactivée sur '{}'", 
            command.getCodeEnvironnement());
        return false;
    }

    if (Boolean.TRUE.equals(config.getActifPlanifie()) 
            && config.getPlanifieDe() != null) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(config.getPlanifieDe()) &&
               (config.getPlanifieA() == null || 
                now.isBefore(config.getPlanifieA()));
    }

    return true;
}
