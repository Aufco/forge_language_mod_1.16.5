package com.example.languagemod;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class FlashcardManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static FlashcardManager instance;
    
    private String currentFlashcardKey = null;
    private String expectedAnswer = null;
    private boolean askingEnglishToSpanish = true;
    private boolean waitingForAnswer = false;
    private boolean isRetryAttempt = false;
    
    private FlashcardManager() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public static FlashcardManager getInstance() {
        if (instance == null) {
            instance = new FlashcardManager();
        }
        return instance;
    }
    
    public void showFlashcard(String key, String english, String spanish, boolean englishToSpanish) {
        if (waitingForAnswer) {
            return; // Don't show a new flashcard if we're waiting for an answer
        }
        
        currentFlashcardKey = key;
        askingEnglishToSpanish = englishToSpanish;
        waitingForAnswer = true;
        isRetryAttempt = false;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        if (englishToSpanish) {
            expectedAnswer = spanish;
            mc.player.displayClientMessage(
                new StringTextComponent("[Flashcard] ")
                    .withStyle(TextFormatting.AQUA, TextFormatting.BOLD)
                    .append(new StringTextComponent("What is \"" + english + "\" in Spanish?")
                        .withStyle(TextFormatting.WHITE)),
                false
            );
        } else {
            expectedAnswer = english;
            mc.player.displayClientMessage(
                new StringTextComponent("[Flashcard] ")
                    .withStyle(TextFormatting.AQUA, TextFormatting.BOLD)
                    .append(new StringTextComponent("What is \"" + spanish + "\" in English?")
                        .withStyle(TextFormatting.WHITE)),
                false
            );
        }
    }
    
    @SubscribeEvent
    public void onChatMessage(ClientChatEvent event) {
        if (!waitingForAnswer || expectedAnswer == null) {
            return;
        }
        
        String message = event.getMessage();
        
        // Don't process commands
        if (message.startsWith("/")) {
            return;
        }
        
        // Cancel the event so the message doesn't get sent to server
        event.setCanceled(true);
        
        // Check the answer
        boolean correct = checkAnswer(message, expectedAnswer);
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        if (correct) {
            mc.player.displayClientMessage(
                new StringTextComponent("[CORRECT] ")
                    .withStyle(TextFormatting.GREEN, TextFormatting.BOLD)
                    .append(new StringTextComponent("Correct! The answer is \"" + expectedAnswer + "\"")
                        .withStyle(TextFormatting.GREEN)),
                false
            );
            
            // Record the attempt (only count as correct if not a retry)
            if (!isRetryAttempt) {
                ProgressManager pm = LanguageDisplayMod.getInstance().getProgressManager();
                if (pm != null) {
                    pm.recordFlashcardAttempt(currentFlashcardKey, true);
                }
            }
            
            waitingForAnswer = false;
            currentFlashcardKey = null;
            expectedAnswer = null;
        } else {
            mc.player.displayClientMessage(
                new StringTextComponent("[INCORRECT] ")
                    .withStyle(TextFormatting.RED, TextFormatting.BOLD)
                    .append(new StringTextComponent("Not quite! The answer is \"" + expectedAnswer + "\". Try typing it again.")
                        .withStyle(TextFormatting.YELLOW)),
                false
            );
            
            // Record the failed attempt
            if (!isRetryAttempt) {
                ProgressManager pm = LanguageDisplayMod.getInstance().getProgressManager();
                if (pm != null) {
                    pm.recordFlashcardAttempt(currentFlashcardKey, false);
                }
                isRetryAttempt = true; // Mark that next attempt is a retry
            }
            
            // Keep waiting for another answer
        }
    }
    
    private boolean checkAnswer(String userAnswer, String correctAnswer) {
        // Normalize both strings
        String normalizedUser = normalizeString(userAnswer);
        String normalizedCorrect = normalizeString(correctAnswer);
        
        // First check for exact match (after normalization)
        if (normalizedUser.equalsIgnoreCase(normalizedCorrect)) {
            return true;
        }
        
        // Calculate Levenshtein distance
        int distance = calculateLevenshteinDistance(normalizedUser.toLowerCase(), normalizedCorrect.toLowerCase());
        
        // Allow up to 2 character differences for short words, or 15% of word length for longer words
        int maxAllowedDistance = Math.max(2, (int)(normalizedCorrect.length() * 0.15));
        
        return distance <= maxAllowedDistance;
    }
    
    private String normalizeString(String input) {
        // Remove accents and special characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        
        // Trim whitespace
        normalized = normalized.trim();
        
        return normalized;
    }
    
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], // substitution
                                   Math.min(dp[i - 1][j],     // deletion
                                           dp[i][j - 1]));    // insertion
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    public boolean isWaitingForAnswer() {
        return waitingForAnswer;
    }
    
    public void cancelCurrentFlashcard() {
        waitingForAnswer = false;
        currentFlashcardKey = null;
        expectedAnswer = null;
        isRetryAttempt = false;
    }
}