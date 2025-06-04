package com.example.languagemod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ProgressManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROGRESS_FILE = "C:/Users/benau/forge_language_mod_1.16.5/language_progress.json";
    private static final String BLOCKS_DATA_FILE = "C:/Users/benau/forge_language_mod_1.16.5/translation_keys/1.16.5/minecraft_blocks_mod_data.json";
    
    private final Map<String, ProgressEntry> progressMap = new HashMap<>();
    private final List<String> itemOrder = new ArrayList<>();
    private final Map<String, BlockData> blockDataMap = new HashMap<>();
    private final List<String> skippedItems = new ArrayList<>();
    private String currentTargetItem = null;
    
    public static class ProgressEntry {
        public boolean discovered;
        public long discoveredTime;
        public boolean skipped;
        
        public ProgressEntry() {
            this.discovered = false;
            this.discoveredTime = 0;
            this.skipped = false;
        }
    }
    
    public static class BlockData {
        public String spanish_name;
        public String english_name;
        public String obtainment_tag;
        public String primary_use;
        public String description_spanish;
        public String description_english;
    }
    
    public ProgressManager() {
        loadBlockData();
        loadProgress();
        updateCurrentTarget();
    }
    
    private void loadBlockData() {
        try (java.io.InputStreamReader reader = new java.io.InputStreamReader(
                new java.io.FileInputStream(BLOCKS_DATA_FILE), java.nio.charset.StandardCharsets.UTF_8)) {
            JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
            
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                JsonObject data = entry.getValue().getAsJsonObject();
                
                BlockData blockData = new BlockData();
                blockData.spanish_name = data.get("spanish_name").getAsString();
                blockData.english_name = data.get("english_name").getAsString();
                blockData.obtainment_tag = data.get("obtainment_tag").getAsString();
                blockData.primary_use = data.get("primary_use").getAsString();
                blockData.description_spanish = data.get("description_spanish").getAsString();
                blockData.description_english = data.get("description_english").getAsString();
                
                blockDataMap.put(key, blockData);
                itemOrder.add(key);
            }
            
            LOGGER.info("Loaded {} items from blocks data file", blockDataMap.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load blocks data file", e);
        }
    }
    
    private void loadProgress() {
        File file = new File(PROGRESS_FILE);
        if (!file.exists()) {
            // Initialize progress for all items
            for (String key : itemOrder) {
                progressMap.put(key, new ProgressEntry());
            }
            saveProgress();
            return;
        }
        
        try (java.io.InputStreamReader reader = new java.io.InputStreamReader(
                new java.io.FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
            
            // Load progress entries
            if (jsonObject.has("progress")) {
                JsonObject progress = jsonObject.getAsJsonObject("progress");
                for (Map.Entry<String, JsonElement> entry : progress.entrySet()) {
                    String key = entry.getKey();
                    JsonObject data = entry.getValue().getAsJsonObject();
                    
                    ProgressEntry progressEntry = new ProgressEntry();
                    progressEntry.discovered = data.get("discovered").getAsBoolean();
                    progressEntry.discoveredTime = data.get("discoveredTime").getAsLong();
                    progressEntry.skipped = data.get("skipped").getAsBoolean();
                    
                    progressMap.put(key, progressEntry);
                    
                    if (progressEntry.skipped && !progressEntry.discovered) {
                        skippedItems.add(key);
                    }
                }
            }
            
            // Ensure all items have progress entries
            for (String key : itemOrder) {
                if (!progressMap.containsKey(key)) {
                    progressMap.put(key, new ProgressEntry());
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
                data.addProperty("skipped", entry.getValue().skipped);
                progress.add(entry.getKey(), data);
            }
            
            root.add("progress", progress);
            
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
            entry.skipped = false;
            skippedItems.remove(key);
            saveProgress();
            updateCurrentTarget();
            LOGGER.info("Marked {} as discovered", key);
            
            // Log progress statistics
            int discovered = getDiscoveredCount();
            int total = getTotalItemCount();
            LOGGER.info("Progress: {}/{} items discovered ({}%)", discovered, total, (discovered * 100) / total);
        } else if (entry == null) {
            LOGGER.warn("No progress entry found for key: {}", key);
        } else {
            LOGGER.info("Item {} was already discovered", key);
        }
    }
    
    public void markBiomeDiscovered(String biomeKey) {
        // For biomes, we'll track them separately
        String key = "biome." + biomeKey.replace(":", ".");
        ProgressEntry entry = progressMap.computeIfAbsent(key, k -> new ProgressEntry());
        if (!entry.discovered) {
            entry.discovered = true;
            entry.discoveredTime = System.currentTimeMillis();
            saveProgress();
            LOGGER.info("Marked biome {} as discovered", key);
        }
    }
    
    public void skipCurrentItem() {
        if (currentTargetItem != null) {
            ProgressEntry entry = progressMap.get(currentTargetItem);
            if (entry != null && !entry.discovered) {
                entry.skipped = true;
                if (!skippedItems.contains(currentTargetItem)) {
                    skippedItems.add(currentTargetItem);
                }
                saveProgress();
                updateCurrentTarget();
                LOGGER.info("Skipped item: {}", currentTargetItem);
            }
        }
    }
    
    private void updateCurrentTarget() {
        currentTargetItem = null;
        
        // First, try to find the next undiscovered item in order
        for (String key : itemOrder) {
            ProgressEntry entry = progressMap.get(key);
            if (entry != null && !entry.discovered && !entry.skipped) {
                currentTargetItem = key;
                break;
            }
        }
        
        // If all non-skipped items are discovered, work on skipped items
        if (currentTargetItem == null && !skippedItems.isEmpty()) {
            for (String key : skippedItems) {
                ProgressEntry entry = progressMap.get(key);
                if (entry != null && !entry.discovered) {
                    currentTargetItem = key;
                    break;
                }
            }
        }
    }
    
    public String getCurrentTargetItem() {
        return currentTargetItem;
    }
    
    public BlockData getCurrentTargetData() {
        if (currentTargetItem != null) {
            return blockDataMap.get(currentTargetItem);
        }
        return null;
    }
    
    public BlockData getBlockData(String key) {
        return blockDataMap.get(key);
    }
    
    public String getCurrentTargetHint() {
        BlockData data = getCurrentTargetData();
        if (data != null) {
            return data.description_english;
        }
        return "No current target item";
    }
    
    public int getDiscoveredCount() {
        return (int) progressMap.values().stream()
                .filter(entry -> entry.discovered)
                .count();
    }
    
    public int getTotalItemCount() {
        return itemOrder.size();
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
        ProgressEntry entry = progressMap.get(key);
        return entry != null && entry.discovered;
    }
}