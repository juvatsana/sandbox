# OpenFeature : Standard ouvert pour la gestion des feature flags

## Introduction

La gestion des **feature flags** (ou feature toggles) est devenue un pilier incontournable du développement logiciel moderne. Elle permet de découpler le déploiement du code de la mise en production d'une fonctionnalité, d'effectuer des tests A/B, de déployer progressivement des nouvelles fonctionnalités (canary releases), ou encore de désactiver rapidement une fonctionnalité défaillante en production sans redéploiement.

Cependant, le marché des feature flags est fragmenté : Unleash, LaunchDarkly, Flagsmith, Growthbook, Split.io — chaque solution dispose de son propre SDK propriétaire. Migrer d'un fournisseur à un autre implique de réécrire tous les appels dans le code applicatif.

**OpenFeature** est né pour résoudre ce problème. Il s'agit d'un standard ouvert, incubé au sein de la **Cloud Native Computing Foundation (CNCF)**, qui définit une API unifiée et agnostique au fournisseur pour l'évaluation des feature flags.

---

## 1. Qu'est-ce qu'OpenFeature ?

### 1.1 Présentation générale

OpenFeature est une **spécification** accompagnée de **SDK officiels** dans de nombreux langages (Java, Go, TypeScript/JavaScript, Python, .NET, PHP, Ruby…). Il définit un contrat standard entre :

- L'**application** qui consomme les feature flags
- Le **provider** (fournisseur) qui implémente la logique d'évaluation

L'idée centrale est simple : l'application n'appelle jamais directement l'API d'un fournisseur de feature flags. Elle passe systématiquement par l'API OpenFeature, qui délègue l'évaluation au provider configuré.

```
Application
    │
    ▼
OpenFeature SDK (API standard)
    │
    ▼
Provider (ex: Unleash, LaunchDarkly, Flagsmith, In-Memory…)
    │
    ▼
Feature Flag Backend
```

### 1.2 Gouvernance et maturité

OpenFeature est un projet de la **CNCF** (Cloud Native Computing Foundation), ce qui garantit sa neutralité vis-à-vis des fournisseurs commerciaux. Le projet est soutenu par des acteurs majeurs comme Microsoft, Dynatrace, Red Hat, LaunchDarkly, Flagsmith, et bien d'autres.

---

## 2. Concepts fondamentaux

### 2.1 Le Provider

Le **provider** est l'implémentation concrète qui sait comment évaluer un feature flag en interrogeant un backend spécifique. C'est la pièce qui change lorsqu'on migre d'un fournisseur à un autre.

Chaque fournisseur de feature flags publie généralement son propre provider OpenFeature. Il en existe également des providers "de base" :

| Provider | Description |
|---|---|
| `InMemoryProvider` | Provider par défaut pour les tests ou la configuration locale |
| `EnvVarProvider` | Évalue les flags depuis des variables d'environnement |
| `Unleash Provider` | Délègue à un serveur Unleash |
| `LaunchDarkly Provider` | Délègue à LaunchDarkly |
| `Flagsmith Provider` | Délègue à Flagsmith |
| `Flipt Provider` | Délègue à Flipt (open source) |

### 2.2 Le Client OpenFeature

Le **client** est l'objet que l'application utilise pour évaluer ses feature flags. Il est obtenu via l'API OpenFeature et encapsule toute la logique d'appel au provider.

### 2.3 L'Evaluation Context

Le **contexte d'évaluation** est un ensemble de données transmises lors de l'évaluation d'un flag. Il permet au provider de prendre des décisions contextuelles, par exemple :

- Activer un flag uniquement pour un utilisateur spécifique (`userId`)
- Cibler une région géographique (`country`)
- Cibler un groupe d'utilisateurs (`beta-testers`)

```json
{
  "targetingKey": "user-123",
  "email": "alice@example.com",
  "country": "FR",
  "plan": "premium"
}
```

### 2.4 Les Hooks

Les **hooks** sont des fonctions qui s'exécutent à différentes étapes du cycle d'évaluation d'un flag :

- `before` : avant l'évaluation
- `after` : après l'évaluation (résultat disponible)
- `error` : en cas d'erreur
- `finally` : toujours exécuté, quel que soit le résultat

Ils sont utilisés pour ajouter des comportements transversaux : logging, métriques, tracing, audit, etc.

### 2.5 Les types de flags

OpenFeature supporte quatre types de valeurs pour les feature flags :

| Type | Description | Exemple d'usage |
|---|---|---|
| `Boolean` | Vrai ou faux | Activer/désactiver une fonctionnalité |
| `String` | Chaîne de caractères | Choisir une variante d'interface |
| `Integer` | Entier | Définir une limite (ex: rate limit) |
| `Float` | Décimal | Définir un pourcentage |
| `Object` | Structure JSON | Configuration complexe |

---

## 3. Architecture et cycle d'évaluation

### 3.1 Vue d'ensemble

```
┌─────────────────────────────────────────────────────────┐
│                      Application                        │
│                                                         │
│   client.getBooleanValue("my-flag", false, context)     │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│                   OpenFeature SDK                       │
│                                                         │
│  1. Exécution des Hooks "before"                        │
│  2. Résolution du flag via le Provider                  │
│  3. Exécution des Hooks "after"                         │
│  4. Retour de la valeur résolue                         │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│                      Provider                           │
│  (ex: UnleashProvider, InMemoryProvider, etc.)          │
│                                                         │
│  - Interroge le backend de feature flags                │
│  - Applique les règles de ciblage                       │
│  - Retourne une ResolutionDetails                       │
└─────────────────────────────────────────────────────────┘
```

### 3.2 L'objet ResolutionDetails

Lors de l'évaluation, le provider ne retourne pas seulement la valeur du flag. Il retourne un objet `ResolutionDetails` qui contient :

- **`value`** : la valeur résolue du flag
- **`reason`** : la raison de la résolution (`STATIC`, `TARGETING_MATCH`, `DEFAULT`, `SPLIT`, `CACHED`, `DISABLED`, `UNKNOWN`, `ERROR`)
- **`variant`** : l'identifiant de la variante sélectionnée (ex: `"on"`, `"off"`, `"variant-A"`)
- **`errorCode`** : en cas d'erreur, le code d'erreur structuré
- **`errorMessage`** : le message d'erreur lisible

---

## 4. Intégration dans une application

### 4.1 Exemple en TypeScript / JavaScript (Node.js)

#### Installation

```bash
npm install @openfeature/server-sdk
# Installer le provider souhaité, par exemple :
npm install @openfeature/unleash-provider
```

#### Configuration du provider

```typescript
import { OpenFeature } from '@openfeature/server-sdk';
import { UnleashProvider } from '@openfeature/unleash-provider';

// Configuration initiale : à faire une seule fois au démarrage
await OpenFeature.setProviderAndWait(
  new UnleashProvider({
    unleashConfig: {
      url: 'https://unleash.mycompany.com/api',
      clientSecret: process.env.UNLEASH_CLIENT_SECRET,
      appName: 'my-application',
    },
  })
);
```

#### Obtention d'un client et évaluation de flags

```typescript
import { OpenFeature, EvaluationContext } from '@openfeature/server-sdk';

// Obtention du client (peut être fait dans chaque module)
const client = OpenFeature.getClient('my-service');

// Définition du contexte d'évaluation
const context: EvaluationContext = {
  targetingKey: 'user-456',
  email: 'bob@example.com',
  country: 'FR',
};

// Évaluation d'un flag booléen
const isNewCheckoutEnabled = await client.getBooleanValue(
  'new-checkout-flow',
  false,       // valeur par défaut si le flag ne peut pas être évalué
  context
);

if (isNewCheckoutEnabled) {
  // Afficher le nouveau tunnel de commande
} else {
  // Afficher l'ancien tunnel
}

// Évaluation d'un flag de type string (variante d'interface)
const theme = await client.getStringValue('ui-theme', 'default', context);

// Évaluation avec détails complets
const details = await client.getBooleanDetails('feature-x', false, context);
console.log(details.reason);  // ex: "TARGETING_MATCH"
console.log(details.variant); // ex: "enabled-for-beta"
```

### 4.2 Exemple en Java (Spring Boot)

#### Dépendance Maven

```xml
<dependency>
  <groupId>dev.openfeature</groupId>
  <artifactId>sdk</artifactId>
  <version>1.x.x</version>
</dependency>
<!-- Provider Unleash -->
<dependency>
  <groupId>dev.openfeature.contrib.providers</groupId>
  <artifactId>unleash</artifactId>
  <version>0.x.x</version>
</dependency>
```

#### Configuration Spring Boot

```java
@Configuration
public class OpenFeatureConfig {

    @Bean
    public OpenFeatureAPI openFeatureAPI() throws Exception {
        UnleashProvider unleashProvider = new UnleashProvider(
            UnleashProviderConfig.builder()
                .unleashConfig(
                    UnleashConfig.newBuilder()
                        .appName("my-spring-app")
                        .instanceId("instance-1")
                        .unleashAPI("https://unleash.mycompany.com/api")
                        .apiKey(System.getenv("UNLEASH_CLIENT_SECRET"))
                        .build()
                )
                .build()
        );

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProviderAndWait(unleashProvider);
        return api;
    }

    @Bean
    public Client openFeatureClient(OpenFeatureAPI api) {
        return api.getClient("my-spring-service");
    }
}
```

#### Utilisation dans un Service

```java
@Service
public class CheckoutService {

    private final Client featureClient;

    public CheckoutService(Client featureClient) {
        this.featureClient = featureClient;
    }

    public Order processOrder(User user, Cart cart) {
        MutableContext context = new MutableContext();
        context.setTargetingKey(user.getId());
        context.add("country", user.getCountry());
        context.add("plan", user.getPlan());

        boolean useNewPaymentFlow = featureClient.getBooleanValue(
            "new-payment-flow", false, context
        );

        if (useNewPaymentFlow) {
            return newPaymentProcessor.process(cart, user);
        } else {
            return legacyPaymentProcessor.process(cart, user);
        }
    }
}
```

### 4.3 Utilisation d'un provider In-Memory pour les tests

```typescript
import { OpenFeature, InMemoryProvider } from '@openfeature/server-sdk';

// Configuration des flags pour les tests
const FLAGS = {
  'new-checkout-flow': {
    disabled: false,
    variants: { on: true, off: false },
    defaultVariant: 'on',
  },
  'ui-theme': {
    disabled: false,
    variants: { dark: 'dark', light: 'light' },
    defaultVariant: 'dark',
  },
};

beforeAll(async () => {
  await OpenFeature.setProviderAndWait(new InMemoryProvider(FLAGS));
});

it('should display the new checkout UI', async () => {
  const client = OpenFeature.getClient();
  const isEnabled = await client.getBooleanValue('new-checkout-flow', false);
  expect(isEnabled).toBe(true);
});
```

---

## 5. Ajout de Hooks (comportements transversaux)

### 5.1 Hook de logging

```typescript
import { Hook, HookContext, EvaluationDetails } from '@openfeature/server-sdk';

const loggingHook: Hook = {
  before(hookContext: HookContext): void {
    console.log(`[OpenFeature] Evaluating flag: ${hookContext.flagKey}`);
  },
  after(hookContext: HookContext, details: EvaluationDetails<unknown>): void {
    console.log(
      `[OpenFeature] Flag: ${hookContext.flagKey} → ` +
      `value=${details.value}, reason=${details.reason}`
    );
  },
  error(hookContext: HookContext, error: unknown): void {
    console.error(`[OpenFeature] Error on flag ${hookContext.flagKey}:`, error);
  },
};

// Application du hook au niveau global (toutes les évaluations)
OpenFeature.addHooks(loggingHook);

// Ou au niveau d'un client spécifique
const client = OpenFeature.getClient();
client.addHooks(loggingHook);
```

### 5.2 Hook de métriques (exemple avec OpenTelemetry)

OpenFeature dispose d'un hook officiel pour OpenTelemetry :

```bash
npm install @openfeature/open-telemetry-hook
```

```typescript
import { OpenFeatureOpenTelemetryHook } from '@openfeature/open-telemetry-hook';

OpenFeature.addHooks(new OpenFeatureOpenTelemetryHook());
```

---

## 6. Gestion du cycle de vie du provider

### 6.1 États du provider

Un provider OpenFeature passe par plusieurs états :

| État | Description |
|---|---|
| `NOT_READY` | Le provider n'est pas encore initialisé |
| `READY` | Le provider est opérationnel |
| `ERROR` | Le provider a rencontré une erreur |
| `STALE` | Le provider n'est plus à jour (cache expiré) |
| `FATAL` | Erreur irrécupérable, la valeur par défaut sera toujours retournée |

### 6.2 Écoute des événements

```typescript
import { OpenFeature, ProviderEvents } from '@openfeature/server-sdk';

OpenFeature.addHandler(ProviderEvents.Ready, () => {
  console.log('Provider OpenFeature prêt !');
});

OpenFeature.addHandler(ProviderEvents.Error, (eventDetails) => {
  console.error('Erreur provider:', eventDetails?.message);
});

OpenFeature.addHandler(ProviderEvents.ConfigurationChanged, (eventDetails) => {
  console.log('Flags modifiés:', eventDetails?.flagsChanged);
  // Invalider les caches applicatifs si nécessaire
});
```

### 6.3 Fermeture propre

```typescript
// À appeler lors de l'arrêt de l'application
await OpenFeature.close();
```

---

## 7. Contexte d'évaluation global

Il est possible de définir un contexte d'évaluation global, qui sera automatiquement enrichi au contexte fourni lors de chaque évaluation :

```typescript
// Définir le contexte global au démarrage (ex: informations sur l'environnement)
OpenFeature.setContext({
  environment: process.env.NODE_ENV,
  region: process.env.AWS_REGION,
  appVersion: process.env.APP_VERSION,
});

// Le contexte de l'appel sera fusionné avec le contexte global
const value = await client.getBooleanValue('my-flag', false, {
  targetingKey: 'user-789',
  plan: 'enterprise',
});
// Le provider reçoit : { environment, region, appVersion, targetingKey, plan }
```

---

## 8. Bonnes pratiques

### 8.1 Définir des valeurs par défaut sûres

Toujours choisir une valeur par défaut qui correspond au comportement sûr ou attendu en cas d'indisponibilité du backend :

```typescript
// ✅ Bon : par défaut, la fonctionnalité est désactivée
const isExperimentalEnabled = await client.getBooleanValue(
  'experimental-feature', false  // false = safe default
);

// ⚠️ Attention : si le flag est un paywall, la valeur par défaut
// doit refléter la logique métier (pas forcément "false")
const isPremiumUnlocked = await client.getBooleanValue(
  'premium-access', false  // false = accès refusé par défaut si le service est down
);
```

### 8.2 Nommer les flags de manière cohérente

Adopter une convention de nommage claire pour les feature flags :

```
# Convention recommandée : kebab-case, préfixé par le domaine
feature-checkout-new-flow
feature-payment-stripe-v2
experiment-homepage-hero-ab
config-api-rate-limit
kill-switch-legacy-api
```

### 8.3 Éviter la logique métier complexe dans les flags

Les flags doivent être des points d'entrée simples. La logique complexe appartient à l'application :

```typescript
// ❌ Mauvais : logique métier dans l'évaluation du flag
const discount = await client.getFloatValue('discount-rate', 0, context);
if (discount > 0.5 && user.isPremium) { /* ... */ }

// ✅ Bon : le flag active/désactive une fonctionnalité ou retourne une config
const discountConfig = await client.getObjectValue('discount-config', {}, context);
// La logique d'application du discount reste dans le service métier
```

### 8.4 Supprimer les flags obsolètes

Les feature flags qui ont été déployés à 100% ou abandonnés doivent être supprimés du code. Accumuler des flags "morts" dans le code augmente la complexité et les risques.

---

## 9. Implémentation d'un Provider personnalisé

Si votre organisation utilise une solution de feature flags interne ou non supportée, vous pouvez implémenter votre propre provider.

```typescript
import {
  Provider,
  ResolutionDetails,
  EvaluationContext,
  ProviderMetadata,
  ProviderStatus,
} from '@openfeature/server-sdk';

class MyCustomProvider implements Provider {
  readonly metadata: ProviderMetadata = {
    name: 'MyCustomProvider',
  };

  status: ProviderStatus = ProviderStatus.NOT_READY;

  async initialize(): Promise<void> {
    // Connexion au backend de feature flags
    await this.flagBackendClient.connect();
    this.status = ProviderStatus.READY;
  }

  async resolveBooleanEvaluation(
    flagKey: string,
    defaultValue: boolean,
    context: EvaluationContext
  ): Promise<ResolutionDetails<boolean>> {
    const flag = await this.flagBackendClient.getFlag(flagKey, context);

    if (!flag) {
      return {
        value: defaultValue,
        reason: 'DEFAULT',
      };
    }

    return {
      value: flag.value as boolean,
      variant: flag.variant,
      reason: 'TARGETING_MATCH',
    };
  }

  // Implémenter également :
  // resolveStringEvaluation, resolveNumberEvaluation, resolveObjectEvaluation
  async resolveStringEvaluation(flagKey, defaultValue, context) { /* ... */ }
  async resolveNumberEvaluation(flagKey, defaultValue, context) { /* ... */ }
  async resolveObjectEvaluation(flagKey, defaultValue, context) { /* ... */ }

  async onClose(): Promise<void> {
    await this.flagBackendClient.disconnect();
  }
}
```

---

## 10. Résumé

OpenFeature apporte une couche d'abstraction standardisée sur la gestion des feature flags, avec plusieurs avantages clés :

- **Portabilité** : migrer de fournisseur ne nécessite de changer qu'une ligne de configuration (le provider)
- **Testabilité** : le `InMemoryProvider` permet de tester le comportement des flags sans dépendance externe
- **Extensibilité** : les hooks permettent d'ajouter logging, métriques et tracing de façon non intrusive
- **Interopérabilité** : la spécification couvre Java, Go, TypeScript, Python, .NET et d'autres langages
- **Gouvernance neutre** : projet CNCF, pas de lock-in sur un fournisseur commercial

En adoptant OpenFeature, les équipes bénéficient d'une API stable et connue quel que soit le backend de feature flags utilisé, tout en conservant la liberté de changer de fournisseur selon leurs besoins.
