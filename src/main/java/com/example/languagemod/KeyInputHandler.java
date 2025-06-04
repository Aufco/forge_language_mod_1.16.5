package com.example.languagemod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static ProgressManager progressManager;
    
    public static final KeyBinding SPEAK_KEY = new KeyBinding(
        "key.languagemod.speak",
        GLFW.GLFW_KEY_F,
        "key.categories.languagemod"
    );
    
    public static void register() {
        ClientRegistry.registerKeyBinding(SPEAK_KEY);
    }
    
    public static void setProgressManager(ProgressManager manager) {
        progressManager = manager;
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (SPEAK_KEY.consumeClick()) {
            LOGGER.info("F key pressed - handling speak with priority system");
            handleSpeakKey();
        }
    }
    
    private void handleSpeakKey() {
        LOGGER.info("handleSpeakKey() called");
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            LOGGER.warn("Player or level is null, cannot handle speak key");
            return;
        }
        
        AudioManager audioManager = AudioManager.getInstance();
        if (!audioManager.isAudioEnabled()) {
            LOGGER.warn("Audio is disabled in AudioManager");
            return;
        }
        
        // Priority system:
        // 1. Check what player is looking at (block or entity)
        RayTraceResult rayTrace = mc.hitResult;
        if (rayTrace != null && rayTrace.getType() != RayTraceResult.Type.MISS) {
            String translationKey = null;
            
            if (rayTrace.getType() == RayTraceResult.Type.BLOCK) {
                translationKey = getBlockTranslationKey((BlockRayTraceResult) rayTrace);
                LOGGER.info("Looking at block, key: " + translationKey);
                
                if (translationKey != null) {
                    audioManager.playAudio(translationKey);
                    markAsDiscovered(translationKey);
                    return;
                }
            } else if (rayTrace.getType() == RayTraceResult.Type.ENTITY) {
                translationKey = getEntityTranslationKey((EntityRayTraceResult) rayTrace);
                LOGGER.info("Looking at entity, key: " + translationKey);
                
                if (translationKey != null) {
                    audioManager.playAudio(translationKey);
                    // Don't mark entities as discovered - no achievements for entities
                    return;
                }
            }
        }
        
        // 2. Check held item
        ItemStack heldItem = mc.player.getMainHandItem();
        if (!heldItem.isEmpty()) {
            String translationKey = getItemTranslationKey(heldItem);
            LOGGER.info("Holding item, key: " + translationKey);
            
            if (translationKey != null) {
                audioManager.playAudio(translationKey);
                markAsDiscovered(translationKey);
                return;
            }
        }
        
        // 3. Fall back to biome
        BlockPos playerPos = mc.player.blockPosition();
        Biome biome = mc.level.getBiome(playerPos);
        ResourceLocation biomeRL = mc.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
        
        if (biomeRL != null) {
            String biomeKey = "biome." + biomeRL.toString().replace(':', '.');
            LOGGER.info("In biome, key: " + biomeKey);
            
            audioManager.playAudio(biomeKey);
            
            // Mark biome as discovered
            if (progressManager != null) {
                boolean wasNewDiscovery = !progressManager.isBiomeDiscovered(biomeRL.toString());
                progressManager.markBiomeDiscovered(biomeRL.toString());
                
                if (wasNewDiscovery) {
                    String biomeName = LanguageDisplayMod.getInstance().getEnglishTranslation(biomeKey);
                    
                    mc.player.displayClientMessage(
                        new net.minecraft.util.text.StringTextComponent("¡Bioma Descubierto! ")
                            .withStyle(net.minecraft.util.text.TextFormatting.AQUA, net.minecraft.util.text.TextFormatting.BOLD)
                            .append(new net.minecraft.util.text.StringTextComponent("Has explorado: ")
                                .withStyle(net.minecraft.util.text.TextFormatting.YELLOW))
                            .append(new net.minecraft.util.text.StringTextComponent(biomeName)
                                .withStyle(net.minecraft.util.text.TextFormatting.WHITE))
                            .append(new net.minecraft.util.text.StringTextComponent(" (" + progressManager.getDiscoveredBiomeCount() + " biomes)")
                                .withStyle(net.minecraft.util.text.TextFormatting.GRAY)),
                        false
                    );
                    
                    LOGGER.info("Achievement: Discovered biome " + biomeName + " (" + progressManager.getDiscoveredBiomeCount() + " biomes discovered)");
                }
            }
        }
    }
    
    private void markAsDiscovered(String translationKey) {
        if (progressManager != null) {
            boolean wasNewDiscovery = !progressManager.isItemDiscovered(translationKey);
            progressManager.markItemDiscovered(translationKey);
            
            if (wasNewDiscovery) {
                ProgressManager.BlockData data = progressManager.getBlockData(translationKey);
                String itemName = data != null ? data.english_name : translationKey;
                
                Minecraft mc = Minecraft.getInstance();
                mc.player.displayClientMessage(
                    new net.minecraft.util.text.StringTextComponent("¡Descubrimiento! ")
                        .withStyle(net.minecraft.util.text.TextFormatting.GOLD, net.minecraft.util.text.TextFormatting.BOLD)
                        .append(new net.minecraft.util.text.StringTextComponent("Has aprendido: ")
                            .withStyle(net.minecraft.util.text.TextFormatting.YELLOW))
                        .append(new net.minecraft.util.text.StringTextComponent(itemName)
                            .withStyle(net.minecraft.util.text.TextFormatting.WHITE))
                        .append(new net.minecraft.util.text.StringTextComponent(" (" + progressManager.getDiscoveredCount() + "/" + progressManager.getTotalItemCount() + ")")
                            .withStyle(net.minecraft.util.text.TextFormatting.GRAY)),
                    false
                );
                
                LOGGER.info("Achievement: Discovered " + itemName + " (" + progressManager.getDiscoveredCount() + "/" + progressManager.getTotalItemCount() + ")");
            }
        }
    }
    
    private void handleSpeakItemKey() {
        // Old method body was removed - handled by new handleSpeakKey
    }
    
    private void handleSpeakBiomeKey() {
        // Old method body was removed - handled by new handleSpeakKey
    }
    
    private void handleSpeakTargetKey() {
        // Old method body was removed - handled by new handleSpeakKey
    }
    
    /**
     * Get the translation key for whatever the player is currently looking at
     */
    private String getCurrentTargetTranslationKey() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return null;
        }
        
        // Check what the player is looking at
        RayTraceResult rayTrace = mc.hitResult;
        LOGGER.info("RayTrace result: " + (rayTrace != null ? rayTrace.getType() : "null"));
        
        if (rayTrace != null) {
            switch (rayTrace.getType()) {
                case BLOCK:
                    String blockKey = getBlockTranslationKey((BlockRayTraceResult) rayTrace);
                    LOGGER.info("Block translation key: " + blockKey);
                    return blockKey;
                case ENTITY:
                    String entityKey = getEntityTranslationKey((EntityRayTraceResult) rayTrace);
                    LOGGER.info("Entity translation key: " + entityKey);
                    return entityKey;
                case MISS:
                    LOGGER.info("Looking at nothing (MISS), checking held item");
                    break;
                default:
                    LOGGER.info("Unknown rayTrace type: " + rayTrace.getType());
                    break;
            }
        }
        
        // If not looking at anything specific, try held item
        ItemStack heldItem = mc.player.getMainHandItem();
        if (!heldItem.isEmpty()) {
            String itemKey = getItemTranslationKey(heldItem);
            LOGGER.info("Held item translation key: " + itemKey);
            return itemKey;
        }
        
        LOGGER.info("No held item found");
        return null;
    }
    
    private String getBlockTranslationKey(BlockRayTraceResult blockRayTrace) {
        try {
            Minecraft mc = Minecraft.getInstance();
            BlockPos targetPos = blockRayTrace.getBlockPos();
            BlockState blockState = mc.level.getBlockState(targetPos);
            Block block = blockState.getBlock();
            ResourceLocation blockRL = block.getRegistryName();
            
            LOGGER.info("Block at " + targetPos + ": " + (blockRL != null ? blockRL.toString() : "null registry name"));
            
            if (blockRL != null) {
                String blockKey = "block." + blockRL.toString().replace(':', '.');
                LOGGER.info("Generated block translation key: " + blockKey);
                return blockKey;
            } else {
                LOGGER.warn("Block registry name is null for block: " + block.getClass().getSimpleName());
            }
        } catch (Exception e) {
            LOGGER.error("Error getting block translation key", e);
        }
        return null;
    }
    
    private String getEntityTranslationKey(EntityRayTraceResult entityRayTrace) {
        try {
            Entity entity = entityRayTrace.getEntity();
            ResourceLocation entityRL = entity.getType().getRegistryName();
            
            LOGGER.info("Entity: " + entity.getClass().getSimpleName() + ", Registry: " + (entityRL != null ? entityRL.toString() : "null"));
            
            if (entityRL != null) {
                String entityKey = "entity." + entityRL.toString().replace(':', '.');
                LOGGER.info("Generated entity translation key: " + entityKey);
                return entityKey;
            } else {
                LOGGER.warn("Entity registry name is null for entity: " + entity.getClass().getSimpleName());
            }
        } catch (Exception e) {
            LOGGER.error("Error getting entity translation key", e);
        }
        return null;
    }
    
    private String getItemTranslationKey(ItemStack itemStack) {
        try {
            // Use the item's built-in translation key method
            String translationKey = itemStack.getItem().getDescriptionId();
            ResourceLocation itemRL = itemStack.getItem().getRegistryName();
            
            LOGGER.info("Item: " + itemStack.getDisplayName().getString() + 
                       ", Registry: " + (itemRL != null ? itemRL.toString() : "null") + 
                       ", Translation key: " + translationKey);
            
            return translationKey;
        } catch (Exception e) {
            LOGGER.error("Error getting item translation key", e);
        }
        return null;
    }
}