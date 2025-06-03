# Language Display Mod for Minecraft 1.16.5

A Minecraft Forge mod that displays real-time information about your surroundings with multilingual support.

## Features

The mod displays the following information in the top-left corner of your screen:

- **Current Position**: Your current block coordinates (X, Y, Z)
- **Biome Information**: Current biome with English and Spanish translations
- **Target Block**: Information about the block you're looking at with translations
- **Held Item**: Details about the item in your main hand with translations
- **Entity Info**: Information about entities under your crosshair with translations

## Example Display

```
Block: 31 77 6

Biome Info:
Biome: minecraft:jungle
Translation key: biome.minecraft.jungle
English: Jungle
Spanish: Jungla

Target Block Info:
Targeted Block: 32, 73, 11
Namespaced ID: minecraft:melon
Translation key: block.minecraft.melon
English: Melon
Spanish: Sandía

Held Item (in main hand):
Held Item: minecraft:stone_sword
Translation key: item.minecraft.stone_sword
English: Stone Sword
Spanish: Espada de piedra

Entity Under Crosshair:
Entity: minecraft:zombie
Translation key: entity.minecraft.zombie
English: Zombie
Spanish: Zombi
```

## Requirements

- Minecraft 1.16.5
- Minecraft Forge 36.2.34 or higher
- Java 8

## Installation

1. Install Minecraft Forge for 1.16.5
2. Download the mod JAR file
3. Place the JAR file in your `.minecraft/mods` folder
4. Launch Minecraft with the Forge profile

## Building from Source

1. Clone the repository
2. Open a terminal in the project directory
3. Run `./gradlew build` (or `gradlew build` on Windows)
4. The built JAR will be in `build/libs/`

## Development Setup

1. Clone the repository
2. Run `./gradlew genEclipseRuns` for Eclipse
3. Run `./gradlew eclipse` to generate project files
4. Import the project into your IDE
5. Run `./gradlew runClient` to test the mod

## Languages Supported

- English (en_us)
- Spanish (es_mx)

## License

All rights reserved

## Author

Ben