package com.example.languagemod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ModSounds {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "languagemod");
    
    // Map to store registered sound events
    private static final Map<String, RegistryObject<SoundEvent>> registeredSounds = new HashMap<>();
    
    // Register all sounds during class initialization
    static {
        registerAllSoundsFromJson();
    }
    
    /**
     * Load and register all sounds from sounds.json
     */
    private static void registerAllSoundsFromJson() {
        try {
            // Load sounds.json from resources
            InputStream inputStream = ModSounds.class.getResourceAsStream("/assets/languagemod/sounds.json");
            if (inputStream == null) {
                LOGGER.error("Could not find sounds.json!");
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                Gson gson = new Gson();
                JsonObject soundsJson = gson.fromJson(reader, JsonObject.class);
                
                // Register each sound
                for (Map.Entry<String, com.google.gson.JsonElement> entry : soundsJson.entrySet()) {
                    String soundKey = entry.getKey();
                    // Convert sound key to translation key
                    // e.g., "es_mx.entity.minecraft.cat" -> "entity.minecraft.cat"
                    if (soundKey.startsWith("es_mx.")) {
                        String translationKey = soundKey.substring(6); // Remove "es_mx." prefix
                        registerSound(translationKey);
                    }
                }
                
                LOGGER.info("Registered {} sound events from sounds.json", registeredSounds.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to register sounds from sounds.json", e);
        }
    }
    
    /**
     * Register a sound event for a translation key
     * @param translationKey The Minecraft translation key (e.g., "block.minecraft.stone")
     * @return The registered sound event
     */
    private static RegistryObject<SoundEvent> registerSound(String translationKey) {
        if (translationKey == null || translationKey.isEmpty()) {
            return null;
        }
        
        // Check if already registered
        if (registeredSounds.containsKey(translationKey)) {
            return registeredSounds.get(translationKey);
        }
        
        // Convert translation key to sound name (e.g., "block.minecraft.stone" -> "es_mx.block.minecraft.stone")
        String soundName = "es_mx." + translationKey;
        ResourceLocation soundLocation = new ResourceLocation("languagemod", soundName);
        
        // Register the sound event
        RegistryObject<SoundEvent> soundEvent = SOUNDS.register(soundName, 
            () -> new SoundEvent(soundLocation));
        
        registeredSounds.put(translationKey, soundEvent);
        
        return soundEvent;
    }
    
    /**
     * Get a registered sound event for a translation key
     * @param translationKey The translation key
     * @return The sound event or null if not registered
     */
    public static SoundEvent getSoundEvent(String translationKey) {
        RegistryObject<SoundEvent> registryObject = registeredSounds.get(translationKey);
        return registryObject != null ? registryObject.get() : null;
    }
    
    /**
     * This method is kept for compatibility but doesn't do anything since sounds are pre-registered
     */
    public static void registerAllSounds(Map<String, String> translationKeys) {
        LOGGER.info("All {} sounds were pre-registered during class initialization", registeredSounds.size());
    }
    
    public static int getRegisteredSoundCount() {
        return registeredSounds.size();
    }
}