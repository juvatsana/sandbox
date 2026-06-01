# Guide Renovate — Détection des changements d'APIs externes

## Qu'est-ce que Renovate ?

Renovate est un outil automatisé de gestion des dépendances. Il surveille les versions des librairies utilisées dans un projet et ouvre automatiquement des **Merge Requests** lorsqu'une nouvelle version est disponible.

Il supporte nativement : Maven, npm, Docker, et bien d'autres.

---

## Architecture mise en place chez JVO

### Le dépôt central `renovate-config`

Toute la configuration Renovate est **centralisée** dans un seul dépôt partagé :

```
socles/java/renovate-config
```

Les projets n'ont qu'un `renovate.json` minimaliste qui hérite de cette config :

```json
{
  "$schema": "https://docs.renovatebot.com/renovate-schema.json",
  "extends": [
    "local>mfe-solutionit/socles/java/renovate-config"
  ]
}
```

Cela permet de **maintenir les règles en un seul endroit** pour tous les projets Java de l'organisation.

---

## Configuration centrale (`config.js`)

### Comportement global

| Paramètre | Valeur | Description |
|---|---|---|
| `minimumReleaseAge` | 10 days | Attend 10 jours avant de proposer une MAJ (stabilité) |
| `prHourlyLimit` | 10 | Maximum 10 MR créées par heure |
| `labels` | MCO Continu | Toutes les MR Renovate sont taguées |
| `persistRepoData` | true | Mémorise l'état entre les runs |
| `baseBranch` | 1.11.x, 1.21.x | Branches surveillées |

### Registries configurés

```javascript
// Docker → Registry Harbor interne JVO
{
    matchDatasources: ["docker"],
    registryUrls: ["https://harbor.devops.jvo.fr/"],
    pinDigests: false
},

// Maven → Nexus interne JVO
{
    matchDatasources: ["maven"],
    registryUrls: [
        "https://nexus.dtni.jvo.fr/content/repositories/public-releases"
    ]
},

// npm → Nexus interne JVO
{
    matchDatasources: ["npm"],
    registryUrls: [
        "http://nexus.dtni.jvo.fr/content/groups/public-npm-releases/"
    ]
}
```

### Règles de mise à jour automatique

- Les **patches** (ex: 1.0.1 → 1.0.2) sont **auto-mergés** sauf versions `0.x.x`
- Les dépendances internes JVO ont un délai de stabilité de **0 jours** (mise à jour immédiate)
- Les dépendances externes ont un délai de **10 jours**

---

## Cas d'usage : Surveillance d'une API externe

### Contexte

Le dépôt `api-karma-mfe` consomme une API externe **Karma** via un `WebClient`. Tous les endpoints sont centralisés dans une enum `KarmaEndpoint`.

### Flux de détection

```
API Karma (externe)
        ↓
swagger-gen génère karma-kapia.yaml (+85 000 lignes)
        ↓  mvn -P karma-kapia
Client Java généré automatiquement (DTOs, interfaces)
        ↓
Publié comme artefact Maven sur Nexus
        ↓
Renovate détecte la nouvelle version sur Nexus
        ↓
Renovate ouvre une MR sur api-karma-mfe
```

### Ce que Renovate détecte

Renovate surveille la **version de l'artefact Maven** généré par `swagger-gen`. Dès qu'une nouvelle version est publiée sur Nexus, il propose automatiquement la mise à jour dans le `pom.xml` du projet consommateur.

```xml
<!-- pom.xml de api-karma-mfe -->
<dependency>
    <groupId>fr.jvo.api.kapia</groupId>
    <artifactId>karma</artifactId>
    <version>1.8.7</version>  ← Renovate surveille cette version
</dependency>
```

---

## Snapshots vs Releases

Il est **fortement recommandé** de surveiller les **releases uniquement**.

| | Releases | Snapshots |
|---|---|---|
| Stabilité | ✅ Stable et définitive | ⚠️ Écrasée à chaque build |
| Détection Renovate | ✅ Fonctionne parfaitement | ❌ Mal supporté |
| Déclenchement MR | ✅ Un vrai changement intentionnel | ❌ Faux positifs possibles |
| `minimumReleaseAge` | ✅ Fonctionne | ❌ Ne fonctionne pas |

---

## Ajouter un nouveau registry Maven

Si une société partenaire publie ses artefacts sur un Nexus différent, il suffit d'ajouter l'URL dans le tableau `registryUrls` du `config.js` central :

```javascript
{
    matchDatasources: ["maven"],
    registryUrls: [
        // Nexus JVO
        "https://nexus.dtni.jvo.fr/content/repositories/public-releases",
        // Nexus société partenaire — releases uniquement
        "https://nexus.[partenaire].fr/content/repositories/releases"
    ]
}
```

Renovate interroge les URLs **dans l'ordre** et s'arrête dès qu'il trouve la dépendance.

---

## Variables CI/CD requises

Pour que Renovate fonctionne dans un projet, configurer ces variables dans GitLab CI :

| Variable | Description |
|---|---|
| `CI_API_V4_URL` | URL de l'API GitLab JVO |
| `CICD_HARBOR_FACTORY_TOKEN_WRITE` | Token Harbor avec droits écriture |
| `CICD_HARBOR_FACTORY_ACCOUNT_READ` | Compte Harbor lecture |
| `CICD_HARBOR_FACTORY_TOKEN_READ` | Token Harbor lecture |

---

## Aller plus loin : détecter les breaking changes

Renovate signale **qu'une version a changé**, mais pas **ce qui a changé dans le contrat API**. Pour détecter les breaking changes sur les endpoints, combiner avec `oasdiff` dans la CI :

```yaml
# .gitlab-ci.yml
check-api-breaking-changes:
  script:
    - oasdiff breaking resources/openapi/karma-kapia-old.yaml resources/openapi/karma-kapia-new.yaml
  allow_failure: false
```

Cela bloquera la MR si un endpoint est supprimé ou modifié de façon incompatible.

---

## Ressources

- [Documentation Renovate](https://docs.renovatebot.com)
- [Configuration options](https://docs.renovatebot.com/configuration-options/)
- [oasdiff — OpenAPI diff tool](https://github.com/oasdiff/oasdiff)
- 
