package com.example.languagemod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
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
        
        // Register /hint command
        LiteralArgumentBuilder<CommandSource> hintCommand = Commands.literal("hint")
                .executes(context -> {
                    if (progressManager != null) {
                        String hint = progressManager.getCurrentTargetHint();
                        String targetItem = progressManager.getCurrentTargetItem();
                        
                        if (targetItem != null) {
                            ProgressManager.BlockData data = progressManager.getCurrentTargetData();
                            if (data != null) {
                                context.getSource().sendSuccess(
                                    new StringTextComponent("Current target: ")
                                        .withStyle(TextFormatting.YELLOW)
                                        .append(new StringTextComponent(data.english_name)
                                            .withStyle(TextFormatting.WHITE)),
                                    false
                                );
                            }
                        }
                        
                        context.getSource().sendSuccess(
                            new StringTextComponent("Hint: ")
                                .withStyle(TextFormatting.AQUA)
                                .append(new StringTextComponent(hint)
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
                });
        
        // Register /skip command
        LiteralArgumentBuilder<CommandSource> skipCommand = Commands.literal("skip")
                .executes(context -> {
                    if (progressManager != null) {
                        String currentItem = progressManager.getCurrentTargetItem();
                        if (currentItem != null) {
                            ProgressManager.BlockData data = progressManager.getBlockData(currentItem);
                            String itemName = data != null ? data.english_name : currentItem;
                            
                            progressManager.skipCurrentItem();
                            
                            context.getSource().sendSuccess(
                                new StringTextComponent("Skipped: ")
                                    .withStyle(TextFormatting.YELLOW)
                                    .append(new StringTextComponent(itemName)
                                        .withStyle(TextFormatting.WHITE))
                                    .append(new StringTextComponent(" (moved to end of queue)")
                                        .withStyle(TextFormatting.GRAY)),
                                false
                            );
                            
                            // Show new target
                            String newTarget = progressManager.getCurrentTargetItem();
                            if (newTarget != null) {
                                ProgressManager.BlockData newData = progressManager.getBlockData(newTarget);
                                if (newData != null) {
                                    context.getSource().sendSuccess(
                                        new StringTextComponent("New target: ")
                                            .withStyle(TextFormatting.GREEN)
                                            .append(new StringTextComponent(newData.english_name)
                                                .withStyle(TextFormatting.WHITE)),
                                        false
                                    );
                                }
                            } else {
                                context.getSource().sendSuccess(
                                    new StringTextComponent("All items discovered!")
                                        .withStyle(TextFormatting.GREEN),
                                    false
                                );
                            }
                        } else {
                            context.getSource().sendSuccess(
                                new StringTextComponent("No items left to skip!")
                                    .withStyle(TextFormatting.YELLOW),
                                false
                            );
                        }
                    } else {
                        context.getSource().sendFailure(
                            new StringTextComponent("Progress system not initialized!")
                                .withStyle(TextFormatting.RED)
                        );
                    }
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
        
        dispatcher.register(hintCommand);
        dispatcher.register(skipCommand);
        dispatcher.register(slowCommand);
        dispatcher.register(testAudioCommand);
        
        LOGGER.info("Language commands registered");
    }
}