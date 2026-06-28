# End+ Mod — Planning Document

**Target:** Minecraft Java Edition 1.21.1  
**Java Version:** Java 21  
**Mod Loader:** Fabric (preferred for server-side compatibility)  
**Side:** Primarily server-side; some features require client-side rendering (particles, sounds)  
**Mod ID:** `endplus`

---

## Table of Contents

1. [Project Structure](#1-project-structure)
2. [Ender Dragon Overhaul](#2-ender-dragon-overhaul)
3. [Dragon Minions](#3-dragon-minions)
4. [New End Biomes & Regions](#4-new-end-biomes--regions)
5. [New Structures](#5-new-structures)
6. [New Mobs](#6-new-mobs)
7. [New Items & Weapons](#7-new-items--weapons)
8. [New Blocks & Materials](#8-new-blocks--materials)
9. [New Enchantments](#9-new-enchantments)
10. [New End Boss: The Void Leviathan](#10-new-end-boss-the-void-leviathan)
11. [Lore & Advancements](#11-lore--advancements)
12. [Command System](#12-command-system)
13. [Config System](#13-config-system)
14. [Implementation Order](#14-implementation-order)

---

## 1. Project Structure

```
EndPlus/
├── src/main/java/com/endplus/
│   ├── EndPlus.java                  # Mod initializer
│   ├── EndPlusServer.java            # Server-side initializer
│   ├── entity/
│   │   ├── dragon/                     # Dragon AI patches
│   │   ├── minion/                     # All minion entities
│   │   └── boss/                       # Void Leviathan
│   ├── world/
│   │   ├── biome/                      # New end biomes
│   │   ├── structure/                  # New structures
│   │   └── gen/                        # World gen hooks
│   ├── block/                          # New blocks
│   ├── item/                           # New items, weapons, armor
│   ├── enchantment/                    # New enchantments
│   ├── advancement/                    # Custom advancements
│   └── config/                         # Config handling
├── src/main/resources/
│   ├── fabric.mod.json
│   ├── endplus.mixins.json
│   ├── data/endplus/
│   │   ├── loot_tables/
│   │   ├── recipes/
│   │   ├── advancements/
│   │   └── worldgen/
│   └── assets/endplus/
│       ├── textures/
│       ├── models/
│       └── lang/
└── build.gradle
```

**Dependencies:**
- Fabric API
- Fabric Loader ≥ 0.16
- Mixin (bundled with Fabric)

---

## 2. Ender Dragon Overhaul

### 2.1 Increased Health
- Base HP raised from **200** → **500** health points (250 hearts)
- Configurable via `config.json` (`dragon_max_health`, default 500)

### 2.2 New Attack Phases
The dragon fight is split into **4 phases** based on remaining HP percentage:

| Phase | HP Threshold | Behavior Changes |
|-------|-------------|------------------|
| 1 | 100%–75% | Vanilla behavior + slightly faster |
| 2 | 75%–50% | Dragon breath lingers longer; starts summoning minions every 60s |
| 3 | 50%–25% | Dragon charges more frequently; summons minions every 30s; gains Void Shield |
| 4 | 25%–0% | Rage mode: double charge speed, constant minion waves, deadly breath |

### 2.3 Void Shield (Phase 3+)
- At the start of Phase 3 the dragon gains a **Void Shield** — an aura that reduces all incoming damage by 50%.
- The shield is broken by destroying **2 Void Crystals** that spawn mid-air around the arena when Phase 3 begins.
- Void Crystals look like End Crystals tinted purple; they regenerate after 90 seconds if not destroyed simultaneously.
- Shield re-activates 90 seconds after being broken, requiring players to destroy the crystals again.

### 2.4 Enhanced Dragon Breath
- Dragon breath clouds are **30% larger** and linger for **50% longer**.
- In Phase 4, the dragon occasionally fires a **Void Beam** — a direct-fire projectile (like a laser) that deals 12 damage, knocks back, and applies **Void Rot** (see enchantments/effects).

### 2.5 Improved AI
- Dragon will now **target specific players** who recently damaged it, rather than always attacking the nearest.
- Dragon performs a **Dive Bomb** attack: swoops to near-ground level attempting to knock players off the island.
- Dragon periodically **hovers** above the fountain for 5 seconds, projecting a downward breath AoE during which it cannot be damaged (forces players to dodge rather than spam damage).

### 2.6 Dragon Death & Loot
- Dragon drops **Dragon Heart** (new item, always 1 drop).
- Dragon drops **Void Scale** × 3–6.
- Experience raised from 12,000 → 20,000 XP.
- Killing the dragon in under **10 minutes** grants the "Swift Slayer" advancement and drops a bonus **Void Crown** (cosmetic helmet with enchantments).

---

## 3. Dragon Minions

All minions are summoned by the dragon during Phase 2–4. Minions despawn if the dragon dies.

### 3.1 Void Imp
- **Size:** Small (like a baby zombie)
- **HP:** 20 (10 hearts)
- **Damage:** 3 (1.5 hearts), fast attack speed
- **Behavior:** Swarms players in groups of 4–8; pathfinds aggressively; can teleport short distances (3–5 blocks) if the player runs
- **Summon Rate:** Phase 2+, 4–8 per wave
- **Drop:** Void Dust (0–2), Ender Pearl (0–1, 20% chance)

### 3.2 Ender Phantom
- **Size:** Medium, flying (like a phantom but End-themed)
- **HP:** 35 (17.5 hearts)
- **Damage:** 6 (3 hearts), sweeping dive attack
- **Behavior:** Flies above players, dives to strike, inflicts **Levitation** (2s) on hit — dangerous near the edge of the island
- **Summon Rate:** Phase 2+, 2–3 per wave
- **Drop:** Phantom Membrane (0–1), Void Scale (0–1)

### 3.3 Endrite Golem
- **Size:** Large (Iron Golem sized)
- **HP:** 120 (60 hearts)
- **Damage:** 10 (5 hearts) melee + knockback
- **Behavior:** Slow, tanky; draws aggro; absorbs 20% of damage dealt to the dragon while alive (dragon damage reduction stacks with Void Shield); cannot be knocked back; immune to arrows
- **Summon Rate:** Phase 3+, 1 per wave (max 2 alive at once)
- **Drop:** Endrite Shard × 2–4, Iron Ingot × 1–3

### 3.4 Void Witch
- **Size:** Same as Witch
- **HP:** 60 (30 hearts)
- **Damage:** Indirect — throws debuff potions
- **Behavior:** Stays at range; throws custom **Void Potion** (applies Blindness 5s + Weakness 10s + Void Rot 5s); heals nearby minions for 4 HP every 3 seconds; teleports away if a player gets within 5 blocks
- **Summon Rate:** Phase 3+, 1 per wave
- **Drop:** Glass Bottle × 1–3, Void Dust × 1–2, Glowstone Dust × 0–2

### 3.5 Shadow Drake (Phase 4 only)
- **Size:** Small dragon (baby ender dragon aesthetic)
- **HP:** 80 (40 hearts)
- **Damage:** 8 melee + breath attack (deals 2/tick for 3s)
- **Behavior:** Flies; uses a weaker version of dragon breath; prioritizes destroying placed blocks (beds, respawn anchors) in the arena; can perch on End Crystals to rapidly heal them (heals 5 HP/s to the crystal)
- **Summon Rate:** Phase 4 only, 1–2 per wave
- **Drop:** Shadow Scale × 1–2, Dragon Heart Fragment × 1 (50% chance)

---

## 4. New End Biomes & Regions

All biomes generate in the **Outer End Islands** (beyond 1000 blocks from origin). The main island and its immediate surroundings are unchanged.

### 4.1 Void Wastes
- **Description:** Barren islands of cracked End Stone; no vegetation; extreme darkness; occasional void geysers
- **Generation:** Most common outer biome (~40% of outer end)
- **Features:**
  - **Void Geysers:** Columns that periodically shoot players upward (useful for travel, dangerous near edges)
  - Cracked End Stone (new block) as ground cover
  - Scattered Void Crystal outcrops (decorative, not harvestable without Silk Touch)
  - Void Stalker mob spawns here (see Mobs)

### 4.2 Crystalline Fields
- **Description:** Dense forests of giant End Crystal spires; beautiful but dangerous due to crystal explosions
- **Generation:** ~20% of outer end
- **Features:**
  - Towering crystal pillars (3–20 blocks tall, various colors: white, purple, cyan)
  - Crystal Shards drop when crystals are broken (resource)
  - Crystal Bloom flower (decorative, emits soft light)
  - Prism Crawler mob spawns here (see Mobs)
  - **Crystal Resonance:** Being near large crystal clusters gives players a random buff (Speed, Jump Boost, or Night Vision) for 30s every 2 minutes

### 4.3 Umbral Forest
- **Description:** Towering twisted trees made of Umbral Wood (new block); dark canopy; bioluminescent ground flora
- **Generation:** ~15% of outer end
- **Features:**
  - Umbral Trees: 10–30 block tall trees; Umbral Wood + Umbral Leaves
  - Gloom Spores (particle effect, decorative)
  - Voidmoss ground cover (climbable like vines)
  - Loot: Umbral Wood for building; rare **Heartwood Core** item in hollow tree trunks
  - Shade Lurker mob spawns here (see Mobs)

### 4.4 Skyreach Peaks
- **Description:** Towering End Stone mountain ranges; high-altitude floating platforms connected by natural bridges
- **Generation:** ~10% of outer end
- **Features:**
  - Procedurally generated mountain spires up to 60 blocks tall
  - Exposed Void Ore veins (new ore)
  - Aerie structures (see Structures)
  - Wind Updrafts: invisible columns that grant Levitation when stepped on (safe travel mechanic)
  - Cliff Wraith mob spawns here (see Mobs)

### 4.5 The Sunken Citadel Region
- **Description:** Ruined architecture half-submerged in the void; chunks of what appear to be an ancient civilization
- **Generation:** ~10% of outer end; rare, large
- **Features:**
  - Ruined Purpur architecture with new **Void-Touched Purpur** variant (darker, cracked)
  - Void Pools (liquid void — decorative, deadly on contact: instant death)
  - Rich loot in partially buried chests
  - Ancient End Golem mob spawns here (see Mobs)
  - Gateway to **The Citadel** structure (see Structures)

### 4.6 The Nether Scar (Micro-biome)
- **Description:** A rare anomaly where Nether-like terrain bleeds into the End; small patches of Netherrack, Soul Sand, and Crimson/Warped fungi growing from End Stone
- **Generation:** ~5% of outer end; very small patches
- **Features:**
  - Mixed Nether/End blocks
  - Basalt pillars rising from the void
  - Nether Gold Ore occasionally appears
  - Unique **Scar Chest** loot table with Nether + End hybrid items

---

## 5. New Structures

### 5.1 Void Spire
- **Location:** Void Wastes biome
- **Size:** Medium (20×20×40)
- **Description:** A tall obsidian-and-End-Stone tower with a glowing purple top
- **Loot:** Void Chest containing Void Dust, Endrite Shards, occasionally a Void Weapon
- **Mob Spawners:** Void Imp spawner at the base; Void Witch at the top
- **Puzzle:** To unlock the top chest, players must light 4 Void Runes at the base of the tower (right-clicking with a Dragon Heart Fragment)

### 5.2 Crystal Sanctum
- **Location:** Crystalline Fields biome
- **Size:** Large (40×40×20)
- **Description:** An underground (below island level) domed chamber filled with crystal columns and a central altar
- **Loot:** Crystal Altar chest — high-quality gear, Crystal Shard bundles, crafting reagents
- **Mechanic:** The altar can be interacted with to craft **Prism Gear** (see Items) using Crystal Shards + Void Dust
- **Boss:** Sanctum Guardian miniboss (elite Prism Crawler variant, 200 HP)

### 5.3 Umbral Ruin
- **Location:** Umbral Forest biome
- **Size:** Medium (30×30×15)
- **Description:** The remains of a structure grown over by Umbral Trees; large roots weave through broken walls
- **Loot:** Buried chests with Umbral Wood, Heartwood Cores, rare enchanted books (Umbral Sight, Soul Bind)
- **Secret:** A hidden basement room accessible by breaking Voidmoss reveals a **Lore Tablet** (readable item describing pre-dragon End civilization)

### 5.4 Skyreach Aerie
- **Location:** Skyreach Peaks biome
- **Size:** Medium — perched on mountain peak
- **Description:** Nest-like structure of End Stone and Purpur; home to Shadow Drake eggs (decorative but can be hatched, see Items)
- **Loot:** Aerie Chest: Shadow Scales, Void Ore, occasionally Shadow Drake Egg (rare)
- **Hazard:** Wind Burst trap — stepping on certain pressure plates fires players sideways off the peak

### 5.5 The Citadel (Major Structure)
- **Location:** Sunken Citadel Region; 1 per world
- **Size:** Massive (80×80×50 above void)
- **Description:** The final dungeon of the mod. A fully realized ancient End city that predates the Ender Dragon. Multiple floors, mob spawners, puzzles, and a boss arena.
- **Floors:**
  - **Floor 1 (Entry Hall):** Void Stalkers, trapped chests, environmental hazards (Void Pools)
  - **Floor 2 (Library):** Ancient End Golems; Lore Tablets detailing the history of the End civilization; Enchantment Tables powered by Void Energy
  - **Floor 3 (Armory):** Elite Endrite Golems; Void Weapon racks (loot); crafting station for Void-tier gear
  - **Floor 4 (Throne Room / Boss Arena):** Spawns **The Void Leviathan** (see Boss section)
- **Loot:** Best-in-mod gear; exclusive **Void Sovereign** armor set; Leviathan Trophy (decorative)
- **One-time:** The Citadel can only be completed once per world (boss does not respawn); subsequent visits yield diminishing loot

### 5.6 End Shipwreck
- **Location:** Any outer biome; floating in void between islands
- **Size:** Small (ship hull ~25 blocks long)
- **Description:** A wrecked End-Stone-and-Purpur sailing vessel; hovering in void via levitation blocks underneath
- **Loot:** Random mid-tier loot; occasionally a **Navigation Crystal** (points toward the nearest Citadel)

---

## 6. New Mobs

### 6.1 Void Stalker
- **Biome:** Void Wastes
- **HP:** 45 (22.5 hearts)
- **Damage:** 7 (3.5 hearts)
- **Behavior:** Camouflages as End Stone while stationary (invisible unless within 8 blocks or hit); leaps at players for burst damage; pack-hunts (alerts nearby Void Stalkers when it attacks)
- **Drop:** Void Dust (1–3), Camouflage Membrane (rare, crafting ingredient)

### 6.2 Prism Crawler
- **Biome:** Crystalline Fields
- **HP:** 30 (15 hearts)
- **Damage:** 5 (2.5 hearts) + Crystal Shards projectile (2 damage, 3 shards per burst)
- **Behavior:** Spider-like; walks on crystal surfaces; fires shard bursts at range; cracks nearby crystals when damaged (creates environmental hazard)
- **Drop:** Crystal Shard (1–3), Spider Eye (0–1)

### 6.3 Shade Lurker
- **Biome:** Umbral Forest
- **HP:** 55 (27.5 hearts)
- **Damage:** 8 (4 hearts) + inflicts **Gloom** (Slowness II + Blindness, 3s)
- **Behavior:** Invisible in shadow (light level < 4); visible in light; stalks players from a distance before lunging; can phase through Umbral Wood blocks (ignores Umbral Wood walls)
- **Drop:** Gloom Essence (1–2), Umbral Leaf (0–2)

### 6.4 Cliff Wraith
- **Biome:** Skyreach Peaks
- **HP:** 40 (20 hearts)
- **Damage:** 6 (3 hearts) + pushes target backward 5 blocks
- **Behavior:** Flying; swoops from above; extremely fast; immune to fall damage; attacks in pairs; drops a player's held item (10% chance on hit, item lands 3–8 blocks away)
- **Drop:** Wraith Feather (1–2), Bone (0–2)

### 6.5 Ancient End Golem
- **Biome:** Sunken Citadel Region
- **HP:** 150 (75 hearts)
- **Damage:** 12 (6 hearts) melee + Void Slam (AoE, 8 damage, 5-block radius, 30s cooldown)
- **Behavior:** Patrols structures; does not pursue beyond 20 blocks; immune to arrows; slow but high damage; drops its own HP into a Void Stone shard when killed (used in crafting)
- **Drop:** Void Stone (2–4), Ancient Rune Fragment (0–1, for crafting)

### 6.6 Ender Parasite (Passive/Neutral)
- **Biome:** Any outer biome
- **HP:** 10 (5 hearts)
- **Damage:** None normally; if player gets within 2 blocks, latches on and drains 1 HP/s for 5s before detaching
- **Behavior:** Clings to walls and ceilings; drops from ceiling onto unsuspecting players; can latch on and be shaken off by sprinting for 2 seconds
- **Drop:** Parasite Fluid (0–1, brewing ingredient for resistance potions)

---

## 7. New Items & Weapons

### 7.1 Materials & Reagents

| Item | Source | Use |
|------|--------|-----|
| Dragon Heart | Ender Dragon drop (always) | Crafting Void-tier gear; powering Void Runes |
| Void Scale | Dragon + Void Stalker drop | Void armor crafting |
| Void Dust | Minion drops, Void Wastes | Crafting reagent; explosive traps |
| Endrite Shard | Endrite Golem drop | Heavy armor/weapons |
| Crystal Shard | Crystalline Fields, Prism Crawler | Prism Gear crafting; building |
| Shadow Scale | Shadow Drake drop | Shadow armor crafting |
| Heartwood Core | Umbral Ruin loot | Bow crafting; wand handles |
| Gloom Essence | Shade Lurker drop | Brewing (Gloom Potion) |
| Wraith Feather | Cliff Wraith drop | Elytra upgrade material |
| Void Stone | Ancient End Golem drop | Citadel crafting; Leviathan gear |
| Ancient Rune Fragment | End Golem, Citadel loot | Unlocking advanced recipes |
| Navigation Crystal | End Shipwreck loot | Points to nearest Citadel |
| Dragon Heart Fragment | Shadow Drake drop (50%) | Recharging Void Shield; portal fuel |

### 7.2 Weapons

#### Void Blade (Sword)
- **Stats:** 9 attack damage, 1.8 attack speed
- **Effect:** On hit, applies **Void Rot** (2 HP/s for 5s); 15% chance to teleport the target 5–10 blocks away (disorienting, not lethal)
- **Craft:** Dragon Heart + Void Scale × 4 + Endrite Shard × 2 (Smithing Table)
- **Durability:** 2500

#### Prism Lance (Spear — functions as trident)
- **Stats:** 10 melee damage, 8 ranged damage
- **Effect:** When thrown, creates a burst of Crystal Shards on impact (hits all mobs within 3 blocks for 4 damage each)
- **Craft:** Crystal Shard × 6 + Heartwood Core × 1 + Void Dust × 2
- **Durability:** 1800

#### Shadow Bow
- **Stats:** Standard bow draw; arrows deal +4 bonus damage to End mobs
- **Effect:** Arrows fired become invisible (silent, no trail); with fully drawn bow, fires a **Shadow Bolt** that passes through one entity before stopping
- **Craft:** Shadow Scale × 3 + Heartwood Core × 2 + String × 3
- **Durability:** 800

#### Endrite Maul (Two-handed — functions as axe)
- **Stats:** 13 attack damage, 0.8 attack speed
- **Effect:** Knockback is tripled; AoE ground slam (right-click to charge, releases 6-block radius shockwave); disables shields for 5s
- **Craft:** Endrite Shard × 5 + Void Scale × 2 + Blaze Rod × 1 (handle)
- **Durability:** 3000

#### Void Wand (Ranged Magic — functions as crossbow)
- **Stats:** Fires Void Bolts (6 damage, inflict Blindness 3s)
- **Effect:** Charged shot (hold use button 3s) fires a seeking Void Bolt that tracks the nearest mob within 20 blocks
- **Craft:** Dragon Heart Fragment × 2 + Heartwood Core × 1 + Crystal Shard × 4
- **Durability:** 600 charges

### 7.3 Armor Sets

#### Void Sovereign Set (Full Set)
- **Source:** Citadel loot / Leviathan drops
- **Stats:** Better than Netherite; Void Sovereign Helmet, Chestplate, Leggings, Boots
- **Set Bonus (2 pieces):** Void Step — teleport 5 blocks in look direction (15s cooldown, right-click while crouching)
- **Set Bonus (4 pieces):** Void Form — once per minute, nullify all damage from a single hit; brief invulnerability frames (0.5s)
- **Toughness:** 4 per piece; Knockback Resistance: 0.2 per piece

#### Endrite Heavy Set
- **Source:** Crafted with Endrite Shards
- **Stats:** Slightly below Netherite in protection; high toughness
- **Set Bonus (4 pieces):** Endrite Resolve — when below 20% HP, gain Resistance II for 10s (3-minute cooldown)
- **Special:** Immune to knockback while crouching

#### Shadow Set (Light Armor)
- **Source:** Crafted with Shadow Scales
- **Stats:** Low protection but high mobility bonuses
- **Set Bonus (2 pieces):** +20% movement speed in End dimension
- **Set Bonus (4 pieces):** Become partially invisible in shadow (light level < 7); mobs lose aggro at 12 blocks instead of 6

#### Prism Set (Balanced)
- **Source:** Crystal Sanctum altar crafting
- **Stats:** Iron-tier protection; magical properties
- **Set Bonus (4 pieces):** Prismatic Shield — absorbs the first 8 damage of any hit, then recharges after 20s

### 7.4 Tools & Utility Items

| Item | Function |
|------|----------|
| Void Pickaxe | Mines End Stone and Void Ore 3× faster; has a 10% chance to auto-smelt mined ores |
| Ender Compass | Points toward the nearest Citadel (craftable with Navigation Crystal) |
| Void Pouch | Stores up to 27 items; if the player dies in the End, the pouch is preserved and dropped intact |
| Dragon Scale Map | Reveals a large portion of the End's island map in a 2000-block radius |
| Shadow Drake Egg | Decorative; can be "hatched" by placing near an End Crystal for 10 minutes — produces a Shadow Drake pet (passive, follows player, does not fight) |
| Heartwood Staff | Right-click to summon a temporary Umbral Tree for 60s (provides cover/climbing in the End) |
| Void Lens | Equip in offhand; reveals hidden Void Stalkers and invisible mobs within 16 blocks |

---

## 8. New Blocks & Materials

### 8.1 Natural Blocks

| Block | Generation | Properties |
|-------|-----------|------------|
| Cracked End Stone | Void Wastes | Weaker End Stone; breaks into Gravel on explosion |
| Void-Touched Purpur | Sunken Citadel Region | Purpur variant; slightly darker; same blast resistance |
| Umbral Wood (Log/Plank/Slab/Stair) | Umbral Forest | Buildable wood; naturally fire-resistant; dark purple-black |
| Umbral Leaves | Umbral Forest trees | Glowing edges; no sapling; decays without log |
| Voidmoss | Umbral Forest floor | Climbable like vines; dark blue-green |
| Crystal Spire | Crystalline Fields | Decorative; emits light (level 8); cannot be picked up without Silk Touch |
| Endrite Ore | Skyreach Peaks deep | Mined for Endrite Shards; requires Diamond+ pickaxe |
| Void Ore | All outer biomes, deep | Mined for Void Dust and Void Stones; requires Iron+ pickaxe |
| Void Pool (Liquid) | Sunken Citadel Region | Decorative liquid; instant kill on contact; cannot be contained in bucket |

### 8.2 Crafted/Processed Blocks

| Block | Source | Use |
|-------|--------|-----|
| Endrite Block | 9× Endrite Shards | Decorative; high blast resistance |
| Void Stone Bricks | Void Stone × 4 | Citadel-aesthetic building block |
| Crystal Glass | Crystal Shard × 4 | Transparent colored glass; light-emitting |
| Umbral Planks | Umbral Wood logs | Building; crafting tables, etc. |
| Void Lamp | Void Dust + Crystal Shard + End Stone | Light-emitting block; level 15 |
| Void Rune Block | Ancient Rune Fragment × 4 | Activatable puzzle element; used in Void Spire |

---

## 9. New Enchantments

### 9.1 Weapon Enchantments

| Enchantment | Applies To | Effect | Max Level |
|-------------|-----------|--------|-----------|
| Void Strike | Swords, Axes | +15%/level bonus damage to End dimension mobs | III |
| Soul Bind | Swords | Killed mobs drop +50%/level XP; 10%/level chance to refill saturation | II |
| Phase Cut | Swords | 5%/level chance to ignore armor entirely on hit | II |
| Umbral Sight | Helmets | Night vision effect in darkness; +1 level = works in all light levels | II |
| Void Walk | Boots | Negate void fall damage (I = survive 1 fall, II = permanent immunity) | II |
| Crystal Aegis | Chestplates | On taking >8 damage in one hit, create a crystal barrier absorbing 10 damage (60s cooldown per level reduction) | III |

### 9.2 Incompatibilities
- Void Strike is incompatible with Sharpness and Smite
- Phase Cut is incompatible with Looting
- Void Walk is incompatible with Feather Falling

### 9.3 Obtaining Enchantments
- Void Strike, Void Walk: Enchantment Table (rare); Citadel loot
- Soul Bind: Umbral Ruin loot only (not obtainable via enchanting table)
- Phase Cut: Void Spire top chest; very rare Citadel loot
- Umbral Sight: Umbral Ruin loot; trading with future Wandering Trader in End (post-dragon)
- Crystal Aegis: Crystal Sanctum altar crafting

---

## 10. New End Boss: The Void Leviathan

The Void Leviathan is a massive serpentine creature residing in The Citadel's throne room. It is the final challenge of the mod and drops the best loot.

### 10.1 Stats
- **HP:** 800 (400 hearts)
- **Size:** 8 blocks wide, 30 blocks long (multi-segment entity like a worm)
- **Spawn:** Upon first player entering the Citadel Throne Room

### 10.2 Attack Patterns

| Attack | Description | Cooldown |
|--------|-------------|----------|
| Tail Whip | Rear segment sweeps in arc, 12 damage + 8-block knockback | 8s |
| Void Surge | Fires 3 tracking Void Bolts in a spread, each 8 damage + Void Rot | 10s |
| Coil Strike | Leviathan wraps around a section of the arena, crushing anyone inside for 15 damage/s for 3s | 20s |
| Summon Parasites | Spawns 6 Ender Parasites per player | 30s |
| Void Scream | AoE 15-block scream that applies Blindness + Weakness for 5s | 45s |
| Dive (Phase 2) | At 50% HP, the Leviathan disappears into a void portal then emerges beneath a random player, dealing 20 damage | 25s |

### 10.3 Phases
- **Phase 1 (100–50%):** All attacks above except Dive
- **Phase 2 (50–0%):** All attacks; Dive enabled; movement speed +30%; Void Surge fires 5 bolts instead of 3; ambient Void Pools rise from the floor in random locations (environmental hazard)

### 10.4 Weak Points
- The Leviathan has 5 glowing **Void Nodes** along its back; each Node absorbs 40% of incoming damage. Players must destroy Nodes (each has 80 HP) to deal full damage to the body.
- Nodes regenerate at 10% HP (8 HP per 10s); players must destroy all 5 within 30s of destroying the first to permanently remove them.

### 10.5 Drops
- **Void Sovereign Armor Set** (full set, 1 per kill)
- **Leviathan Heart** × 1 — used to craft the Void Sovereign upgrade (Smithing Table)
- **Void Stone** × 8–12
- **Ancient Rune Fragment** × 4–6
- **Leviathan Trophy** (decorative head-like block for display)
- **40,000 XP**

---

## 11. Lore & Advancements

### 11.1 Lore Concept
The End was once a thriving civilization of beings called the **Aetherans** — a society that mastered void energy. The Ender Dragon was their creation: a guardian meant to protect the dimension. Something went wrong, the dragon turned on its creators, and the civilization collapsed. The Endermen are the descendants of the Aetherans, trapped in a form of void-cursed servitude. The Void Leviathan was the Aetherans' ultimate weapon, never activated — sealed in the Citadel until a player discovers it.

### 11.2 Lore Delivery
- **Lore Tablets:** Found in Umbral Ruins and Citadel Library; 8 total; each a readable item with 1–3 paragraphs of lore
- **Ender Codex:** Crafted item (Book + Dragon Heart Fragment) that auto-collects lore from tablets the player has read; shows collected entries in a custom screen

### 11.3 Advancements

All End+ advancements use `"frame": "challenge"` — this renders them in **purple** in the chat/console when triggered, distinguishing them clearly from vanilla advancements.

Advancements requiring non-standard game state (simultaneous mobs, multi-stage events) use `minecraft:impossible` as their criterion placeholder; they are granted via code using `AdvancementProgress` when the relevant game logic fires.

#### Dragon

| ID | Title | Trigger | Vanilla Criterion |
|----|-------|---------|-------------------|
| `dragon/dragonslayer` | Dragonslayer | Kill the Ender Dragon | `player_killed_entity` |
| `dragon/swift_slayer` | Swift Slayer | Kill dragon in under 10 min | Code-granted (`impossible`) |
| `dragon/phase_breaker` | Phase Breaker | Break Void Shield twice in one fight | Code-granted (`impossible`) |
| `dragon/void_sovereign` | Void Sovereign | Kill the Void Leviathan | `player_killed_entity` |

#### Mob Kills

| ID | Title | Trigger | Vanilla Criterion |
|----|-------|---------|-------------------|
| `mobs/void_stalker_slayer` | Gone in a Blink | Kill a Void Stalker | `player_killed_entity` |
| `mobs/prism_crawler_slayer` | Crystal Breaker | Kill a Prism Crawler | `player_killed_entity` |
| `mobs/shade_lurker_slayer` | Into the Light | Kill a Shade Lurker | `player_killed_entity` |
| `mobs/cliff_wraith_slayer` | Grounded | Kill a Cliff Wraith | `player_killed_entity` |
| `mobs/ancient_golem_slayer` | Old Bones | Kill an Ancient End Golem | `player_killed_entity` |
| `mobs/parasite_survivor` | Parasite Problems | Have 3 Ender Parasites latch on simultaneously | Code-granted (`impossible`) |
| `mobs/bestiary_complete` | Void Bestiary | Kill at least one of every new mob | Code-granted (`impossible`) |

#### Exploration

| ID | Title | Trigger | Vanilla Criterion |
|----|-------|---------|-------------------|
| `exploration/void_wastes` | Barren Horizons | Enter the Void Wastes biome | `location` |
| `exploration/crystalline_fields` | Crystal Garden | Enter the Crystalline Fields biome | `location` |
| `exploration/umbral_forest` | Under the Canopy | Enter the Umbral Forest biome | `location` |
| `exploration/skyreach_peaks` | Summit Seeker | Enter the Skyreach Peaks biome | `location` |
| `exploration/sunken_citadel` | Lost Civilization | Enter the Sunken Citadel Region | `location` |
| `exploration/biome_walker` | Void Walker | Visit all 6 new End biomes | All five above (AND-required) |

#### Items & Crafting

| ID | Title | Trigger | Vanilla Criterion |
|----|-------|---------|-------------------|
| `items/dragon_heart_obtained` | Heart of the Beast | Obtain a Dragon Heart | `inventory_changed` |
| `items/first_scales` | Scaled Up | Obtain Void Scales | `inventory_changed` |
| `items/endrite_miner` | Endrite Prospector | Mine Endrite Ore | `inventory_changed` |
| `items/void_miner` | Void Dust Collector | Mine Void Ore | `inventory_changed` |

#### Lore

| ID | Title | Trigger | Vanilla Criterion |
|----|-------|---------|-------------------|
| `lore/archivist` | Archivist | Collect all 8 Lore Tablets | Code-granted (`impossible`) |

---

## 12. Command System

### `/endplus help`
Outputs a formatted info panel to the executing player's chat. No permission level required.

**Output design:**
```
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
          ✦  End+  ✦
    An End Dimension Overhaul Mod
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
 Author   Julius Pleunes
 Version  1.0.0
 GitHub   https://github.com/juliuspleunes4/EndPlus
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
 Features
  ✦ Enhanced Dragon Fight  (500 HP, 4 phases, minions)
  ✦ 5 Dragon Minions
  ✦ 6 New End Biomes
  ✦ 6 New Structures incl. The Citadel
  ✦ 6 New Mobs
  ✦ New Weapons, Armor & Materials
  ✦ The Void Leviathan  (Final Boss — 800 HP)
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
 Commands
  /endplus help    Show this menu
  /endplus config  View active config values  [OP]
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
```

**Color scheme:**
- Dividers `▬▬▬`: `DARK_PURPLE`
- Title `End+`: `LIGHT_PURPLE` + Bold
- `✦` bullets: `LIGHT_PURPLE`
- Labels (Author, Version, GitHub, section headers): `GOLD` + Bold
- Values (names, version number): `WHITE`
- GitHub URL: `AQUA` + Underlined + clickable `OPEN_URL`
- Feature text: `GRAY`
- Command names: `YELLOW`
- Command descriptions: `DARK_GRAY`

### `/endplus config`
Requires permission level 2 (operator). Dumps current config values in color-coded key=value pairs grouped by category.

---

## 13. Config System

Located at `config/endplus.json` (server-side config, loaded on startup):

```json
{
  "dragon": {
    "max_health": 500,
    "phase_thresholds": [75, 50, 25],
    "void_shield_reduction": 0.5,
    "void_shield_crystal_count": 2,
    "minion_wave_interval_phase2": 60,
    "minion_wave_interval_phase3": 30,
    "xp_reward": 20000,
    "fast_kill_threshold_seconds": 600
  },
  "minions": {
    "enable_void_imp": true,
    "enable_ender_phantom": true,
    "enable_endrite_golem": true,
    "enable_void_witch": true,
    "enable_shadow_drake": true,
    "max_simultaneous_minions": 20
  },
  "world": {
    "enable_void_wastes": true,
    "enable_crystalline_fields": true,
    "enable_umbral_forest": true,
    "enable_skyreach_peaks": true,
    "enable_sunken_citadel_region": true,
    "enable_nether_scar": true,
    "citadel_min_distance": 3000,
    "citadel_max_distance": 8000
  },
  "leviathan": {
    "max_health": 800,
    "enable": true,
    "respawnable": false
  },
  "difficulty_scaling": {
    "scale_with_player_count": true,
    "hp_multiplier_per_extra_player": 0.25,
    "damage_multiplier_per_extra_player": 0.1,
    "extra_minions_per_extra_player": 2
  }
}
```

---

## 14. Implementation Order

### Phase A — Foundation (Week 1–2)
1. Gradle project setup (Fabric 1.21.1, Java 21)
2. Mod initializer, Mixin setup, config loader
3. Register new blocks (End Stone variants, Umbral Wood, Void Ore, Endrite Ore)
4. Register new items (materials, reagents — no functionality yet)
5. Basic loot tables and recipes

### Phase B — Dragon Overhaul (Week 3–4)
1. Mixin into `EnderDragonEntity` to modify HP
2. Phase system (HP threshold tracking)
3. Void Shield mechanic + Void Crystals
4. Enhanced breath attack (size/duration)
5. Void Beam projectile
6. Improved AI (target tracking, Dive Bomb, hover AoE)
7. Dragon loot changes

### Phase C — Minions (Week 5–6)
1. Register all 5 minion entity types with models/textures
2. AI goals for each minion
3. Dragon summon logic (wave spawning tied to phase system)
4. Minion loot tables

### Phase D — New Mobs (Week 7–8)
1. Void Stalker (camouflage + pack AI)
2. Prism Crawler (shard projectile AI)
3. Shade Lurker (shadow stealth AI)
4. Cliff Wraith (knockback + item drop AI)
5. Ancient End Golem (patrol AI + AoE)
6. Ender Parasite (latch mechanic)
7. Spawn rules per biome

### Phase E — World Generation (Week 9–11)
1. Register 6 new End biomes
2. Biome-specific features (geysers, crystal spires, Umbral Trees, wind updrafts, void pools)
3. Biome surface/noise configuration
4. Integration with Fabric's biome API for outer End injection

### Phase F — Structures (Week 12–14)
1. Void Spire (NBT structure + generation)
2. Crystal Sanctum (underground, biome-specific)
3. Umbral Ruin (forest biome)
4. Skyreach Aerie (peak-top)
5. End Shipwreck (void-floating)
6. The Citadel (massive — multi-floor, 3 weeks alone)

### Phase G — Items, Weapons & Armor (Week 15–16)
1. Weapon implementations (Void Blade, Prism Lance, Shadow Bow, Endrite Maul, Void Wand)
2. Armor set implementations + set bonus logic
3. Utility items (Void Pouch death-save logic, Ender Compass, Heartwood Staff)
4. All crafting recipes

### Phase H — Enchantments (Week 17)
1. Register all 6 enchantments
2. Custom enchantment effect logic (Phase Cut, Void Walk, etc.)
3. Loot table integration for enchantment books

### Phase I — Void Leviathan Boss (Week 18–20)
1. Multi-segment entity architecture
2. All attack patterns
3. Void Node mechanic
4. Phase transitions
5. Loot + death event

### Phase J — Lore & Advancements (Week 21)
1. Lore Tablets (readable item UI)
2. Ender Codex (collection tracking)
3. All custom advancements + triggers

### Phase K — Polish & QA (Week 22–24)
1. Balance pass (damage, HP, drop rates)
2. Difficulty scaling (multiplayer config)
3. Sound design (custom sounds or repurposed vanilla)
4. Particle effects
5. Server-side compatibility testing (no client requirement for core features)
6. Performance profiling (entity AI, world gen)
7. Config validation

---

*This document is the authoritative design spec for End+. All implementation decisions should reference back to this plan. Deviations should be noted in commit messages.*
