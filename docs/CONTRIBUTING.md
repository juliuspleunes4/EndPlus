# Contributing to End+

Thank you for your interest in contributing to End+. This document covers everything you need to get the project running locally, understand the codebase, and submit a quality pull request.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Getting Started](#getting-started)
3. [Project Structure](#project-structure)
4. [Building and Running](#building-and-running)
5. [Code Standards](#code-standards)
6. [Mixin Guidelines](#mixin-guidelines)
7. [Adding Content](#adding-content)
8. [Submitting a Pull Request](#submitting-a-pull-request)
9. [Reporting Bugs](#reporting-bugs)

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java (JDK) | 21 or higher |
| Git | Any recent version |
| IDE | IntelliJ IDEA (recommended), Eclipse, or VS Code with Java extensions |

> **Note:** Make sure your IDE's JDK is set to Java 21. Fabric Loom will not generate run configurations correctly with an older JDK.

---

## Getting Started

### 1. Fork and clone

```bash
git clone https://github.com/juliuspleunes4/EndPlus.git
cd EndPlus
```

### 2. Generate IDE metadata

**IntelliJ IDEA:**
```bash
./gradlew genSources idea
```

**Eclipse:**
```bash
./gradlew genSources eclipse
```

Open the project in your IDE after this step. Loom will have downloaded Minecraft sources and mapped them to Yarn, which allows you to navigate vanilla code during development.

### 3. Verify the build

```bash
./gradlew build
```

A successful build outputs a JAR to `build/libs/`. If this fails, check that your JDK is set to 21 and that Gradle can reach the internet to download dependencies.

---

## Project Structure

```
src/main/java/com/endplus/
├── BetterEnd.java          ← Mod initializer (both sides)
├── config/                 ← EndPlusConfig: Gson-based config loader
├── effect/                 ← Custom status effects
├── entity/
│   ├── dragon/             ← Dragon phase logic and interfaces
│   └── projectile/         ← Projectile entities (e.g. VoidBeamEntity)
├── mixin/
│   └── entity/
│       ├── dragon/         ← Dragon-specific mixins
│       └── ...             ← Other entity mixins
└── registry/               ← ModBlocks, ModItems, ModEffects, ModEntities, ModCreativeTabs

src/main/resources/
├── fabric.mod.json
├── endplus.mixins.json     ← Mixin registration
├── assets/endplus/
│   ├── blockstates/
│   ├── lang/en_us.json
│   └── models/
└── data/endplus/
    ├── advancements/
    ├── loot_table/
    ├── recipe/
    └── tags/
```

All data-driven content (loot tables, advancements, recipes, tags) lives under `data/`. All client assets (models, blockstates, lang) live under `assets/`. Java logic lives under `src/main/java/`.

---

## Building and Running

| Command | Description |
|---------|-------------|
| `./gradlew build` | Compile and produce the mod JAR |
| `./gradlew runClient` | Launch a Fabric client with the mod loaded |
| `./gradlew runServer` | Launch a Fabric dedicated server |
| `./gradlew genSources` | Generate mapped Minecraft sources for navigation |

When running in dev, the `run/` directory acts as the game directory. Config, worlds, and logs go there. This directory is excluded from version control by `.gitignore`.

---

## Code Standards

### Production code only

All committed code must be production-ready. This means:

- No debug logging (`LOGGER.info(...)` calls for temporary debugging, console dumps, etc.)
- No commented-out code blocks
- No `TODO` or `FIXME` comments — open a GitHub Issue instead
- No placeholder implementations that silently do nothing without documenting why

### Comments

Write no comments by default. Add a comment only when the **why** is non-obvious — a hidden constraint, a Minecraft quirk, a specific bug workaround. If removing the comment wouldn't confuse a future reader, do not write it.

### Naming conventions

| Context | Convention | Example |
|---------|-----------|---------|
| Java classes | PascalCase | `VoidBeamEntity` |
| Java methods and fields | camelCase | `fireVoidBeam()` |
| Mixin-added fields/methods | `endplus_` prefix | `endplus_shieldActive` |
| Registry IDs | `snake_case` | `endplus:void_beam` |
| JSON data files | `snake_case` | `void_stone_bricks.json` |
| Constants | `UPPER_SNAKE_CASE` | `MOD_ID` |

### Mappings

This project uses **Yarn 1.21.1+build.3** mappings. Always use Yarn-mapped names in your code. Do not use Mojang or intermediary names. If you are unsure of a mapped name, use `./gradlew genSources` and navigate to the class in your IDE.

### No client-only code in shared sources

`src/main/java/` is the shared (both-sides) source set. Do not call client-only APIs (`MinecraftClient`, renderers, `RenderSystem`, etc.) from this source set. Client-only code belongs in a `client` entrypoint and source set.

---

## Mixin Guidelines

End+ uses Mixins for all modifications to vanilla Minecraft classes.

### Targeting

- Prefer `@Inject` at `@At("TAIL")` or `@At("HEAD")` unless you need a specific injection point
- Use `@ModifyVariable` to change method parameters without cancellation tricks
- Avoid `@Overwrite` entirely — it breaks compatibility with other mods
- Use `@Redirect` only when no other approach is feasible, and document why

### Fields and methods added via mixin

- All mixin-added fields and methods must be annotated with `@Unique`
- All mixin-added fields and methods must be prefixed with `endplus_` to avoid conflicts
- Interface methods implemented via mixin follow the same `endplus_` prefix

### Mixin registration

Mixins that target classes present on both sides (most entity/world classes) go in the `"mixins"` array of `endplus.mixins.json`. Mixins that target server-only classes (e.g. `EnderDragonFight`) go in `"server"`. Client-only mixins go in `"client"`.

### Safety

`defaultRequire: 1` is set in `endplus.mixins.json`, which means every `@Inject` or `@ModifyVariable` must match at least one bytecode location. If your injection target does not match, the game will crash on startup with a clear error. Fix the target rather than lowering `defaultRequire`.

---

## Adding Content

### New block or item

1. Add the block/item field in `ModBlocks.java` or `ModItems.java` and call `register()` from within the file. `initialize()` is called at startup — no further wiring needed.
2. Add a blockstate file to `assets/endplus/blockstates/`.
3. Add block and item model files to `assets/endplus/models/block/` and `/item/`.
4. Add a loot table to `data/endplus/loot_table/blocks/`.
5. Add translation keys to `assets/endplus/lang/en_us.json`.
6. Add the item to `ModCreativeTabs.java`.
7. Add mineable/harvest-level tags under `data/minecraft/tags/block/`.

### New entity

1. Register an `EntityType` in `ModEntities.java`.
2. Create the entity class under `com.endplus.entity/`.
3. Register a renderer in a client entrypoint (if the entity needs a visual appearance).
4. Add a loot table to `data/endplus/loot_table/entities/`.
5. Add spawn rules via biome modification (Phase E and beyond).

### New status effect

1. Extend `StatusEffect` in `com.endplus.effect/`.
2. Register via `Registry.registerReference` in `ModEffects.java`.
3. Add the translation key `effect.endplus.<name>` to `en_us.json`.

### New advancement

1. Add the JSON file under `data/endplus/advancements/`.
2. Use `"frame": "challenge"` — all End+ advancements use the challenge (purple) frame.
3. For advancements that require custom game logic, use `minecraft:impossible` as the criterion and grant the advancement in code via `ServerAdvancementTracker.grantCriterion()`.
4. Add title and description translation keys to `en_us.json`.

---

## Submitting a Pull Request

1. **Branch from `main`** — use a descriptive branch name: `feature/void-stalker-mob`, `fix/shield-damage-reduction`, etc.
2. **One concern per PR** — do not bundle unrelated changes. A mob implementation and a config refactor are separate PRs.
3. **Test your change in-game** — run `./gradlew runClient` or `runServer` and verify the feature works. Describe what you tested in the PR description.
4. **Check for regressions** — if your change touches the dragon fight or world generation, verify vanilla End behavior is unaffected when the relevant config options are disabled.
5. **No merge commits** — rebase onto `main` before opening the PR: `git rebase origin/main`.
6. **Fill in the PR template** — describe what changed, why, and how you tested it.

PRs that break the build, contain debug code, or do not follow the code standards above will be asked for revisions before merging.

---

## Reporting Bugs

Open an issue on [GitHub](https://github.com/juliuspleunes4/EndPlus/issues) and include:

- End+ version (visible with `/endplus help`)
- Minecraft version and Fabric Loader version
- A clear description of expected vs. actual behavior
- Steps to reproduce
- The latest log file from `run/logs/latest.log` (or your server's equivalent) if there is a crash or error

Feature requests are also welcome via GitHub Issues. Use the `enhancement` label and describe the intended behavior and how it fits the mod's design goals.
