# Language Display Mod - Development Notes

## Project Overview
This is a Minecraft Forge mod for version 1.16.5 that displays real-time language translations for in-game elements with interactive Spanish audio support and a flashcard-based learning system.

## Technical Details

### Mod Structure
- **Mod ID**: `languagemod`
- **Main Class**: `com.example.languagemod.LanguageDisplayMod`
- **Forge Version**: 36.2.34
- **Minecraft Version**: 1.16.5

### Key Components

1. **Translation System**
   - Loads translation files from `src/main/resources/assets/languagemod/lang/`
   - Supports English (`en_us.json`) and Spanish (`es_mx.json`)
   - Uses Gson for JSON parsing with UTF-8 encoding
   - Translation keys follow Minecraft's standard format (e.g., `block.minecraft.stone`)
   - All translations loaded from bundled resources for proper mod distribution

2. **Progress Tracking System (`ProgressManager.java`)**
   - Tracks discovered items, blocks, biomes, and entities
   - Saves progress to config directory (`languagemod_progress.json`)
   - Manages flashcard data and spaced repetition
   - Tracks mastery status (5 correct answers = mastered)
   - Handles welcome message preferences
   - No target system - all items discovered organically

3. **Flashcard System (`FlashcardManager.java`)**
   - Interactive quiz system in chat
   - Bidirectional questions (English→Spanish or Spanish→English)
   - Fuzzy matching with Levenshtein distance
   - Accent-insensitive matching (é = e)
   - Case-insensitive answers
   - Retry mechanism for incorrect answers
   - Mastery notification after 5 correct answers

4. **Audio System (`AudioManager.java` + `ModSounds.java`)**
   - Uses Minecraft's native sound system for proper integration
   - OGG format only (bundled in mod resources)
   - Sound events registered via Forge's DeferredRegister system
   - Audio files named with translation keys (e.g., `block.minecraft.stone.ogg`)
   - Proper volume control respecting Minecraft settings
   - Sound events defined in `sounds.json` with 2,124 registered sounds

5. **Input Handler (`KeyInputHandler.java`)**
   - Single F key with smart priority system:
     - If looking at block/entity → speak that
     - If not looking but holding item → speak item
     - If neither → speak current biome
   - Marks items/biomes/entities as discovered
   - No discovery messages shown
   - Generates appropriate translation keys

6. **Command System (`LanguageCommands.java`)**
   - `/languagehelp` - Shows all available commands
   - `/progress` - Shows discovery and mastery statistics
   - `/flashcard` - Manually trigger a flashcard
   - `/slow` - Check current playback speed
   - `/slow <0.25-2.0>` - Set audio playback speed
   - `/testaudio <key>` - Test audio for specific translation key
   - `/languagetoggle` - Toggle welcome message on/off
   - `/hint` and `/skip` - Deprecated commands

7. **HUD Overlay Renderer**
   - Shows position, biome, looking at, and holding information
   - Spanish translations shown in white, English in gray
   - Text with shadow for better readability
   - Section headers always visible (no flashing)
   - Gold headers for visual organization
   - Clean spacing between sections

### Learning System Features
- **Discovery System**: Press F to discover and hear items
- **Flashcard Timing**: 
  - Initial flashcard when discovering (if 5+ min since last correct answer)
  - Periodic flashcards every 5 minutes for discovered items
- **Mastery System**: 5 correct flashcard answers = mastered
- **Progress Persistence**: Saves between game sessions
- **No Spanish Descriptions**: Clean, simple interface

### Audio Implementation
- **Audio Files**: 2,124 OGG audio files in `src/main/resources/assets/languagemod/sounds/es_mx/`
- **File Naming**: Direct translation key naming (e.g., `block.minecraft.stone.ogg`)
- **Format Support**: OGG only (Minecraft native sound system)
- **Playback Methods**: Minecraft's native sound system with proper sound event registration
- **Volume Control**: Respects Minecraft master volume and sound effect volume
- **Registration**: Uses Forge's DeferredRegister system for proper sound event handling

### Data Files
- **en_us.json**: English translations for all game elements
- **es_mx.json**: Spanish translations (includes entity names)
- **progress_tracker.json**: Player progress and flashcard tracking
  - Discovered items with timestamps
  - Flashcard attempt counts
  - Correct answer counts
  - Mastery status
  - User preferences

### Usage Flow
1. Player explores world normally
2. Player presses F while looking at/holding something
3. Audio plays Spanish pronunciation
4. Item marked as discovered (silently)
5. Flashcard may appear (based on timing)
6. Player answers flashcards to build mastery
7. After 5 correct answers, mastery message appears

### Key Classes
- `LanguageDisplayMod`: Main mod class, manages translations and overlay
- `AudioManager`: Handles audio playback with multiple fallback methods
- `KeyInputHandler`: Processes F key with smart priority detection
- `ProgressManager`: Tracks learning progress and flashcard data
- `FlashcardManager`: Interactive quiz system with fuzzy matching
- `LanguageCommands`: Implements all slash commands

### Current Status
- ✅ Full translation system with 5000+ items
- ✅ Progress tracking and persistence (saves to config directory)
- ✅ Flashcard system with fuzzy matching
- ✅ Mastery notifications
- ✅ Proper mod structure with bundled resources
- ✅ Command system for progress and settings
- ✅ Entity translations included
- ✅ UTF-8 support for proper Spanish characters
- ✅ Clean HUD without flashing
- ✅ Minecraft native sound system integration

### Recent Changes
- **Major Refactoring**: Converted from external file system to proper Minecraft mod structure
- **Audio System Overhaul**: Replaced VLC/MP3 system with Minecraft's native sound system
- **Resource Bundling**: All files now bundled in mod JAR for single-file distribution
- **Sound Registration**: Implemented proper Forge sound event registration with DeferredRegister
- **Translation Loading**: Now loads from mod resources instead of external files
- **Config Directory**: Progress file now saves to Minecraft's config directory
- Removed all discovery messages (only mastery messages remain)
- Fixed text encoding issues and improved HUD readability

### Known Issues & Limitations
- **Audio Not Playing**: Critical issue - sounds are properly registered but do not play
  - All 2,124 sound events are successfully registered from sounds.json
  - Sound files exist and are valid OGG format (verified playable outside Minecraft)
  - Vanilla Minecraft sounds play correctly (verified with /testvanilla command)
  - Multiple playback methods attempted (world.playSound, playLocalSound, SimpleSound)
  - All volume settings confirmed at 100%
  - Issue appears to be with Minecraft not loading the OGG files from mod resources
  - Possible causes: OGG encoding incompatibility, resource loading issue, or Forge sound system limitation
- **No Configuration GUI**: All settings managed via commands
- **Playback Speed**: Minecraft's sound system doesn't support variable playback speed

### Debug Commands Added
- `/testvanilla` - Plays vanilla bell sound to verify sound system works
- `/debugsound <key>` - Shows detailed debug info for a specific sound
- `/checkresource <key>` - Verifies if OGG file exists in mod resources