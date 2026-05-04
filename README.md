# Legendary Items Mod

A Minecraft Fabric mod for version 1.21.1 that adds legendary pedestals and powerful legendary weapons with unique abilities.

## Features

### Legendary Items

#### Fiery Dagger
- **Stats**: Less damage (3) than netherite sword, more attack speed (-1.4)
- **Passive**: Fire Aspect II built-in, immunity to fire damage
- **Right Click** (15s cooldown): Spawn a ring of infernal flames where fire damage becomes **void damage** (bypasses armor and enchantments, doesn't affect you)
- **Shift + Right Click** (10s cooldown): Become enraged, increasing attack speed and damage. Your fire damage goes through fire resistance (but not armor)
- **Animations**: Cool flame particle effects for both abilities

#### Void Sword
- **Stats**: Standard netherite sword (5 damage), can enchant with Sharpness up to VII
- **Right Click** (7.5s cooldown): Teleport in the direction you're looking (10 blocks) and pull in nearby mobs/players
- **Shift + Right Click** (10s cooldown): Throw the sword (visually) to stun the closest target in place for 4 seconds
- **Animations**: Soul particle effects

#### Vampire Sword
- **Stats**: 4 damage, netherite sword enchantments
- **Passive**: 15% chance to heal 1 heart on hit
- **Right Click** (12.5s cooldown): Heart Steal - Summon a ring that drains 1 max heart from each nearby player and grants it to you (10 second duration)
- **Shift + Right Click** (9s cooldown): Bite - Next hit deals double damage and drains 1 max heart from target (lasts 15 seconds)
- **Animations**: Heart and enchanted particle effects

### Legendary Pedestal
- Place legendary items on pedestals with configurable required items
- Right-click to claim if you have all required items
- Once claimed, the pedestal becomes empty
- Stores item and requirements in NBT data

## Installation

1. Download and install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Download Fabric API for 1.21.1
3. Place the mod JAR in your `mods` folder
4. Launch Minecraft with the Fabric profile

## Building from Source

```bash
./gradlew build
```

The built JAR will be in `build/libs/legendary-items-1.0.0.jar`

## Usage

### Getting Items
- Items can be obtained via commands: `/give @s legendary-items:<item_name>`
- Available items: `fiery_dagger`, `void_sword`, `vampire_sword`, `legendary_pedestal`

### Setting Up a Pedestal
1. Place a Legendary Pedestal block
2. Use commands or mods to set the item and requirements (requires custom modding)
3. Players can right-click to attempt claiming

## Configuration

All cooldowns, damage values, and ability ranges can be modified in the source code:
- `FieryDagger.java`: Fire ring radius, enrage duration
- `VoidSword.java`: Teleport distance, pull range, stun duration
- `VampireSword.java`: Heart drain amounts, cooldowns, proc chance

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.16.9+
- Fabric API 0.104.0+
- Java 21+

## License

MIT License - Feel free to modify and distribute with attribution

## Credits

Created for the legendary items mod project
