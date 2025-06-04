package com.example.languagemod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static AudioManager instance;
    
    private Map<String, String> audioMapping = new HashMap<>();
    private boolean audioEnabled = true;
    private float volumeMultiplier = 1.0f;
    private float playbackSpeed = 1.0f;
    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
    
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
        // Audio files are named directly with translation keys, no mapping file needed
        Path audioDir = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/audio/es_mx/");
        
        if (!Files.exists(audioDir)) {
            LOGGER.warn("Audio directory not found at: " + audioDir);
            audioEnabled = false;
            return;
        }
        
        LOGGER.info("Audio directory exists, audio enabled");
        audioEnabled = true;
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
        
        // Check for both OGG and MP3 files
        String audioFileNameOgg = translationKey + ".ogg";
        String audioFileNameMp3 = translationKey + ".mp3";
        Path audioPathOgg = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/audio/es_mx/" + audioFileNameOgg);
        Path audioPathMp3 = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/audio/es_mx/" + audioFileNameMp3);
        
        Path audioPath = null;
        String audioFileName = null;
        
        // Prefer OGG files if available
        if (Files.exists(audioPathOgg)) {
            audioPath = audioPathOgg;
            audioFileName = audioFileNameOgg;
            LOGGER.info("Found OGG audio file: " + audioPath);
        } else if (Files.exists(audioPathMp3)) {
            audioPath = audioPathMp3;
            audioFileName = audioFileNameMp3;
            LOGGER.info("Found MP3 audio file: " + audioPath);
        }
        
        if (audioPath == null || !Files.exists(audioPath)) {
            LOGGER.warn("No audio file found for key: " + translationKey);
            return;
        }
        
        // Make variables final for lambda
        final String finalAudioFileName = audioFileName;
        final Path finalAudioPath = audioPath;
        
        // Play audio asynchronously to avoid freezing the game
        audioExecutor.submit(() -> {
            try {
                LOGGER.info("Playing audio file: " + finalAudioFileName);
                // Apply volume from Minecraft settings
                float masterVolume = Minecraft.getInstance().options.getSoundSourceVolume(net.minecraft.util.SoundCategory.MASTER);
                float adjustedVolume = masterVolume * volumeMultiplier;
                playExternalAudio(finalAudioFileName, adjustedVolume, playbackSpeed);
                
            } catch (Exception e) {
                LOGGER.error("Failed to play audio for key: " + translationKey, e);
            }
        });
    }
    
    /**
     * Play external audio file using system command
     * This is a simple approach for proof of concept
     */
    private void playExternalAudio(String audioFile, float volume, float speed) {
        try {
            Path audioPath = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/audio/es_mx/" + audioFile);
            
            LOGGER.info("Attempting to play audio file: " + audioPath);
            
            if (!Files.exists(audioPath)) {
                LOGGER.error("Audio file not found: " + audioPath);
                return;
            }
            
            // Use simple command line approach
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                try {
                    // Use VLC if installed (most reliable for MP3)
                    String[] vlcPaths = {
                        "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe",
                        "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe"
                    };
                    
                    String vlcPath = null;
                    for (String path : vlcPaths) {
                        if (Files.exists(Paths.get(path))) {
                            vlcPath = path;
                            break;
                        }
                    }
                    
                    if (vlcPath != null) {
                        ProcessBuilder pb = new ProcessBuilder(
                            vlcPath,
                            "--intf", "dummy",     // No interface
                            "--play-and-exit",     // Exit after playing
                            "--no-video",          // Audio only
                            "--gain", String.valueOf(volume),
                            "--rate", String.valueOf(speed),
                            audioPath.toString()
                        );
                        Process process = pb.start();
                        LOGGER.info("Started VLC audio playback for: " + audioFile);
                        return;
                    }
                } catch (Exception e) {
                    LOGGER.warn("VLC not available: " + e.getMessage());
                }
                
                // Use simple PowerShell command that works reliably
                try {
                    String script = String.format(
                        "(New-Object Media.SoundPlayer '%s').PlaySync()",
                        audioPath.toString().replace("\\", "\\\\")
                    );
                    
                    ProcessBuilder pb = new ProcessBuilder(
                        "powershell", "-WindowStyle", "Hidden", "-Command", script
                    );
                    Process process = pb.start();
                    
                    LOGGER.info("Started PowerShell audio playback for: " + audioFile);
                    
                } catch (Exception e) {
                    LOGGER.error("PowerShell audio playback failed", e);
                    
                    // Last resort - use start command
                    try {
                        ProcessBuilder pb = new ProcessBuilder(
                            "cmd", "/c", "start", "/min", "\"\"", "\"" + audioPath.toString() + "\""
                        );
                        Process process = pb.start();
                        LOGGER.info("Started default player for: " + audioFile);
                    } catch (Exception e2) {
                        LOGGER.error("All playback methods failed", e2);
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to play audio: " + audioFile, e);
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
        if (!audioEnabled) return false;
        Path audioPathOgg = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/audio/es_mx/" + translationKey + ".ogg");
        Path audioPathMp3 = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/audio/es_mx/" + translationKey + ".mp3");
        return Files.exists(audioPathOgg) || Files.exists(audioPathMp3);
    }
    
    public void setPlaybackSpeed(float speed) {
        this.playbackSpeed = Math.max(0.25f, Math.min(2.0f, speed));
        LOGGER.info("Playback speed set to: " + this.playbackSpeed);
    }
    
    public float getPlaybackSpeed() {
        return this.playbackSpeed;
    }
    
    public void shutdown() {
        audioExecutor.shutdown();
    }
}