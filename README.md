<div align="center">

<br>

# ✦ End+ ✦

### A complete overhaul of the End dimension

*A tougher dragon. Hunters in the dark. Islands worth the trip.*

<br>

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=for-the-badge&logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/Fabric-0.16%2B-DBD0B4?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/Status-In%20development-9B59B6?style=for-the-badge)
![License](https://img.shields.io/badge/License-Source--Available-C0392B?style=for-the-badge)

<br>

[Features](#-features) ·
[Dragon fight](#-the-dragon-fight) ·
[Bestiary](#-bestiary) ·
[Roadmap](#-roadmap) ·
[Install](#-installation) ·
[Contribute](#-contributing)

<br>

</div>

---

## 🌌 About

The End has been the same for years. **End+** changes that.

It gives the Ender Dragon a real fight with four phases and minions that fight beside it, fills the
outer islands with new creatures that hunt in the dark, and adds the materials, gear, structures,
and a final boss that make reaching the End worth the effort. It works in singleplayer and on
dedicated servers.

> **Status:** End+ is under active development and has not had its first public release yet. The
> [roadmap](#-roadmap) shows what is already playable and what is still coming.

---

## ✨ Features

| | |
|---|---|
| 🐉 **Enhanced dragon fight** | 500 HP, four phases, a Void Shield, Void Beams, and waves of minions |
| 👹 **Five dragon minions** | Void Imp, Ender Phantom, Endrite Golem, Void Witch, Shadow Drake |
| 🌑 **Six new mobs** | Stalkers, Crawlers, Lurkers, Wraiths, an ancient guardian, and a parasite |
| 🧱 **New blocks and materials** | Umbral wood, Endrite and Void ores, crystal blocks, and more |
| 🏆 **Advancements** | A purple challenge line of advancements for every milestone |
| ⚙️ **Fully configurable** | Dragon health, wave timing, XP, and much more are in a config file |

Planned for later: six new End biomes, six structures including **The Citadel**, new weapons and
armor sets, six enchantments, and the final boss, **The Void Leviathan**. See the
[roadmap](#-roadmap).

---

## 🐉 The Dragon Fight

The dragon now has **500 HP** and changes its behavior as you wear it down.

| Phase | Health | What changes |
|:-----:|:------:|--------------|
| **1** | 100% to 75% | The familiar fight, slightly faster |
| **2** | 75% to 50% | Its breath lingers longer, and minion waves begin |
| **3** | 50% to 25% | Faster waves, and the dragon gains a **Void Shield** |
| **4** | 25% to 0% | Rage. Constant waves, and it fires the **Void Beam** |

**Void Shield.** The shield cuts all incoming damage in half. Destroy the two Void Crystals that
appear around the arena to break it. The crystals return after 90 seconds, and so does the shield.

**Void Beam.** A direct-fire laser that deals 12 damage, knocks you back, and applies **Void Rot**.

**Reward.** The dragon drops a **Dragon Heart**, **Void Scales**, and 20,000 XP. Kill it in under ten
minutes for the **Swift Slayer** advancement.

---

## 📖 Bestiary

### Dragon minions

Summoned in waves during the fight. They vanish when the dragon dies.

| Minion | HP | Appears | Known for |
|--------|:--:|:-------:|-----------|
| **Void Imp** | 20 | Phase 2+ | Swarms in groups and blinks to close the distance |
| **Ender Phantom** | 35 | Phase 2+ | Dive attacks that apply Levitation |
| **Endrite Golem** | 120 | Phase 3+ | Draws aggro and takes part of the damage meant for the dragon |
| **Void Witch** | 60 | Phase 3+ | Throws debuff potions, heals other minions, teleports away |
| **Shadow Drake** | 80 | Phase 4 | Dives at you, wrecks beds and respawn anchors, heals the dragon from a crystal |

### Creatures of the outer End

| Creature | HP | Home | What to expect |
|----------|:--:|------|----------------|
| **Void Stalker** | 45 | Void Wastes | A gaunt hunter that fades into the stone, leaps for burst damage, and calls its pack |
| **Prism Crawler** | 30 | Crystalline Fields | Climbs anything, fires bursts of crystal shards, and shatters crystals when hurt |
| **Shade Lurker** | 55 | Umbral Forest | Near-invisible in the dark, stalks you, strikes once, then pulls back. Walks through Umbral wood |
| **Cliff Wraith** | 40 | Skyreach Peaks | A skeletal flyer with a huge wingspan that hurls you back and can knock the item from your hand |
| **Ancient End Golem** | 150 | Sunken Citadel | A slow stone guardian that patrols its post and slams the ground in a Void Slam |
| **Ender Parasite** | 10 | Outer biomes | *Coming soon* |

---

## 🗺️ Roadmap

| Phase | Content | Status |
|:-----:|---------|:------:|
| **A** | Foundation: blocks, items, loot, recipes, config | ✅ Done |
| **B** | Dragon overhaul: 500 HP, phases, Void Shield, Void Beam | ✅ Done |
| **C** | Dragon minions: five mobs and wave summoning | ✅ Done |
| **D** | New mobs: five of six built, the Ender Parasite and biome spawning remain | 🔨 In progress |
| **E** | World generation: six new End biomes | ⏳ Planned |
| **F** | Structures, including The Citadel | ⏳ Planned |
| **G** | Weapons, armor sets, and utility items | ⏳ Planned |
| **H** | Enchantments | ⏳ Planned |
| **I** | The Void Leviathan boss | ⏳ Planned |
| **J** | Lore and advancements | ⏳ Planned |
| **K** | Polish, balance, and performance | ⏳ Planned |

The full design lives in [`docs/PLANNING.md`](docs/PLANNING.md).

---

## 📦 Installation

> The first release has not been published yet. These steps apply once it is.

**Requirements**

| | |
|---|---|
| Minecraft | **1.21.1** |
| Mod loader | [Fabric Loader](https://fabricmc.net/use/) **0.16.5** or newer |
| Required mod | [Fabric API](https://modrinth.com/mod/fabric-api) |
| Java | **21** or newer |

**Steps**

1. Install Fabric Loader for Minecraft 1.21.1.
2. Put **Fabric API** and the **End+** `.jar` in your `mods` folder.
3. Start the game and travel to the End.

End+ works in singleplayer and on dedicated servers. Players joining a server need the mod installed
too, as it adds new blocks, items, and creatures.

> 💡 **Back up your world** before adding any mod to it.

---

## ⌨️ Commands

| Command | Permission | Description |
|---------|:----------:|-------------|
| `/endplus help` | Everyone | Shows the info panel with features and version |
| `/endplus config` | Operator | Shows the active config values |

---

## ⚙️ Configuration

The config file is created on first start at `config/endplus.json`. A selection of what you can change:

```json
{
  "dragon": {
    "maxHealth": 500,
    "voidShieldReduction": 0.5,
    "voidShieldCrystalCount": 2,
    "minionWaveIntervalPhase2": 60,
    "minionWaveIntervalPhase3": 30,
    "xpReward": 20000,
    "fastKillThresholdSeconds": 600
  },
  "minions": {
    "enableVoidImp": true,
    "enableShadowDrake": true,
    "maxSimultaneousMinions": 20
  }
}
```

---

## 🛠️ Building from source

You need **JDK 21** and Git.

```bash
git clone https://github.com/juliuspleunes4/EndPlus.git
cd EndPlus
./gradlew build
```

The finished mod is written to `build/libs/`. To test it in a development client, run:

```bash
./gradlew runClient
```

Building from source is for your own use and for contributing. See the [license](#-license) before
sharing anything you build.

---

## 🤝 Contributing

Contributions are welcome: code, bug reports, ideas, translations, and playtesting.

- Read [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md) for the setup, code standards, and how to open a
  pull request.
- Follow the [Code of Conduct](docs/CODE_OF_CONDUCT.md).
- Found a bug or have an idea? [Open an issue](https://github.com/juliuspleunes4/EndPlus/issues).

By contributing you agree to the contribution terms in section 3 of the [license](LICENSE.md).

---

## 📜 License

End+ is **source-available**, not open source.

| You can | You cannot |
|---------|------------|
| ✅ Play it and run it on any server | ❌ Re-upload or redistribute it |
| ✅ Make videos, streams, and reviews, monetized or not | ❌ Publish forks, ports, or remakes of it |
| ✅ List it as a modpack dependency | ❌ Reuse its code or assets in other projects |
| ✅ Read the code and modify it for yourself | ❌ Sell it or charge for it |
| ✅ Contribute through pull requests | ❌ Claim it as your own work |

Read the full terms in [`LICENSE.md`](LICENSE.md). If you want to do something that is not listed, ask first.

---

<div align="center">

**Made by [Julius Pleunes](https://github.com/juliuspleunes4)**

*End+ is an unofficial mod and is not affiliated with Mojang Studios or Microsoft.
Minecraft is a trademark of Mojang Studios.*

</div>
