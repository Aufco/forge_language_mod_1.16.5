package com.example.languagemod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static AudioManager instance;
    
    private Map<String, String> audioMapping = new HashMap<>();
    private boolean audioEnabled = true;
    
    public AudioManager() {
        instance = this;
        loadAudioMapping();
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    private void loadAudioMapping() {
        try {
            Path mappingPath = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/audio/es_mx/audio_mapping.json");
            
            if (!Files.exists(mappingPath)) {
                LOGGER.warn("Audio mapping file not found at: " + mappingPath);
                audioEnabled = false;
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(mappingPath.toString()))) {
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                json.entrySet().forEach(entry -> 
                    audioMapping.put(entry.getKey(), entry.getValue().getAsString())
                );
                
                LOGGER.info("Loaded {} audio mappings", audioMapping.size());
                audioEnabled = true;
                
            } catch (IOException e) {
                LOGGER.error("Failed to load audio mapping file", e);
                audioEnabled = false;
            }
            
        } catch (Exception e) {
            LOGGER.error("Error setting up audio mapping", e);
            audioEnabled = false;
        }
    }
    
    /**
     * Play audio for a given translation key
     * @param translationKey The Minecraft translation key (e.g., "block.minecraft.stone")
     */
    public void playAudio(String translationKey) {
        LOGGER.info("playAudio() called with key: " + translationKey);
        
        if (!audioEnabled) {
            LOGGER.warn("Audio is disabled, cannot play audio");
            return;
        }
        
        if (translationKey == null || translationKey.isEmpty()) {
            LOGGER.warn("Translation key is null or empty");
            return;
        }
        
        String audioFile = audioMapping.get(translationKey);
        LOGGER.info("Audio file mapped to: " + audioFile);
        
        if (audioFile == null) {
            LOGGER.warn("No audio file found for translation key: " + translationKey);
            LOGGER.info("Available keys in mapping: " + audioMapping.size());
            return;
        }
        
        // Check if the audio file actually exists
        Path audioPath = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/audio/es_mx/" + audioFile);
        if (!Files.exists(audioPath)) {
            LOGGER.error("Audio file does not exist: " + audioPath);
            LOGGER.error("This means the mapping has an entry but the file is missing!");
            return;
        }
        
        try {
            LOGGER.info("Calling playExternalAudio with file: " + audioFile);
            // For now, we'll use a simple approach with external audio playback
            // since integrating custom audio files into Minecraft's sound system
            // requires more complex resource pack setup
            playExternalAudio(audioFile);
            
        } catch (Exception e) {
            LOGGER.error("Failed to play audio for key: " + translationKey, e);
        }
    }
    
    /**
     * Play external audio file using system command
     * This is a simple approach for proof of concept
     */
    private void playExternalAudio(String audioFile) {
        try {
            Path audioPath = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/audio/es_mx/" + audioFile);
            
            LOGGER.info("Attempting to play audio file: " + audioPath);
            
            if (!Files.exists(audioPath)) {
                LOGGER.error("Audio file not found: " + audioPath);
                return;
            }
            
            // Use different commands based on OS
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                // Windows - use multiple fallback approaches
                boolean success = false;
                
                // Try 1: Use PowerShell with Windows Media Foundation
                try {
                    pb = new ProcessBuilder("powershell", "-Command", 
                        "Add-Type -AssemblyName presentationCore; " +
                        "$player = New-Object System.Windows.Media.MediaPlayer; " +
                        "$player.Open([uri]'" + audioPath.toString() + "'); " +
                        "$player.Play(); Start-Sleep -Seconds 2; $player.Close()");
                    Process process = pb.start();
                    LOGGER.info("Started PowerShell MediaPlayer for: " + audioFile);
                    return;
                } catch (Exception e) {
                    LOGGER.warn("PowerShell MediaPlayer failed: " + e.getMessage());
                }
                
                // Try 2: Use built-in Windows start command with default player
                try {
                    pb = new ProcessBuilder("cmd", "/c", "start", "/B", "\"\"", audioPath.toString());
                    Process process = pb.start();
                    LOGGER.info("Started default Windows player for: " + audioFile);
                    return;
                } catch (Exception e) {
                    LOGGER.warn("Default Windows player failed: " + e.getMessage());
                }
                
                // Try 3: Use PowerShell with simpler SoundPlayer (WAV only)
                try {
                    pb = new ProcessBuilder("powershell", "-Command", 
                        "$player = New-Object System.Media.SoundPlayer('" + audioPath.toString() + "'); " +
                        "$player.PlaySync()");
                    Process process = pb.start();
                    LOGGER.info("Started PowerShell SoundPlayer for: " + audioFile);
                    return;
                } catch (Exception e) {
                    LOGGER.warn("PowerShell SoundPlayer failed: " + e.getMessage());
                }
                
                LOGGER.error("All Windows audio playback methods failed");
                return;
                
            } else if (os.contains("mac")) {
                // macOS - use afplay
                pb = new ProcessBuilder("afplay", audioPath.toString());
            } else {
                // Linux - try multiple players
                if (Files.exists(Paths.get("/usr/bin/aplay"))) {
                    pb = new ProcessBuilder("aplay", audioPath.toString());
                } else if (Files.exists(Paths.get("/usr/bin/paplay"))) {
                    pb = new ProcessBuilder("paplay", audioPath.toString());
                } else {
                    LOGGER.warn("No audio player found on Linux system");
                    return;
                }
            }
            
            // Start the process but don't wait for it to complete
            Process process = pb.start();
            LOGGER.info("Successfully started audio playback process for: " + audioFile);
            
        } catch (Exception e) {
            LOGGER.error("Failed to play external audio: " + audioFile, e);
        }
    }
    
    /**
     * Alternative method using Minecraft's sound system (requires sound registration)
     * This would be the proper way but requires more setup
     */
    private void playMinecraftSound(String soundName) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            
            // This approach would require registering custom sounds in sounds.json
            // and having the audio files in the proper resource pack structure
            ResourceLocation soundLocation = new ResourceLocation("languagemod", soundName);
            SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(soundLocation);
            
            if (soundEvent != null) {
                mc.getSoundManager().play(SimpleSound.forUI(soundEvent, 1.0f));
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to play Minecraft sound: " + soundName, e);
        }
    }
    
    public boolean isAudioEnabled() {
        return audioEnabled;
    }
    
    public void setAudioEnabled(boolean enabled) {
        this.audioEnabled = enabled;
    }
    
    public boolean hasAudioForKey(String translationKey) {
        return audioEnabled && audioMapping.containsKey(translationKey);
    }
}