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
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Language Display Mod Setup Starting");
    }
    
    private void doClientStuff(final FMLClientSetupEvent event) {
        LOGGER.info("Language Display Mod Client Setup");
        loadTranslations();
    }
    
    private void loadTranslations() {
        try {
            Path enPath = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/translation_keys/1.16.5/en_us.json");
            Path esPath = Paths.get("C:/Users/benau/forge_language_mod_1.16.5/translation_keys/1.16.5/es_mx.json");
            
            englishTranslations = loadJsonTranslations(enPath.toString());
            spanishTranslations = loadJsonTranslations(esPath.toString());
            
            LOGGER.info("Loaded {} English translations", englishTranslations.size());
            LOGGER.info("Loaded {} Spanish translations", spanishTranslations.size());
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
        return englishTranslations.getOrDefault(key, key);
    }
    
    public String getSpanishTranslation(String key) {
        return spanishTranslations.getOrDefault(key, key);
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
            
            // Player position
            BlockPos playerPos = mc.player.blockPosition();
            fontRenderer.draw(matrixStack, "Block: " + playerPos.getX() + " " + playerPos.getY() + " " + playerPos.getZ(), x, y, color);
            y += lineHeight * 2;
            
            // Biome info
            fontRenderer.draw(matrixStack, "Biome Info:", x, y, color);
            y += lineHeight;
            
            Biome biome = mc.level.getBiome(playerPos);
            ResourceLocation biomeRL = mc.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
            if (biomeRL != null) {
                String biomeNamespace = biomeRL.toString();
                String biomeKey = "biome." + biomeRL.toString().replace(':', '.');
                
                fontRenderer.draw(matrixStack, "Biome: " + biomeNamespace, x, y, color);
                y += lineHeight;
                fontRenderer.draw(matrixStack, "Translation key: " + biomeKey, x, y, color);
                y += lineHeight;
                fontRenderer.draw(matrixStack, "English: " + LanguageDisplayMod.getInstance().getEnglishTranslation(biomeKey), x, y, color);
                y += lineHeight;
                fontRenderer.draw(matrixStack, "Spanish: " + LanguageDisplayMod.getInstance().getSpanishTranslation(biomeKey), x, y, color);
                y += lineHeight * 2;
            }
            
            // Target block info
            RayTraceResult rayTrace = mc.hitResult;
            if (rayTrace != null && rayTrace.getType() == RayTraceResult.Type.BLOCK) {
                fontRenderer.draw(matrixStack, "Target Block Info:", x, y, color);
                y += lineHeight;
                
                BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) rayTrace;
                BlockPos targetPos = blockRayTrace.getBlockPos();
                BlockState blockState = mc.level.getBlockState(targetPos);
                Block block = blockState.getBlock();
                ResourceLocation blockRL = block.getRegistryName();
                
                if (blockRL != null) {
                    String blockNamespace = blockRL.toString();
                    String blockKey = "block." + blockRL.toString().replace(':', '.');
                    
                    fontRenderer.draw(matrixStack, "Targeted Block: " + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ(), x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "Namespaced ID: " + blockNamespace, x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "Translation key: " + blockKey, x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "English: " + LanguageDisplayMod.getInstance().getEnglishTranslation(blockKey), x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "Spanish: " + LanguageDisplayMod.getInstance().getSpanishTranslation(blockKey), x, y, color);
                    y += lineHeight * 2;
                }
            }
            
            // Held item info
            ItemStack heldItem = mc.player.getMainHandItem();
            if (!heldItem.isEmpty()) {
                fontRenderer.draw(matrixStack, "Held Item (in main hand):", x, y, color);
                y += lineHeight;
                
                ResourceLocation itemRL = heldItem.getItem().getRegistryName();
                if (itemRL != null) {
                    String itemNamespace = itemRL.toString();
                    String itemKey = "item." + itemRL.toString().replace(':', '.');
                    
                    fontRenderer.draw(matrixStack, "Held Item: " + itemNamespace, x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "Translation key: " + itemKey, x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "English: " + LanguageDisplayMod.getInstance().getEnglishTranslation(itemKey), x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "Spanish: " + LanguageDisplayMod.getInstance().getSpanishTranslation(itemKey), x, y, color);
                    y += lineHeight * 2;
                }
            }
            
            // Entity under crosshair
            if (rayTrace != null && rayTrace.getType() == RayTraceResult.Type.ENTITY) {
                fontRenderer.draw(matrixStack, "Entity Under Crosshair:", x, y, color);
                y += lineHeight;
                
                EntityRayTraceResult entityRayTrace = (EntityRayTraceResult) rayTrace;
                Entity entity = entityRayTrace.getEntity();
                ResourceLocation entityRL = entity.getType().getRegistryName();
                
                if (entityRL != null) {
                    String entityNamespace = entityRL.toString();
                    String entityKey = "entity." + entityRL.toString().replace(':', '.');
                    
                    fontRenderer.draw(matrixStack, "Entity: " + entityNamespace, x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "Translation key: " + entityKey, x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "English: " + LanguageDisplayMod.getInstance().getEnglishTranslation(entityKey), x, y, color);
                    y += lineHeight;
                    fontRenderer.draw(matrixStack, "Spanish: " + LanguageDisplayMod.getInstance().getSpanishTranslation(entityKey), x, y, color);
                }
            }
        }
    }
}