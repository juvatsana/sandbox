@Override
public Boolean execute(final EvaluateFonctionnaliteCommand command) {
    // Validation
    Set<ConstraintViolation<EvaluateFonctionnaliteCommand>> violations = 
        validator.validate(command);
    if (!violations.isEmpty()) {
        throw new ConstraintViolationException(violations);
    }

    // 1. Récupérer la fonctionnalité
    final Optional<Fonctionnalite> fonctionnaliteOpt = 
        fonctionnaliteRepository.getFonctionnalite(command.getIdFonctionnalite());

    if (fonctionnaliteOpt.isEmpty()) {
        log.warn("Fonctionnalité inexistante : {}", command.getIdFonctionnalite());
        return false;
    }

    final Fonctionnalite fonctionnalite = fonctionnaliteOpt.get();

    // 2. Récupérer la config fonctionnalite_environnement
    final Optional<FonctionnaliteEnvironnement> configOpt = 
        fonctionnaliteRepository.getFonctionnaliteEnvironnement(
            command.getIdFonctionnalite(),
            command.getCodeEnvironnement()
        );

    if (configOpt.isEmpty()) {
        log.debug("Aucune config pour cette fonctionnalité/environnement");
        return false;
    }

    final FonctionnaliteEnvironnement config = configOpt.get();

    // 3. Vérifier si actif globalement
    if (!config.isActif()) {
        log.debug("Fonctionnalité '{}' désactivée sur '{}'",
            fonctionnalite.getNom(), command.getCodeEnvironnement());
        return false;
    }

    // 4. Vérifier si une planification est active
    if (config.isActifPlanifie() && config.getPlanifieDe() != null) {
        final LocalDateTime now = LocalDateTime.now();
        final boolean dansLaPlage = 
            now.isAfter(config.getPlanifieDe()) &&
            (config.getPlanifieA() == null || now.isBefore(config.getPlanifieA()));

        log.debug("Fonctionnalité '{}' planifiée, dans la plage : {}",
            fonctionnalite.getNom(), dansLaPlage);
        return dansLaPlage;
    }

    // 5. Actif sans planification
    return true;
}
