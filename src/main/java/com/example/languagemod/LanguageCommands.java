package com.example.languagemod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LanguageCommands {
    private static final Logger LOGGER = LogManager.getLogger();
    private static ProgressManager progressManager;
    
    public static void setProgressManager(ProgressManager manager) {
        progressManager = manager;
    }
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        
        // Register /hint command - no longer shows hints since we removed descriptions
        LiteralArgumentBuilder<CommandSource> hintCommand = Commands.literal("hint")
                .executes(context -> {
                    context.getSource().sendSuccess(
                        new StringTextComponent("/hint command has been removed in this version")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    return 1;
                });
        
        // Register /skip command - no longer used since we removed the target system
        LiteralArgumentBuilder<CommandSource> skipCommand = Commands.literal("skip")
                .executes(context -> {
                    context.getSource().sendSuccess(
                        new StringTextComponent("/skip command has been removed in this version")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    return 1;
                });
        
        // Register /slow command with argument
        LiteralArgumentBuilder<CommandSource> slowCommand = Commands.literal("slow")
                .then(Commands.argument("speed", com.mojang.brigadier.arguments.FloatArgumentType.floatArg(0.25f, 2.0f))
                    .executes(context -> {
                        float speed = com.mojang.brigadier.arguments.FloatArgumentType.getFloat(context, "speed");
                        AudioManager.getInstance().setPlaybackSpeed(speed);
                        
                        context.getSource().sendSuccess(
                            new StringTextComponent("Audio playback speed set to: ")
                                .withStyle(TextFormatting.GREEN)
                                .append(new StringTextComponent(String.format("%.2fx", speed))
                                    .withStyle(TextFormatting.WHITE)),
                            false
                        );
                        return 1;
                    }))
                .executes(context -> {
                    // No argument provided, show current speed
                    float currentSpeed = AudioManager.getInstance().getPlaybackSpeed();
                    context.getSource().sendSuccess(
                        new StringTextComponent("Current playback speed: ")
                            .withStyle(TextFormatting.YELLOW)
                            .append(new StringTextComponent(String.format("%.2fx", currentSpeed))
                                .withStyle(TextFormatting.WHITE))
                            .append(new StringTextComponent(" (use /slow <0.25-2.0> to change)")
                                .withStyle(TextFormatting.GRAY)),
                        false
                    );
                    return 1;
                });
        
        // Register /testaudio command for debugging
        LiteralArgumentBuilder<CommandSource> testAudioCommand = Commands.literal("testaudio")
                .then(Commands.argument("key", com.mojang.brigadier.arguments.StringArgumentType.string())
                    .executes(context -> {
                        String key = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "key");
                        AudioManager audioManager = AudioManager.getInstance();
                        
                        context.getSource().sendSuccess(
                            new StringTextComponent("Testing audio for key: ")
                                .withStyle(TextFormatting.YELLOW)
                                .append(new StringTextComponent(key)
                                    .withStyle(TextFormatting.WHITE)),
                            false
                        );
                        
                        if (audioManager.hasAudioForKey(key)) {
                            audioManager.playAudio(key);
                            context.getSource().sendSuccess(
                                new StringTextComponent("Audio file found and playing!")
                                    .withStyle(TextFormatting.GREEN),
                                false
                            );
                        } else {
                            context.getSource().sendSuccess(
                                new StringTextComponent("No audio file found for this key")
                                    .withStyle(TextFormatting.RED),
                                false
                            );
                        }
                        return 1;
                    }));
        
        // Register /flashcard command to manually trigger a flashcard
        LiteralArgumentBuilder<CommandSource> flashcardCommand = Commands.literal("flashcard")
                .executes(context -> {
                    if (progressManager != null) {
                        progressManager.showRandomFlashcard();
                        context.getSource().sendSuccess(
                            new StringTextComponent("Flashcard triggered!")
                                .withStyle(TextFormatting.GREEN),
                            false
                        );
                    } else {
                        context.getSource().sendFailure(
                            new StringTextComponent("Progress system not initialized!")
                                .withStyle(TextFormatting.RED)
                        );
                    }
                    return 1;
                });
        
        // Register /progress command to show progress stats
        LiteralArgumentBuilder<CommandSource> progressCommand = Commands.literal("progress")
                .executes(context -> {
                    if (progressManager != null) {
                        int discovered = progressManager.getDiscoveredCount();
                        int total = progressManager.getTotalItemCount();
                        int mastered = progressManager.getMasteredCount();
                        int biomes = progressManager.getDiscoveredBiomeCount();
                        
                        context.getSource().sendSuccess(
                            new StringTextComponent("=== Progress Statistics ===")
                                .withStyle(TextFormatting.GOLD, TextFormatting.BOLD),
                            false
                        );
                        context.getSource().sendSuccess(
                            new StringTextComponent("Total discovered: " + discovered + "/" + total + " (" + 
                                String.format("%.1f", (discovered * 100.0 / total)) + "%)")
                                .withStyle(TextFormatting.GREEN),
                            false
                        );
                        context.getSource().sendSuccess(
                            new StringTextComponent("Items mastered: " + mastered + " (5+ correct flashcards)")
                                .withStyle(TextFormatting.AQUA),
                            false
                        );
                        context.getSource().sendSuccess(
                            new StringTextComponent("Biomes discovered: " + biomes)
                                .withStyle(TextFormatting.YELLOW),
                            false
                        );
                    } else {
                        context.getSource().sendFailure(
                            new StringTextComponent("Progress system not initialized!")
                                .withStyle(TextFormatting.RED)
                        );
                    }
                    return 1;
                });
        
        // Register /languagehelp command (using languagehelp to avoid conflicts)
        LiteralArgumentBuilder<CommandSource> helpCommand = Commands.literal("languagehelp")
                .executes(context -> {
                    context.getSource().sendSuccess(
                        new StringTextComponent("=== Language Mod Commands ===")
                            .withStyle(TextFormatting.GOLD, TextFormatting.BOLD),
                        false
                    );
                    context.getSource().sendSuccess(
                        new StringTextComponent("/progress - Show your learning progress")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    context.getSource().sendSuccess(
                        new StringTextComponent("/flashcard - Trigger a flashcard quiz")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    context.getSource().sendSuccess(
                        new StringTextComponent("/slow [speed] - Set audio playback speed (0.25-2.0)")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    context.getSource().sendSuccess(
                        new StringTextComponent("/testaudio <key> - Test audio for a translation key")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    context.getSource().sendSuccess(
                        new StringTextComponent("/languagetoggle - Toggle welcome message on/off")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    context.getSource().sendSuccess(
                        new StringTextComponent("/flashcardtime [minutes] - Set flashcard interval (1-120 min)")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    context.getSource().sendSuccess(
                        new StringTextComponent("/languagehelp - Show this help message")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    context.getSource().sendSuccess(
                        new StringTextComponent("/testvanilla - Test vanilla Minecraft sounds")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    context.getSource().sendSuccess(
                        new StringTextComponent("/debugsound <key> - Debug specific sound")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    return 1;
                });
        
        // Register /languagetoggle command
        LiteralArgumentBuilder<CommandSource> toggleCommand = Commands.literal("languagetoggle")
                .executes(context -> {
                    ProgressManager pm = progressManager;
                    if (pm != null) {
                        boolean newValue = !pm.isWelcomeMessageEnabled();
                        pm.setWelcomeMessageEnabled(newValue);
                        context.getSource().sendSuccess(
                            new StringTextComponent("Welcome message is now " + (newValue ? "ENABLED" : "DISABLED"))
                                .withStyle(newValue ? TextFormatting.GREEN : TextFormatting.RED),
                            false
                        );
                    }
                    return 1;
                });
        
        // Register /flashcardtime command to set flashcard interval
        LiteralArgumentBuilder<CommandSource> flashcardTimeCommand = Commands.literal("flashcardtime")
                .then(Commands.argument("minutes", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 120))
                    .executes(context -> {
                        int minutes = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "minutes");
                        if (progressManager != null) {
                            progressManager.setFlashcardIntervalMinutes(minutes);
                            context.getSource().sendSuccess(
                                new StringTextComponent("Flashcard interval set to ")
                                    .withStyle(TextFormatting.GREEN)
                                    .append(new StringTextComponent(minutes + " minutes")
                                        .withStyle(TextFormatting.WHITE)),
                                false
                            );
                        } else {
                            context.getSource().sendFailure(
                                new StringTextComponent("Progress system not initialized!")
                                    .withStyle(TextFormatting.RED)
                            );
                        }
                        return 1;
                    }))
                .executes(context -> {
                    // No argument provided, show current interval
                    if (progressManager != null) {
                        long currentMinutes = progressManager.getFlashcardIntervalMinutes();
                        context.getSource().sendSuccess(
                            new StringTextComponent("Current flashcard interval: ")
                                .withStyle(TextFormatting.YELLOW)
                                .append(new StringTextComponent(currentMinutes + " minutes")
                                    .withStyle(TextFormatting.WHITE))
                                .append(new StringTextComponent(" (use /flashcardtime <1-120> to change)")
                                    .withStyle(TextFormatting.GRAY)),
                            false
                        );
                    } else {
                        context.getSource().sendFailure(
                            new StringTextComponent("Progress system not initialized!")
                                .withStyle(TextFormatting.RED)
                        );
                    }
                    return 1;
                });
        
        // Test vanilla sound command
        LiteralArgumentBuilder<CommandSource> testVanillaCommand = Commands.literal("testvanilla")
                .executes(context -> {
                    try {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player != null) {
                            // Play a vanilla note block sound to verify sound system works
                            mc.player.playSound(SoundEvents.NOTE_BLOCK_BELL, 1.0f, 1.0f);
                            context.getSource().sendSuccess(
                                new StringTextComponent("Playing vanilla bell sound. Did you hear it?")
                                    .withStyle(TextFormatting.YELLOW),
                                false
                            );
                        }
                    } catch (Exception e) {
                        context.getSource().sendFailure(
                            new StringTextComponent("Error: " + e.getMessage())
                                .withStyle(TextFormatting.RED)
                        );
                    }
                    return 1;
                });
        
        // Debug sound command
        LiteralArgumentBuilder<CommandSource> debugSoundCommand = Commands.literal("debugsound")
                .then(Commands.argument("key", com.mojang.brigadier.arguments.StringArgumentType.string())
                    .executes(context -> {
                        String key = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "key");
                        
                        // Get the sound event
                        net.minecraft.util.SoundEvent soundEvent = ModSounds.getSoundEvent(key);
                        if (soundEvent == null) {
                            context.getSource().sendFailure(
                                new StringTextComponent("Sound not registered for key: " + key)
                                    .withStyle(TextFormatting.RED)
                            );
                            return 1;
                        }
                        
                        // Debug info
                        context.getSource().sendSuccess(
                            new StringTextComponent("=== Sound Debug Info ===")
                                .withStyle(TextFormatting.GOLD),
                            false
                        );
                        context.getSource().sendSuccess(
                            new StringTextComponent("Translation key: " + key)
                                .withStyle(TextFormatting.YELLOW),
                            false
                        );
                        context.getSource().sendSuccess(
                            new StringTextComponent("Sound location: " + soundEvent.getLocation())
                                .withStyle(TextFormatting.YELLOW),
                            false
                        );
                        context.getSource().sendSuccess(
                            new StringTextComponent("Expected file: sounds/es_mx/" + key + ".ogg")
                                .withStyle(TextFormatting.YELLOW),
                            false
                        );
                        
                        // Try to play it directly
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player != null && mc.level != null) {
                            mc.level.playSound(mc.player, mc.player.blockPosition(), soundEvent, 
                                net.minecraft.util.SoundCategory.RECORDS, 1.0f, 1.0f);
                            context.getSource().sendSuccess(
                                new StringTextComponent("Attempted to play sound using World.playSound")
                                    .withStyle(TextFormatting.GREEN),
                                false
                            );
                        }
                        
                        return 1;
                    }));
        
        // Check resource command
        LiteralArgumentBuilder<CommandSource> checkResourceCommand = Commands.literal("checkresource")
                .then(Commands.argument("key", com.mojang.brigadier.arguments.StringArgumentType.string())
                    .executes(context -> {
                        String key = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "key");
                        
                        // Check if the OGG file exists as a resource
                        String resourcePath = "/assets/languagemod/sounds/es_mx/" + key + ".ogg";
                        java.io.InputStream stream = LanguageDisplayMod.class.getResourceAsStream(resourcePath);
                        
                        if (stream != null) {
                            try {
                                int size = stream.available();
                                stream.close();
                                context.getSource().sendSuccess(
                                    new StringTextComponent("Resource found: " + resourcePath + " (Size: " + size + " bytes)")
                                        .withStyle(TextFormatting.GREEN),
                                    false
                                );
                            } catch (Exception e) {
                                context.getSource().sendFailure(
                                    new StringTextComponent("Error reading resource: " + e.getMessage())
                                        .withStyle(TextFormatting.RED)
                                );
                            }
                        } else {
                            context.getSource().sendFailure(
                                new StringTextComponent("Resource NOT found: " + resourcePath)
                                    .withStyle(TextFormatting.RED)
                            );
                        }
                        
                        return 1;
                    }));
        
        dispatcher.register(hintCommand);
        dispatcher.register(skipCommand);
        dispatcher.register(slowCommand);
        dispatcher.register(testAudioCommand);
        dispatcher.register(flashcardCommand);
        dispatcher.register(progressCommand);
        dispatcher.register(helpCommand);
        dispatcher.register(toggleCommand);
        dispatcher.register(flashcardTimeCommand);
        dispatcher.register(testVanillaCommand);
        dispatcher.register(debugSoundCommand);
        dispatcher.register(checkResourceCommand);
        
        LOGGER.info("Language commands registered");
    }
}