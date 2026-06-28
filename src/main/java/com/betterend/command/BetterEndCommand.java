package com.betterend.command;

import com.betterend.BetterEnd;
import com.betterend.config.BetterEndConfig;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BetterEndCommand {

    private static final String GITHUB = "https://github.com/juliuspleunes4/BetterEnd";
    private static final String AUTHOR = "Julius Pleunes";
    private static final String DIVIDER = "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                CommandManager.literal("betterend")
                    .then(CommandManager.literal("help")
                        .executes(BetterEndCommand::executeHelp))
                    .then(CommandManager.literal("config")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(BetterEndCommand::executeConfig))
            )
        );
    }

    private static int executeHelp(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();

        send(source, divider());
        send(source, title());
        send(source, subtitle());
        send(source, divider());
        send(source, infoRow("Author ", AUTHOR));
        send(source, infoRow("Version", BetterEnd.MOD_VERSION));
        send(source, githubRow());
        send(source, divider());
        send(source, sectionHeader("Features"));
        send(source, featureBullet("Enhanced Dragon Fight", "500 HP, 4 phases, minions"));
        send(source, featureBullet("5 Dragon Minions", "Void Imp, Ender Phantom, Endrite Golem, Void Witch, Shadow Drake"));
        send(source, featureBullet("6 New End Biomes", "Void Wastes, Crystalline Fields, Umbral Forest, Skyreach Peaks & more"));
        send(source, featureBullet("6 New Structures", "incl. The Citadel dungeon"));
        send(source, featureBullet("6 New Mobs", "Void Stalker, Prism Crawler, Shade Lurker & more"));
        send(source, featureBullet("New Gear & Materials", "Void Blade, Shadow Bow, Endrite Maul, 4 armor sets"));
        send(source, featureBullet("The Void Leviathan", "Final boss — 800 HP, 5 weak-point Nodes"));
        send(source, divider());
        send(source, sectionHeader("Commands"));
        send(source, commandRow("/betterend help  ", "Show this menu"));
        send(source, commandRow("/betterend config", "View active config values  [OP]"));
        send(source, divider());

        return 1;
    }

    private static int executeConfig(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        BetterEndConfig cfg = BetterEnd.CONFIG;

        send(source, divider());
        send(source, Text.literal(" BetterEnd Config").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withBold(true)));
        send(source, divider());

        send(source, configSection("Dragon"));
        send(source, configRow("max_health", String.valueOf(cfg.dragon.maxHealth)));
        send(source, configRow("xp_reward", String.valueOf(cfg.dragon.xpReward)));
        send(source, configRow("fast_kill_threshold_seconds", String.valueOf(cfg.dragon.fastKillThresholdSeconds)));
        send(source, configRow("void_shield_reduction", String.valueOf(cfg.dragon.voidShieldReduction)));
        send(source, configRow("minion_wave_interval_phase2", cfg.dragon.minionWaveIntervalPhase2 + "s"));
        send(source, configRow("minion_wave_interval_phase3", cfg.dragon.minionWaveIntervalPhase3 + "s"));

        send(source, configSection("Minions"));
        send(source, configRow("void_imp", String.valueOf(cfg.minions.enableVoidImp)));
        send(source, configRow("ender_phantom", String.valueOf(cfg.minions.enableEnderPhantom)));
        send(source, configRow("endrite_golem", String.valueOf(cfg.minions.enableEndriteGolem)));
        send(source, configRow("void_witch", String.valueOf(cfg.minions.enableVoidWitch)));
        send(source, configRow("shadow_drake", String.valueOf(cfg.minions.enableShadowDrake)));
        send(source, configRow("max_simultaneous", String.valueOf(cfg.minions.maxSimultaneousMinions)));

        send(source, configSection("World"));
        send(source, configRow("void_wastes", String.valueOf(cfg.world.enableVoidWastes)));
        send(source, configRow("crystalline_fields", String.valueOf(cfg.world.enableCrystallineFields)));
        send(source, configRow("umbral_forest", String.valueOf(cfg.world.enableUmbralForest)));
        send(source, configRow("skyreach_peaks", String.valueOf(cfg.world.enableSkyreachPeaks)));
        send(source, configRow("sunken_citadel", String.valueOf(cfg.world.enableSunkenCitadelRegion)));
        send(source, configRow("nether_scar", String.valueOf(cfg.world.enableNetherScar)));
        send(source, configRow("citadel_min_distance", String.valueOf(cfg.world.citadelMinDistance)));
        send(source, configRow("citadel_max_distance", String.valueOf(cfg.world.citadelMaxDistance)));

        send(source, configSection("Leviathan"));
        send(source, configRow("max_health", String.valueOf(cfg.leviathan.maxHealth)));
        send(source, configRow("enabled", String.valueOf(cfg.leviathan.enable)));
        send(source, configRow("respawnable", String.valueOf(cfg.leviathan.respawnable)));

        send(source, configSection("Difficulty Scaling"));
        send(source, configRow("scale_with_player_count", String.valueOf(cfg.difficultyScaling.scaleWithPlayerCount)));
        send(source, configRow("hp_mult_per_extra_player", String.valueOf(cfg.difficultyScaling.hpMultiplierPerExtraPlayer)));
        send(source, configRow("dmg_mult_per_extra_player", String.valueOf(cfg.difficultyScaling.damageMultiplierPerExtraPlayer)));
        send(source, configRow("extra_minions_per_player", String.valueOf(cfg.difficultyScaling.extraMinionsPerExtraPlayer)));

        send(source, divider());

        return 1;
    }

    private static void send(ServerCommandSource source, Text text) {
        source.sendFeedback(() -> text, false);
    }

    private static Text divider() {
        return Text.literal(DIVIDER).setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE));
    }

    private static Text title() {
        return Text.literal("          ✦  BetterEnd  ✦")
                .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withBold(true));
    }

    private static Text subtitle() {
        return Text.literal("    An End Dimension Overhaul Mod")
                .setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE));
    }

    private static Text infoRow(String label, String value) {
        return Text.empty()
                .append(Text.literal(" " + label + "  ").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true)))
                .append(Text.literal(value).setStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(false)));
    }

    private static Text githubRow() {
        MutableText link = Text.literal(GITHUB)
                .setStyle(Style.EMPTY
                        .withColor(Formatting.AQUA)
                        .withUnderline(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, GITHUB)));
        return Text.empty()
                .append(Text.literal(" GitHub  ").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true)))
                .append(link);
    }

    private static Text sectionHeader(String label) {
        return Text.literal(" " + label)
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true));
    }

    private static Text featureBullet(String name, String detail) {
        return Text.empty()
                .append(Text.literal("  ✦ ").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withBold(false)))
                .append(Text.literal(name).setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                .append(Text.literal("  " + detail).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
    }

    private static Text commandRow(String cmd, String description) {
        return Text.empty()
                .append(Text.literal("  " + cmd + "  ").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)))
                .append(Text.literal(description).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
    }

    private static Text configSection(String label) {
        return Text.literal(" " + label)
                .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withBold(true));
    }

    private static Text configRow(String key, String value) {
        Formatting valueColor = "true".equals(value) ? Formatting.GREEN
                : "false".equals(value) ? Formatting.RED
                : Formatting.AQUA;
        return Text.empty()
                .append(Text.literal("   " + key + " = ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                .append(Text.literal(value).setStyle(Style.EMPTY.withColor(valueColor)));
    }
}
