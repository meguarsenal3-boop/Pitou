# Neferpitou (Forge 1.20.1)

Mod que transforma o seu player na Neferpitou. Este é o **esqueleto**: estado,
config, classificação de força de mob e comandos. As habilidades entram em cima
desta base nas próximas etapas.

## Como abrir e rodar

Este projeto não inclui o `gradle-wrapper.jar` (binário). Faça **uma** das opções:

**Opção A — colar num MDK novo (recomendado):**
1. Baixe o Forge MDK 1.20.1 (47.3.0) em files.minecraftforge.net.
2. Copie a pasta `gradle/`, `gradlew` e `gradlew.bat` do MDK para cá.
3. Substitua o `build.gradle`, `gradle.properties` e `settings.gradle` pelos daqui.
4. Copie a pasta `src/`.

**Opção B — gerar o wrapper:**
1. Tenha o Gradle instalado.
2. Rode `gradle wrapper --gradle-version 8.1.1` nesta pasta.

Depois:
```
./gradlew genIntellijRuns   # ou genEclipseRuns
./gradlew build             # gera o .jar em build/libs
```

## Comandos (precisam de permissão nível 2 / OP)

- `/pitou transform` — liga/desliga a transformação.
- `/pitou debug mode <IDENTITY|NEN|HATSU|TERPSICHORA>` — força o tier desbloqueado.
- `/pitou debug king <player>` — define o Rei.
- `/pitou debug king clear` — remove o Rei.
- `/pitou debug paranoia <0-4>` — seta a paranoia.
- `/pitou debug status` — mostra o estado atual.
- `/pitou debug reset` — zera tudo.
- `/pitou debug threat` — mostra o ThreatLevel/score da criatura que você está mirando.

## Onde mexer nos números

Tudo fica em `config/neferpitou-common.toml` (criado no primeiro boot).
Edite no bloco de notas, salve e reabra o mundo. Sem recompilar.

## Forçar a força de um mob específico (mods estranhos)

Crie tags de EntityType num datapack/no resources:
`data/neferpitou/tags/entity_types/force_apex.json` (ou force_strong/weak/trivial), ex:
```json
{ "replace": false, "values": ["saintsdragons:lightning_dragon"] }
```

## Estrutura

```
com.megu.neferpitou
├── Neferpitou            (entrada do mod)
├── config/PitouConfig    (todos os números, vira o .toml)
├── capability/           (estado da Pitou ligado ao player)
│   ├── PitouMode         (tiers: IDENTITY < NEN < HATSU < TERPSICHORA)
│   ├── PitouData         (o estado: transformada, Rei, paranoia, maestrias...)
│   ├── PitouDataProvider
│   └── PitouCapability   (attach + persistência + sync)
├── network/              (canal + packet de sync servidor->client)
├── threat/               (ThreatLevel + ThreatClassifier automático)
└── command/PitouCommands (transform + debug)
```
