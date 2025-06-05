# Minecraft Language Learning Mod

A Minecraft Forge mod for version 1.16.5 that helps players learn Spanish through immersive gameplay with real-time translations, audio pronunciations, and a flashcard-based learning system.

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

### Audio Support
- Press **F** to hear Spanish pronunciation
- Smart priority: looks at block/entity first, then held item, then biome
- Over 4,800 audio files covering blocks, items, entities, and biomes
- Adjustable playback speed with `/slow` command

### Commands
- **/languagehelp**: Shows all available commands
- **/progress**: View your discovery and mastery statistics
- **/flashcard**: Manually trigger a flashcard quiz
- **/slow**: Check current audio playback speed
- **/slow <0.25-2.0>**: Set audio playback speed
- **/testaudio <key>**: Test audio for specific translation key
- **/languagetoggle**: Toggle welcome message on/off

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
- (Recommended) VLC Media Player for MP3 audio support

## Installation

1. Install Minecraft Forge for 1.16.5
2. Download the mod JAR file
3. Place the JAR file in your `.minecraft/mods` folder
4. Ensure audio files are in `C:/Users/benau/forge_language_mod_1.16.5/audio/es_mx/`
5. Translation files should be in `C:/Users/benau/forge_language_mod_1.16.5/translation_keys/1.16.5/`
6. Launch Minecraft with the Forge profile

## Controls

- **F Key**: Discover and hear Spanish pronunciation for what you're looking at

## Languages Supported

- English (en_us)
- Spanish (es_mx) with full audio pronunciation

## Building from Source

1. Clone the repository
2. Run `./gradlew build` (or `gradlew build` on Windows)
3. The built JAR will be in `build/libs/`

## Development Setup

1. Clone the repository
2. Run `./gradlew genEclipseRuns` for Eclipse
3. Import the project into your IDE
4. Run `./gradlew runClient` to test the mod

## Source Files

### Main Package (`com.example.languagemod`)

- **LanguageDisplayMod.java**: Main mod class that initializes the mod, loads translations, and manages the HUD overlay renderer.
- **AudioManager.java**: Handles audio file playback with VLC integration and multiple fallback methods for cross-platform support.
- **KeyInputHandler.java**: Processes the F key input with smart priority detection for blocks, entities, items, and biomes.
- **ProgressManager.java**: Tracks player's learning progress, flashcard data, and handles save/load of progress.
- **FlashcardManager.java**: Manages flashcard quizzes with fuzzy matching for answers.
- **LanguageCommands.java**: Implements slash commands for progress, flashcards, and settings.

## Data Files

- **en_us.json**: English translations for all Minecraft elements
- **es_mx.json**: Spanish translations including entity names
- **progress_tracker.json**: Automatically created to track your progress and flashcard data

## Audio Files

The mod includes over 4,800 Spanish audio files located in `audio/es_mx/` covering:
- All vanilla blocks
- All vanilla items  
- All vanilla entities
- Biomes, advancements, and UI elements

Audio files should be named with translation keys (e.g., `block.minecraft.stone.mp3` or `block.minecraft.stone.ogg`)

## License

All rights reserved

## Author

Ben