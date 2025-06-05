# Minecraft Language Learning Mod

A Minecraft Forge mod for version 1.16.5 that helps players learn Spanish through immersive gameplay with real-time translations and a flashcard-based learning system.

## Features

### Visual Display
The mod shows clean, minimal information in the top-left corner:
- **Position**: Your current coordinates
- **Biome**: Current biome with Spanish and English translations
- **Looking At**: Block or entity you're targeting with translations
- **Holding**: Item in your main hand with translations

### Learning System
- **Discovery System**: Look at blocks, entities, items, or biomes and press F to discover them
- **Flashcard Quizzes**: Test your knowledge with Spanish-English translation questions
- **Spaced Repetition**: Flashcards appear when discovering items and periodically every 5 minutes
- **Mastery Tracking**: Master items by correctly answering their flashcard 5 times
- **Progress Tracking**: Saves your progress between sessions

### Audio Support (Currently Non-Functional)
- Press **F** to hear Spanish pronunciation (not working - see Known Issues)
- Smart priority: looks at block/entity first, then held item, then biome
- 2,124 OGG audio files included but not playing
- ⚠️ **Audio system is broken - focus on visual learning features**

### Commands
- **/languagehelp**: Shows all available commands
- **/progress**: View your discovery and mastery statistics
- **/flashcard**: Manually trigger a flashcard quiz
- **/slow**: Check current audio playback speed (non-functional)
- **/slow <0.25-2.0>**: Set audio playback speed (non-functional)
- **/testaudio <key>**: Test audio for specific translation key (confirms registration but no sound)
- **/languagetoggle**: Toggle welcome message on/off
- **/flashcardtime [minutes]**: Set flashcard interval (1-120 minutes)

### Debug Commands
- **/testvanilla**: Plays vanilla bell sound (works - confirms sound system is functional)
- **/debugsound <key>**: Debug specific sound with multiple playback methods
- **/checkresource <key>**: Verify if OGG file exists in mod resources

## Example Display

```
Position: 150, 64, -200

Biome:
  Bosque
  Forest

Looking At:
  Piedra
  Stone

Holding:
  Pico de Diamante
  Diamond Pickaxe
```

## Flashcard System

When you receive a flashcard, type your answer in chat:
- Questions can be English→Spanish or Spanish→English
- Fuzzy matching allows minor typos
- Spanish special characters (é, ñ, etc.) are optional
- After 5 correct answers, the item is mastered

## Requirements

- Minecraft 1.16.5
- Minecraft Forge 36.2.34 or higher
- Java 8
- Windows/Mac/Linux

## Installation

1. Install Minecraft Forge for 1.16.5
2. Download the mod JAR file
3. Place the JAR file in your `.minecraft/mods` folder
4. Launch Minecraft with the Forge profile

Note: All translations and audio files are bundled inside the mod JAR

## Controls

- **F Key**: Discover items and trigger (non-functional) Spanish pronunciation

## Languages Supported

- English (en_us)
- Spanish (es_mx) - visual translations work, audio does not

## Building from Source

1. Clone the repository
2. Run `./gradlew build` (or `gradlew build` on Windows)
3. The built JAR will be in `build/libs/`

## Development Setup

1. Clone the repository
2. Run `./gradlew genEclipseRuns` for Eclipse
3. Import the project into your IDE
4. Run `./gradlew runClient` to test the mod

## Known Issues

### Critical: Audio Not Playing
The mod's audio system is completely non-functional despite extensive debugging:

**Symptoms:**
- All 2,124 sound events are registered successfully
- OGG files are valid and playable outside Minecraft
- Vanilla Minecraft sounds work (verified with /testvanilla)
- No errors in logs, claims to play successfully
- Multiple playback methods attempted (world.playSound, playLocalSound, SimpleSound)
- All volume settings at 100%

**Debugging Attempted:**
- Verified sounds.json has correct structure
- Removed namespace prefixes from sound paths
- Tested multiple sound categories (MASTER, RECORDS, BLOCKS)
- Added resource verification commands
- Tried different OGG encodings

**Root Cause:** Unknown - appears to be a fundamental incompatibility between the mod's sound loading and Minecraft/Forge's sound system. The visual learning features work perfectly, so the mod is still usable without audio.

## Working Features

Despite the audio issues, these features work perfectly:
- ✅ Real-time translation overlay
- ✅ Discovery system (F key)
- ✅ Flashcard quizzes
- ✅ Progress tracking and persistence
- ✅ Mastery system
- ✅ All commands except audio-related ones
- ✅ 5000+ Spanish translations

## Author

Ben