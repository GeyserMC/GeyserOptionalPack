# GeyserOptionalPack

Optional Bedrock resource pack to extend Geyser functionality

### Implements

- Armor base arms/baseplate visibility
- Armor stand poses
- Illusioners
- Iron golem cracked textures
- Missing particles
- Shulker invisibility parity
- Spectral arrow entity texture

Currently is not in use yet. Stay tuned!

### Manually building

Run `./copy_java_files_to_pack.sh` in this directory and the necessary files from the vanilla jar will be copied to the required directories. Zip up and you're set. If planning to contribute, run `./copy_java_files_to_pack.sh -njc` to prevent the script from one-lining the JSON files, as this is handled by the CI.

### Legal

This repository is not endorsed nor affiliated with Mojang, Minecraft, or Microsoft.
