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
   - Loads translation files from `translation_keys/1.16.5/` directory
   - Supports English (`en_us.json`) and Spanish (`es_mx.json`)
   - Uses Gson for JSON parsing with UTF-8 encoding
   - Translation keys follow Minecraft's standard format (e.g., `block.minecraft.stone`)
   - All translations loaded from standard language files only

2. **Progress Tracking System (`ProgressManager.java`)**
   - Tracks discovered items, blocks, biomes, and entities
   - Saves progress to `progress_tracker.json`
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

4. **Audio System (`AudioManager.java`)**
   - Direct file loading - no mapping file needed
   - Supports both MP3 and OGG formats (prefers OGG)
   - Audio files named with translation keys (e.g., `block.minecraft.stone.mp3`)
   - VLC integration for reliable playback (if installed)
   - Fallback methods for Windows/Mac/Linux
   - Asynchronous playback to prevent game freezing
   - Playback speed control via `/slow` command

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
- **Audio Files**: 4,800+ audio files in `audio/es_mx/` directory
- **File Naming**: Direct translation key naming (no mapping needed)
- **Format Support**: MP3 and OGG (OGG preferred)
- **Playback Methods**:
  - VLC (hidden mode, best for MP3)
  - PowerShell Media Player (Windows)
  - Default system player (last resort)
- **Volume Control**: Respects Minecraft master volume
- **Speed Control**: Adjustable playback speed

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
- ✅ Progress tracking and persistence
- ✅ Flashcard system with fuzzy matching
- ✅ Mastery notifications
- ✅ Audio playback (best with VLC or OGG files)
- ✅ Command system for progress and settings
- ✅ Entity translations included
- ✅ UTF-8 support for proper Spanish characters
- ✅ Clean HUD without flashing

### Recent Changes
- Removed all discovery messages (only mastery messages remain)
- Removed Spanish descriptions from HUD
- Fixed text encoding issues (removed special quotes and ¡ characters)
- Added persistent section headers to prevent flashing
- Implemented flashcard timing logic (5-minute cooldown after correct answer)
- Added shadow to all HUD text for better readability
- Simplified welcome message
- Updated command system with /languagehelp and /progress

### Known Limitations
- MP3 playback requires VLC for best results
- Windows-specific file paths (hardcoded)
- No configuration GUI yet
- Audio speed control limited by playback method