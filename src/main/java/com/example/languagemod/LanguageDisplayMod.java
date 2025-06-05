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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Mod("languagemod")
public class LanguageDisplayMod {
    private static final Logger LOGGER = LogManager.getLogger();
    private static LanguageDisplayMod instance;
    
    private Map<String, String> englishTranslations = new HashMap<>();
    private Map<String, String> spanishTranslations = new HashMap<>();
    private ProgressManager progressManager;
    
    public LanguageDisplayMod() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        
        // Register the sound events deferred register
        ModSounds.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new OverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        MinecraftForge.EVENT_BUS.register(LanguageCommands.class);
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Language Display Mod Setup Starting");
    }
    
    private void doClientStuff(final FMLClientSetupEvent event) {
        LOGGER.info("Language Display Mod Client Setup");
        loadTranslations();
        
        // Initialize progress manager
        progressManager = new ProgressManager();
        KeyInputHandler.setProgressManager(progressManager);
        LanguageCommands.setProgressManager(progressManager);
        
        // Initialize audio system
        AudioManager audioManager = AudioManager.getInstance();
        audioManager.registerAllSounds(englishTranslations);
        
        // Log how many sounds were registered
        LOGGER.info("Total sounds registered: {}", ModSounds.getRegisteredSoundCount());
        
        // Register keybindings
        KeyInputHandler.register();
    }
    
    private void loadTranslations() {
        try {
            englishTranslations = loadJsonTranslationsFromResource("/assets/languagemod/lang/en_us.json");
            spanishTranslations = loadJsonTranslationsFromResource("/assets/languagemod/lang/es_mx.json");
            
            LOGGER.info("Loaded {} English translations", englishTranslations.size());
            LOGGER.info("Loaded {} Spanish translations", spanishTranslations.size());
            
            // Test a few translations
            LOGGER.info("Test - Stone Sword EN: " + englishTranslations.get("item.minecraft.stone_sword"));
            LOGGER.info("Test - Stone Sword ES: " + spanishTranslations.get("item.minecraft.stone_sword"));
        } catch (Exception e) {
            LOGGER.error("Failed to load translations", e);
        }
    }
    
    private Map<String, String> loadJsonTranslationsFromResource(String resourcePath) {
        Map<String, String> translations = new HashMap<>();
        try {
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                LOGGER.error("Could not find resource: " + resourcePath);
                return translations;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                json.entrySet().forEach(entry -> translations.put(entry.getKey(), entry.getValue().getAsString()));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load translations from resource: " + resourcePath, e);
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
    
    public ProgressManager getProgressManager() {
        return progressManager;
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && progressManager != null) {
            progressManager.checkAndShowFlashcard();
        }
    }
    
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (progressManager != null) {
            // Always show startup flashcard regardless of welcome message setting
            if (progressManager.isWelcomeMessageEnabled()) {
                // Send welcome messages with slight delay to ensure player is ready
                Minecraft.getInstance().execute(() -> {
                    event.getPlayer().displayClientMessage(
                        new StringTextComponent("=== Language Learning Mod ===")
                            .withStyle(TextFormatting.GOLD, TextFormatting.BOLD),
                        false
                    );
                    event.getPlayer().displayClientMessage(
                        new StringTextComponent("Press F while looking at objects to learn Spanish!")
                            .withStyle(TextFormatting.YELLOW),
                        false
                    );
                    event.getPlayer().displayClientMessage(
                        new StringTextComponent("Type /languagehelp for commands")
                            .withStyle(TextFormatting.GREEN),
                        false
                    );
                });
            }
            
            // Show startup flashcard after a short delay (regardless of welcome message setting)
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // 3 second delay
                    Minecraft.getInstance().execute(() -> {
                        progressManager.showStartupFlashcard();
                    });
                } catch (InterruptedException e) {
                    // Handle interruption
                }
            }).start();
        }
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
            int lineHeight = 12;
            int sectionSpacing = 16;
            
            // Current location with shadow
            BlockPos playerPos = mc.player.blockPosition();
            String posText = "Position: " + playerPos.getX() + ", " + playerPos.getY() + ", " + playerPos.getZ();
            fontRenderer.drawShadow(matrixStack, posText, x, y, 0xFFFFFF);
            y += sectionSpacing;
            
            // Biome info - always show header
            fontRenderer.drawShadow(matrixStack, "Biome:", x, y, 0xFFD700);
            y += lineHeight;
            
            Biome biome = mc.level.getBiome(playerPos);
            ResourceLocation biomeRL = mc.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
            if (biomeRL != null) {
                String biomeKey = "biome." + biomeRL.toString().replace(':', '.');
                String spanishBiome = LanguageDisplayMod.getInstance().getSpanishTranslation(biomeKey);
                String englishBiome = LanguageDisplayMod.getInstance().getEnglishTranslation(biomeKey);
                fontRenderer.drawShadow(matrixStack, "  " + spanishBiome, x, y, 0xFFFFFF);
                y += lineHeight;
                fontRenderer.drawShadow(matrixStack, "  " + englishBiome, x, y, 0xBBBBBB);
            }
            y += sectionSpacing;
            
            // Looking At info - always show header
            fontRenderer.drawShadow(matrixStack, "Looking At:", x, y, 0xFFD700);
            y += lineHeight;
            
            // Check for block or entity
            RayTraceResult rayTrace = mc.hitResult;
            boolean foundTarget = false;
            
            if (rayTrace != null && rayTrace.getType() == RayTraceResult.Type.BLOCK) {
                BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) rayTrace;
                BlockState blockState = mc.level.getBlockState(blockRayTrace.getBlockPos());
                Block block = blockState.getBlock();
                ResourceLocation blockRL = block.getRegistryName();
                
                if (blockRL != null) {
                    String blockKey = "block." + blockRL.toString().replace(':', '.');
                    String spanishBlock = LanguageDisplayMod.getInstance().getSpanishTranslation(blockKey);
                    String englishBlock = LanguageDisplayMod.getInstance().getEnglishTranslation(blockKey);
                    fontRenderer.drawShadow(matrixStack, "  " + spanishBlock, x, y, 0xFFFFFF);
                    y += lineHeight;
                    fontRenderer.drawShadow(matrixStack, "  " + englishBlock, x, y, 0xBBBBBB);
                    foundTarget = true;
                }
            }
            
            // Check for entity if no block found
            if (!foundTarget && rayTrace != null && rayTrace.getType() == RayTraceResult.Type.ENTITY) {
                EntityRayTraceResult entityRayTrace = (EntityRayTraceResult) rayTrace;
                Entity entity = entityRayTrace.getEntity();
                ResourceLocation entityRL = entity.getType().getRegistryName();
                
                if (entityRL != null) {
                    String entityKey = "entity." + entityRL.toString().replace(':', '.');
                    String enTranslation = LanguageDisplayMod.getInstance().getEnglishTranslation(entityKey);
                    String esTranslation = LanguageDisplayMod.getInstance().getSpanishTranslation(entityKey);
                    
                    // If translation is missing, show cleaner format
                    if (enTranslation.equals(entityKey)) {
                        String cleanName = entityRL.getPath();
                        cleanName = cleanName.substring(0, 1).toUpperCase() + cleanName.substring(1).replace('_', ' ');
                        enTranslation = cleanName;
                    }
                    
                    fontRenderer.drawShadow(matrixStack, "  " + esTranslation, x, y, 0xFFFFFF);
                    y += lineHeight;
                    fontRenderer.drawShadow(matrixStack, "  " + enTranslation, x, y, 0xBBBBBB);
                    foundTarget = true;
                }
            }
            
            // Always maintain consistent spacing for Looking At section
            if (!foundTarget) {
                // Match the Y advancement of content case: only lineHeight (like Spanish text)
                y += lineHeight; // Match what Spanish text would do
            }
            // Always use the same section spacing as biome section
            y += sectionSpacing;
            
            // Holding info - always show header
            fontRenderer.drawShadow(matrixStack, "Holding:", x, y, 0xFFD700);
            y += lineHeight;
            
            ItemStack heldItem = mc.player.getMainHandItem();
            if (!heldItem.isEmpty()) {
                String translationKey = heldItem.getItem().getDescriptionId();
                String spanishItem = LanguageDisplayMod.getInstance().getSpanishTranslation(translationKey);
                String englishItem = LanguageDisplayMod.getInstance().getEnglishTranslation(translationKey);
                fontRenderer.drawShadow(matrixStack, "  " + spanishItem, x, y, 0xFFFFFF);
                y += lineHeight;
                fontRenderer.drawShadow(matrixStack, "  " + englishItem, x, y, 0xBBBBBB);
            } else {
                // Always maintain consistent spacing for Holding section
                y += lineHeight; // Spanish line space
                y += lineHeight; // English line space
            }
        }
    }
}