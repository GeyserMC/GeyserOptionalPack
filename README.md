# GeyserOptionalPack

Optional Bedrock resource pack to extend Geyser functionality. Learn more on its [wiki page](https://github.com/GeyserMC/Geyser/wiki/GeyserOptionalPack).

Download: https://ci.opencollab.dev/job/GeyserMC/job/GeyserOptionalPack/job/master/lastSuccessfulBuild/artifact/GeyserOptionalPack.mcpack

### Implements

- Armor base arms/baseplate visibility
- Armor stand poses
- Illusioners
- Missing particles
- Offhand animations
- Shulker invisibility parity
- Spectral arrow entity texture

### Manually building

Run `./prepare_pack.sh` in this directory and the necessary files from the vanilla jar will be copied to the required directories. Zip up and you're set. Optionally, you may compress the output JSON files by running `./prepare_pack.sh -jc`. This should not be done when planning to contribute. You may then package with `zip GeyserOptionalPack.mcpack -r . -x ".*" Jenkinsfile required_files.txt prepare_pack.sh`.

### Legal

This repository is not endorsed nor affiliated with Mojang, Minecraft, or Microsoft.
