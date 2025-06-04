package com.example.languagemod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Mod("languagemod")
public class LanguageDisplayMod {
    private static final Logger LOGGER = LogManager.getLogger();
    private static LanguageDisplayMod instance;
    
    private Map<String, String> englishTranslations = new HashMap<>();
    private Map<String, String> spanishTranslations = new HashMap<>();
    
    public LanguageDisplayMod() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new OverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Language Display Mod Setup Starting");
    }
    
    private void doClientStuff(final FMLClientSetupEvent event) {
        LOGGER.info("Language Display Mod Client Setup");
        loadTranslations();
        
        // Initialize audio system
        AudioManager.getInstance();
        
        // Register keybindings
        KeyInputHandler.register();
    }
    
    private void loadTranslations() {
        try {
            Path enPath = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/translation_keys/1.16.5/en_us.json");
            Path esPath = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/translation_keys/1.16.5/es_mx.json");
            
            englishTranslations = loadJsonTranslations(enPath.toString());
            spanishTranslations = loadJsonTranslations(esPath.toString());
            
            LOGGER.info("Loaded {} English translations", englishTranslations.size());
            LOGGER.info("Loaded {} Spanish translations", spanishTranslations.size());
            
            // Test a few translations
            LOGGER.info("Test - Stone Sword EN: " + englishTranslations.get("item.minecraft.stone_sword"));
            LOGGER.info("Test - Stone Sword ES: " + spanishTranslations.get("item.minecraft.stone_sword"));
        } catch (Exception e) {
            LOGGER.error("Failed to load translations", e);
        }
    }
    
    private Map<String, String> loadJsonTranslations(String filePath) {
        Map<String, String> translations = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            json.entrySet().forEach(entry -> translations.put(entry.getKey(), entry.getValue().getAsString()));
        } catch (IOException e) {
            LOGGER.error("Failed to load translations from: " + filePath, e);
        }
        return translations;
    }
    
    public String getEnglishTranslation(String key) {
        String translation = englishTranslations.get(key);
        if (translation == null) {
            LOGGER.warn("Missing English translation for key: " + key);
            return key;
        }
        return translation;
    }
    
    public String getSpanishTranslation(String key) {
        String translation = spanishTranslations.get(key);
        if (translation == null) {
            LOGGER.warn("Missing Spanish translation for key: " + key);
            return key;
        }
        return translation;
    }
    
    public static LanguageDisplayMod getInstance() {
        return instance;
    }
    
    public static class OverlayRenderer {
        @SubscribeEvent
        public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
            if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
                return;
            }
            
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                return;
            }
            
            MatrixStack matrixStack = event.getMatrixStack();
            FontRenderer fontRenderer = mc.font;
            
            int x = 5;
            int y = 5;
            int lineHeight = 10;
            int color = 0xFFFFFF;
            
            // Start without header for cleaner look
            y += lineHeight;
            
            // Current location
            BlockPos playerPos = mc.player.blockPosition();
            fontRenderer.draw(matrixStack, "Position: " + playerPos.getX() + ", " + playerPos.getY() + ", " + playerPos.getZ(), x, y, color);
            y += lineHeight * 2;
            
            // Biome info
            Biome biome = mc.level.getBiome(playerPos);
            ResourceLocation biomeRL = mc.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
            if (biomeRL != null) {
                String biomeKey = "biome." + biomeRL.toString().replace(':', '.');
                
                fontRenderer.draw(matrixStack, "Biome:", x, y, 0xFFFF00);
                y += lineHeight;
                fontRenderer.draw(matrixStack, "  EN: " + LanguageDisplayMod.getInstance().getEnglishTranslation(biomeKey), x, y, color);
                y += lineHeight;
                fontRenderer.draw(matrixStack, "  ES: " + LanguageDisplayMod.getInstance().getSpanishTranslation(biomeKey), x, y, color);
                y += lineHeight * 2;
            }
            
            // Target block info
            RayTraceResult rayTrace = mc.hitResult;
            if (rayTrace != null && rayTrace.getType() == RayTraceResult.Type.BLOCK) {
                BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) rayTrace;
                BlockState blockState = mc.level.getBlockState(blockRayTrace.getBlockPos());
                Block block = blockState.getBlock();
                ResourceLocation blockRL = block.getRegistryName();
                
                if (blockRL != null) {
                    String blockKey = "block." + blockRL.toString().replace(':', '.');
                    
                    fontRenderer.draw(matrixStack, "Looking At:", x, y, 0xFFFF00);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "  EN: " + LanguageDisplayMod.getInstance().getEnglishTranslation(blockKey), x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "  ES: " + LanguageDisplayMod.getInstance().getSpanishTranslation(blockKey), x, y, color);
                    y += lineHeight * 2;
                }
            }
            
            // Held item info
            ItemStack heldItem = mc.player.getMainHandItem();
            if (!heldItem.isEmpty()) {
                String translationKey = heldItem.getItem().getDescriptionId();
                
                fontRenderer.draw(matrixStack, "Holding:", x, y, 0xFFFF00);
                y += lineHeight;
                fontRenderer.draw(matrixStack, "  EN: " + LanguageDisplayMod.getInstance().getEnglishTranslation(translationKey), x, y, color);
                y += lineHeight;
                fontRenderer.draw(matrixStack, "  ES: " + LanguageDisplayMod.getInstance().getSpanishTranslation(translationKey), x, y, color);
                y += lineHeight * 2;
            }
            
            // Entity under crosshair
            if (rayTrace != null && rayTrace.getType() == RayTraceResult.Type.ENTITY) {
                EntityRayTraceResult entityRayTrace = (EntityRayTraceResult) rayTrace;
                Entity entity = entityRayTrace.getEntity();
                ResourceLocation entityRL = entity.getType().getRegistryName();
                
                if (entityRL != null) {
                    String entityKey = "entity." + entityRL.toString().replace(':', '.');
                    
                    fontRenderer.draw(matrixStack, "Looking At:", x, y, 0xFFFF00);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "  EN: " + LanguageDisplayMod.getInstance().getEnglishTranslation(entityKey), x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "  ES: " + LanguageDisplayMod.getInstance().getSpanishTranslation(entityKey), x, y, color);
                }
            }
        }
    }
}