package com.example.languagemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AudioManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static AudioManager instance;
    
    private boolean audioEnabled = true;
    private float volumeMultiplier = 1.0f;
    private float playbackSpeed = 1.0f; // Note: Minecraft doesn't support variable playback speed
    
    public AudioManager() {
        instance = this;
        LOGGER.info("AudioManager initialized for Minecraft sound system");
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * Play audio for a given translation key using Minecraft's sound system
     * @param translationKey The Minecraft translation key (e.g., "block.minecraft.stone")
     */
    public void playAudio(String translationKey) {
        if (!audioEnabled) {
            LOGGER.debug("Audio is disabled");
            return;
        }
        
        if (translationKey == null || translationKey.isEmpty()) {
            LOGGER.warn("Translation key is null or empty");
            return;
        }
        
        LOGGER.info("Playing audio for key: " + translationKey);
        
        // Get the registered sound event from ModSounds
        SoundEvent soundEvent = ModSounds.getSoundEvent(translationKey);
        if (soundEvent == null) {
            LOGGER.debug("No sound event registered for key: {}", translationKey);
            return;
        }
        
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.getSoundManager() == null) {
                LOGGER.warn("Cannot play sound - player or sound manager not available");
                return;
            }
            
            // Get volume settings from Minecraft
            float masterVolume = mc.options.getSoundSourceVolume(SoundCategory.MASTER);
            float blocksVolume = mc.options.getSoundSourceVolume(SoundCategory.BLOCKS);
            float recordsVolume = mc.options.getSoundSourceVolume(SoundCategory.RECORDS);
            
            LOGGER.info("Volume settings - Master: {}, Blocks: {}, Records: {}", masterVolume, blocksVolume, recordsVolume);
            
            // Try multiple playback methods
            LOGGER.info("Attempting multiple playback methods for sound: {}", translationKey);
            
            // Method 1: Play at player position
            mc.level.playSound(mc.player, mc.player.blockPosition(), soundEvent, 
                SoundCategory.MASTER, 1.0f, 1.0f);
            LOGGER.info("Method 1: Played using world.playSound at player position");
            
            // Method 2: Play local sound
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), 
                soundEvent, SoundCategory.MASTER, 1.0f, 1.0f, false);
            LOGGER.info("Method 2: Played using playLocalSound");
            
            // Method 3: SimpleSound with different approach
            SimpleSound sound = SimpleSound.forUI(soundEvent, 1.0f, 1.0f);
            mc.getSoundManager().play(sound);
            LOGGER.info("Method 3: Played using SimpleSound.forUI");
            
            LOGGER.info("Sound event location: {}", soundEvent.getLocation());
            
        } catch (Exception e) {
            LOGGER.error("Failed to play audio for key: " + translationKey, e);
        }
    }
    
    /**
     * Check if audio is available for a given translation key
     * This checks if the sound event is registered
     */
    public boolean hasAudioForKey(String translationKey) {
        if (!audioEnabled || translationKey == null) {
            return false;
        }
        
        SoundEvent soundEvent = ModSounds.getSoundEvent(translationKey);
        return soundEvent != null;
    }
    
    /**
     * Register all possible sound events based on available translation keys
     * This should be called during mod setup with all known translation keys
     */
    public void registerAllSounds(java.util.Map<String, String> translationKeys) {
        ModSounds.registerAllSounds(translationKeys);
    }
    
    /**
     * Pre-register common sound events that we know we'll need
     * This is now handled automatically during ModSounds static initialization
     */
    public void preregisterCommonSounds() {
        LOGGER.info("Sound pre-registration is handled automatically during class loading");
        LOGGER.info("Currently have {} sound events registered", getRegisteredSoundCount());
    }
    
    public boolean isAudioEnabled() {
        return audioEnabled;
    }
    
    public void setAudioEnabled(boolean enabled) {
        this.audioEnabled = enabled;
        LOGGER.info("Audio enabled: " + enabled);
    }
    
    public void setPlaybackSpeed(float speed) {
        // Note: Minecraft's sound system doesn't support variable playback speed
        // This is kept for API compatibility but has no effect
        this.playbackSpeed = Math.max(0.25f, Math.min(2.0f, speed));
        LOGGER.info("Playback speed set to: {} (Note: Minecraft doesn't support variable speed)", this.playbackSpeed);
    }
    
    public float getPlaybackSpeed() {
        return this.playbackSpeed;
    }
    
    /**
     * Get the number of registered sound events
     */
    public int getRegisteredSoundCount() {
        return ModSounds.getRegisteredSoundCount();
    }
    
    /**
     * Test if a specific sound can be played (for debugging)
     */
    public void testSound(String translationKey) {
        LOGGER.info("Testing sound for key: " + translationKey);
        playAudio(translationKey);
    }
    
    /**
     * Cleanup method (called when mod shuts down)
     */
    public void shutdown() {
        LOGGER.info("AudioManager shutdown complete");
    }
}