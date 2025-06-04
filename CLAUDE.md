# Language Display Mod - Development Notes

## Project Overview
This is a Minecraft Forge mod for version 1.16.5 that displays real-time language translations for in-game elements with interactive Spanish audio support and a gamified learning system.

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
   - Enhanced block data from `minecraft_blocks_mod_data.json` with descriptions
   - Uses Gson for JSON parsing with UTF-8 encoding
   - Translation keys follow Minecraft's standard format (e.g., `block.minecraft.stone`)

2. **Progress Tracking System (`ProgressManager.java`)**
   - Tracks discovered items, blocks, and biomes
   - Saves progress to `language_progress.json`
   - Presents items in sequential order from the data file
   - Supports skipping items (moved to end of queue)
   - Displays current target item with Spanish description
   - Shows progress counter (X/Y items discovered)

3. **Audio System (`AudioManager.java`)**
   - Direct file loading - no mapping file needed
   - Supports both MP3 and OGG formats (prefers OGG)
   - Audio files named with translation keys (e.g., `block.minecraft.stone.mp3`)
   - VLC integration for reliable playback (if installed)
   - Fallback methods for Windows/Mac/Linux
   - Asynchronous playback to prevent game freezing
   - Playback speed control via `/slow` command

4. **Input Handler (`KeyInputHandler.java`)**
   - Single F key with smart priority system:
     - If looking at block/entity → speak that
     - If not looking but holding item → speak item
     - If neither → speak current biome
   - Marks items/biomes as discovered (not entities)
   - Generates appropriate translation keys

5. **Command System (`LanguageCommands.java`)**
   - `/hint` - Shows English description of current target item
   - `/skip` - Moves current target to end of queue
   - `/slow` - Check current playback speed
   - `/slow <0.25-2.0>` - Set audio playback speed
   - `/testaudio <key>` - Test audio for specific translation key

6. **HUD Overlay Renderer**
   - Shows progress (X/Y items discovered)
   - Displays current target item with Spanish description
   - Shows player position, biome, and what they're looking at
   - Real-time updates with color-coded information
   - Word-wrapped Spanish descriptions

### Learning System Features
- **Gamified Collection**: Players must discover all items/biomes
- **Achievement Messages**: "¡Descubrimiento!" notifications in chat
- **Sequential Targets**: Items presented in order from data file
- **Skip System**: Can skip difficult items for later
- **Progress Persistence**: Saves between game sessions
- **Spanish Descriptions**: Detailed descriptions for learning context

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
- **minecraft_blocks_mod_data.json**: Enhanced data with:
  - Spanish/English names
  - Obtainment tags
  - Primary use categories
  - Detailed descriptions in both languages
- **language_progress.json**: Player progress tracking

### Usage Flow
1. Player sees current target item in HUD with Spanish description
2. Player obtains item and presses F while holding it
3. Audio plays Spanish pronunciation
4. Achievement message appears
5. Progress updates and next target appears
6. Use `/hint` for help or `/skip` to defer items

### Key Classes
- `LanguageDisplayMod`: Main mod class, manages translations and overlay
- `AudioManager`: Handles audio playback with multiple fallback methods
- `KeyInputHandler`: Processes F key with smart priority detection
- `ProgressManager`: Tracks learning progress and manages targets
- `LanguageCommands`: Implements hint, skip, and audio commands

### Current Status
- ✅ Full translation system with 900+ items
- ✅ Progress tracking and persistence
- ✅ Achievement notifications
- ✅ Audio playback (best with VLC or OGG files)
- ✅ Command system for hints and skipping
- ✅ Entity translations included
- ✅ UTF-8 support for proper Spanish characters

### Known Limitations
- MP3 playback requires VLC for best results
- Windows-specific file paths (hardcoded)
- No configuration GUI yet
- Audio speed control limited by playback method