# Language Display Mod for Minecraft 1.16.5

A Minecraft Forge mod that displays real-time language translations for in-game elements with audio support. Perfect for learning Spanish while playing Minecraft!

## Features

### Visual Display
The mod shows clean, minimal information in the top-left corner:
- **Position**: Your current coordinates
- **Biome**: Current biome with English and Spanish translations
- **Looking At**: Block or entity you're targeting with translations
- **Holding**: Item in your main hand with translations

### Audio Support
- Press **F** to hear Spanish audio pronunciation of whatever you're looking at
- If not looking at anything, it reads your held item
- Over 4,800 audio files covering blocks, items, entities, and more

## Example Display

```
Position: 150, 64, -200

Biome:
  EN: Forest
  ES: Bosque

Looking At:
  EN: Stone
  ES: Piedra

Holding:
  EN: Diamond Pickaxe
  ES: Pico de Diamante
```

## Requirements

- Minecraft 1.16.5
- Minecraft Forge 36.2.34 or higher
- Java 8
- Windows (for audio support)

## Installation

1. Install Minecraft Forge for 1.16.5
2. Download the mod JAR file
3. Place the JAR file in your `.minecraft/mods` folder
4. Launch Minecraft with the Forge profile

## Controls

- **F Key**: Play Spanish audio for the item/block/entity you're looking at

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

## Audio Files

The mod includes over 4,800 Spanish audio files located in `audio/es_mx/` covering:
- All vanilla blocks
- All vanilla items  
- All vanilla entities
- Biomes, advancements, and UI elements

## License

All rights reserved

## Author

Ben