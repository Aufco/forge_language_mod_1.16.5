package com.example.languagemod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraftforge.fml.loading.FMLPaths;
import java.util.*;

public class ProgressManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROGRESS_FILE_NAME = "languagemod_progress.json";
    private static final String EN_US_RESOURCE = "/assets/languagemod/lang/en_us.json";
    private static final String ES_MX_RESOURCE = "/assets/languagemod/lang/es_mx.json";
    
    private final Map<String, ProgressEntry> progressMap = new HashMap<>();
    private final Map<String, String> englishTranslations = new HashMap<>();
    private final Map<String, String> spanishTranslations = new HashMap<>();
    private long lastFlashcardTime = 0;
    private long flashcardInterval = 5 * 60 * 1000; // 5 minutes in milliseconds (configurable)
    private boolean welcomeMessageEnabled = true;
    private long lastCorrectFlashcardTime = 0;
    private long lastFlashcardAnswerTime = 0; // Tracks any flashcard answer (correct or incorrect)
    
    public static class ProgressEntry {
        public boolean discovered;
        public long discoveredTime;
        public int correctCount;
        public int attemptCount;
        public long lastAttemptTime;
        public boolean mastered;
        
        public ProgressEntry() {
            this.discovered = false;
            this.discoveredTime = 0;
            this.correctCount = 0;
            this.attemptCount = 0;
            this.lastAttemptTime = 0;
            this.mastered = false;
        }
    }
    
    public ProgressManager() {
        loadTranslations();
        loadProgress();
        lastFlashcardTime = System.currentTimeMillis();
    }
    
    private void loadTranslations() {
        try {
            // Load English translations from resources
            loadTranslationsFromResource(EN_US_RESOURCE, englishTranslations);
            
            // Load Spanish translations from resources
            loadTranslationsFromResource(ES_MX_RESOURCE, spanishTranslations);
            
            LOGGER.info("Loaded {} English and {} Spanish translations", 
                       englishTranslations.size(), spanishTranslations.size());
            
            // Initialize progress entries for all translation keys
            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(englishTranslations.keySet());
            allKeys.addAll(spanishTranslations.keySet());
            
            for (String key : allKeys) {
                progressMap.putIfAbsent(key, new ProgressEntry());
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to load translation files", e);
        }
    }
    
    private void loadTranslationsFromResource(String resourcePath, Map<String, String> targetMap) {
        try {
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                LOGGER.error("Could not find resource: " + resourcePath);
                return;
            }
            
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    targetMap.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load translations from resource: " + resourcePath, e);
        }
    }
    
    private void loadProgress() {
        File file = getProgressFile();
        if (!file.exists()) {
            saveProgress();
            return;
        }
        
        try (InputStreamReader reader = new InputStreamReader(
                new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
            
            if (jsonObject.has("progress")) {
                JsonObject progress = jsonObject.getAsJsonObject("progress");
                for (Map.Entry<String, JsonElement> entry : progress.entrySet()) {
                    String key = entry.getKey();
                    JsonObject data = entry.getValue().getAsJsonObject();
                    
                    ProgressEntry progressEntry = progressMap.getOrDefault(key, new ProgressEntry());
                    progressEntry.discovered = data.get("discovered").getAsBoolean();
                    progressEntry.discoveredTime = data.get("discoveredTime").getAsLong();
                    progressEntry.correctCount = data.has("correctCount") ? data.get("correctCount").getAsInt() : 0;
                    progressEntry.attemptCount = data.has("attemptCount") ? data.get("attemptCount").getAsInt() : 0;
                    progressEntry.lastAttemptTime = data.has("lastAttemptTime") ? data.get("lastAttemptTime").getAsLong() : 0;
                    progressEntry.mastered = data.has("mastered") ? data.get("mastered").getAsBoolean() : false;
                    
                    progressMap.put(key, progressEntry);
                }
            }
            
            // Load preferences
            if (jsonObject.has("preferences")) {
                JsonObject prefs = jsonObject.getAsJsonObject("preferences");
                if (prefs.has("welcomeMessageEnabled")) {
                    welcomeMessageEnabled = prefs.get("welcomeMessageEnabled").getAsBoolean();
                }
                if (prefs.has("flashcardInterval")) {
                    flashcardInterval = prefs.get("flashcardInterval").getAsLong();
                }
                if (prefs.has("lastFlashcardAnswerTime")) {
                    lastFlashcardAnswerTime = prefs.get("lastFlashcardAnswerTime").getAsLong();
                }
            }
            
            LOGGER.info("Loaded progress for {} items", progressMap.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load progress file", e);
        }
    }
    
    public void saveProgress() {
        try {
            JsonObject root = new JsonObject();
            JsonObject progress = new JsonObject();
            
            for (Map.Entry<String, ProgressEntry> entry : progressMap.entrySet()) {
                JsonObject data = new JsonObject();
                data.addProperty("discovered", entry.getValue().discovered);
                data.addProperty("discoveredTime", entry.getValue().discoveredTime);
                data.addProperty("correctCount", entry.getValue().correctCount);
                data.addProperty("attemptCount", entry.getValue().attemptCount);
                data.addProperty("lastAttemptTime", entry.getValue().lastAttemptTime);
                data.addProperty("mastered", entry.getValue().mastered);
                progress.add(entry.getKey(), data);
            }
            
            root.add("progress", progress);
            
            // Save preferences
            JsonObject prefs = new JsonObject();
            prefs.addProperty("welcomeMessageEnabled", welcomeMessageEnabled);
            prefs.addProperty("flashcardInterval", flashcardInterval);
            prefs.addProperty("lastFlashcardAnswerTime", lastFlashcardAnswerTime);
            root.add("preferences", prefs);
            
            // Create directory if it doesn't exist
            File file = getProgressFile();
            file.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(file)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(root, writer);
            }
            
        } catch (IOException e) {
            LOGGER.error("Failed to save progress", e);
        }
    }
    
    private File getProgressFile() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        return configDir.resolve(PROGRESS_FILE_NAME).toFile();
    }
    
    public void markItemDiscovered(String key) {
        ProgressEntry entry = progressMap.get(key);
        if (entry != null && !entry.discovered) {
            entry.discovered = true;
            entry.discoveredTime = System.currentTimeMillis();
            saveProgress();
            LOGGER.info("Marked {} as discovered", key);
            
            // Show flashcard for newly discovered items if no flashcard answered since startup or 5 minutes have passed
            long currentTime = System.currentTimeMillis();
            if (lastFlashcardAnswerTime == 0 || currentTime - lastFlashcardAnswerTime >= flashcardInterval) {
                showInitialFlashcard(key);
                resetFlashcardTimer();
            }
        }
    }
    
    public void markBiomeDiscovered(String biomeKey) {
        String key = "biome." + biomeKey.replace(":", ".");
        markItemDiscovered(key);
    }
    
    public void markEntityDiscovered(String entityKey) {
        markItemDiscovered(entityKey);
    }
    
    private void showInitialFlashcard(String key) {
        String english = englishTranslations.get(key);
        String spanish = spanishTranslations.get(key);
        
        if (english != null && spanish != null) {
            FlashcardManager.getInstance().showFlashcard(key, english, spanish, true);
        }
    }
    
    public void checkAndShowFlashcard() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFlashcardTime >= flashcardInterval) {
            lastFlashcardTime = currentTime;
            showRandomFlashcard();
        }
    }
    
    public void showRandomFlashcard() {
        // Get all discovered but not mastered items
        List<String> eligibleKeys = new ArrayList<>();
        for (Map.Entry<String, ProgressEntry> entry : progressMap.entrySet()) {
            if (entry.getValue().discovered && !entry.getValue().mastered) {
                eligibleKeys.add(entry.getKey());
            }
        }
        
        if (eligibleKeys.isEmpty()) {
            return;
        }
        
        // Pick a random key
        Random random = new Random();
        String key = eligibleKeys.get(random.nextInt(eligibleKeys.size()));
        
        String english = englishTranslations.get(key);
        String spanish = spanishTranslations.get(key);
        
        if (english != null && spanish != null) {
            // Randomly decide whether to ask English->Spanish or Spanish->English
            boolean askEnglishToSpanish = random.nextBoolean();
            FlashcardManager.getInstance().showFlashcard(key, english, spanish, askEnglishToSpanish);
        }
    }
    
    public void recordFlashcardAttempt(String key, boolean correct) {
        ProgressEntry entry = progressMap.get(key);
        if (entry != null) {
            entry.attemptCount++;
            entry.lastAttemptTime = System.currentTimeMillis();
            
            // Track that any flashcard was answered
            lastFlashcardAnswerTime = System.currentTimeMillis();
            
            if (correct) {
                entry.correctCount++;
                lastCorrectFlashcardTime = System.currentTimeMillis();
                
                if (entry.correctCount >= 5 && !entry.mastered) {
                    entry.mastered = true;
                    LOGGER.info("Item {} has been mastered!", key);
                    
                    // Show mastery message
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        String spanishName = spanishTranslations.get(key);
                        mc.player.displayClientMessage(
                            new net.minecraft.util.text.StringTextComponent("Mastered: ")
                                .withStyle(net.minecraft.util.text.TextFormatting.GOLD, net.minecraft.util.text.TextFormatting.BOLD)
                                .append(new net.minecraft.util.text.StringTextComponent(spanishName)
                                    .withStyle(net.minecraft.util.text.TextFormatting.YELLOW)),
                            false
                        );
                    }
                }
            }
            
            // Reset the 5-minute timer whenever any flashcard is answered
            resetFlashcardTimer();
            saveProgress();
        }
    }
    
    public int getDiscoveredCount() {
        return (int) progressMap.values().stream()
                .filter(entry -> entry.discovered)
                .count();
    }
    
    public int getTotalItemCount() {
        return progressMap.size();
    }
    
    public int getMasteredCount() {
        return (int) progressMap.values().stream()
                .filter(entry -> entry.mastered)
                .count();
    }
    
    public int getDiscoveredBiomeCount() {
        return (int) progressMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("biome.") && entry.getValue().discovered)
                .count();
    }
    
    public boolean isItemDiscovered(String key) {
        ProgressEntry entry = progressMap.get(key);
        return entry != null && entry.discovered;
    }
    
    public boolean isBiomeDiscovered(String biomeKey) {
        String key = "biome." + biomeKey.replace(":", ".");
        return isItemDiscovered(key);
    }
    
    public String getEnglishTranslation(String key) {
        return englishTranslations.getOrDefault(key, key);
    }
    
    public String getSpanishTranslation(String key) {
        return spanishTranslations.getOrDefault(key, key);
    }
    
    public boolean isWelcomeMessageEnabled() {
        return welcomeMessageEnabled;
    }
    
    public void setWelcomeMessageEnabled(boolean enabled) {
        welcomeMessageEnabled = enabled;
        saveProgress();
    }
    
    public long getFlashcardIntervalMinutes() {
        return flashcardInterval / (60 * 1000);
    }
    
    public void setFlashcardIntervalMinutes(int minutes) {
        if (minutes >= 1 && minutes <= 120) { // Allow 1-120 minutes
            flashcardInterval = minutes * 60 * 1000L;
            saveProgress();
        }
    }
    
    public void resetFlashcardTimer() {
        lastFlashcardTime = System.currentTimeMillis();
    }
    
    public void showStartupFlashcard() {
        // Reset timer to 0 when joining world (as per requirements)
        lastFlashcardAnswerTime = 0;
        // Show a random flashcard using same logic as other random flashcards
        showRandomFlashcard();
        resetFlashcardTimer();
    }
}