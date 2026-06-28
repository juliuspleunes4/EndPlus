package com.betterend.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class BetterEndConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public DragonConfig dragon = new DragonConfig();
    public MinionConfig minions = new MinionConfig();
    public WorldConfig world = new WorldConfig();
    public LeviathanConfig leviathan = new LeviathanConfig();
    public DifficultyScalingConfig difficultyScaling = new DifficultyScalingConfig();

    public static class DragonConfig {
        public int maxHealth = 500;
        public int[] phaseThresholds = {75, 50, 25};
        public double voidShieldReduction = 0.5;
        public int voidShieldCrystalCount = 2;
        public int minionWaveIntervalPhase2 = 60;
        public int minionWaveIntervalPhase3 = 30;
        public int xpReward = 20000;
        public int fastKillThresholdSeconds = 600;
    }

    public static class MinionConfig {
        public boolean enableVoidImp = true;
        public boolean enableEnderPhantom = true;
        public boolean enableEndriteGolem = true;
        public boolean enableVoidWitch = true;
        public boolean enableShadowDrake = true;
        public int maxSimultaneousMinions = 20;
    }

    public static class WorldConfig {
        public boolean enableVoidWastes = true;
        public boolean enableCrystallineFields = true;
        public boolean enableUmbralForest = true;
        public boolean enableSkyreachPeaks = true;
        public boolean enableSunkenCitadelRegion = true;
        public boolean enableNetherScar = true;
        public int citadelMinDistance = 3000;
        public int citadelMaxDistance = 8000;
    }

    public static class LeviathanConfig {
        public int maxHealth = 800;
        public boolean enable = true;
        public boolean respawnable = false;
    }

    public static class DifficultyScalingConfig {
        public boolean scaleWithPlayerCount = true;
        public double hpMultiplierPerExtraPlayer = 0.25;
        public double damageMultiplierPerExtraPlayer = 0.1;
        public int extraMinionsPerExtraPlayer = 2;
    }

    public static BetterEndConfig load(Path configDir) {
        Path configFile = configDir.resolve("betterend.json");
        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                return GSON.fromJson(reader, BetterEndConfig.class);
            } catch (IOException e) {
                return new BetterEndConfig();
            }
        }
        BetterEndConfig defaults = new BetterEndConfig();
        save(defaults, configFile);
        return defaults;
    }

    private static void save(BetterEndConfig config, Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ignored) {
        }
    }
}
