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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ProgressManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROGRESS_FILE = "C:/Users/benau/forge_language_mod_1.16.5/progress_tracker.json";
    private static final String EN_US_FILE = "C:/Users/benau/forge_language_mod_1.16.5/translation_keys/1.16.5/en_us.json";
    private static final String ES_MX_FILE = "C:/Users/benau/forge_language_mod_1.16.5/translation_keys/1.16.5/es_mx.json";
    
    private final Map<String, ProgressEntry> progressMap = new HashMap<>();
    private final Map<String, String> englishTranslations = new HashMap<>();
    private final Map<String, String> spanishTranslations = new HashMap<>();
    private long lastFlashcardTime = 0;
    private static final long FLASHCARD_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
    private boolean welcomeMessageEnabled = true;
    private long lastCorrectFlashcardTime = 0;
    
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
            // Load English translations
            try (InputStreamReader reader = new InputStreamReader(
                    new java.io.FileInputStream(EN_US_FILE), StandardCharsets.UTF_8)) {
                JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    englishTranslations.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            
            // Load Spanish translations
            try (InputStreamReader reader = new InputStreamReader(
                    new java.io.FileInputStream(ES_MX_FILE), StandardCharsets.UTF_8)) {
                JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    spanishTranslations.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            
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
    
    private void loadProgress() {
        File file = new File(PROGRESS_FILE);
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
            root.add("preferences", prefs);
            
            // Create directory if it doesn't exist
            File file = new File(PROGRESS_FILE);
            file.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(file)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(root, writer);
            }
            
        } catch (IOException e) {
            LOGGER.error("Failed to save progress", e);
        }
    }
    
    public void markItemDiscovered(String key) {
        ProgressEntry entry = progressMap.get(key);
        if (entry != null && !entry.discovered) {
            entry.discovered = true;
            entry.discoveredTime = System.currentTimeMillis();
            saveProgress();
            LOGGER.info("Marked {} as discovered", key);
            
            // Show flashcard if enough time has passed since last correct answer
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCorrectFlashcardTime >= FLASHCARD_INTERVAL) {
                showInitialFlashcard(key);
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
        if (currentTime - lastFlashcardTime >= FLASHCARD_INTERVAL) {
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
}