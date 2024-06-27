# Combat Sounds
mkdir -p sounds/geyser/entity/player/attack/

# Critical Attack
wget -O sounds/geyser/entity/player/attack/crit1.ogg https://resources.download.minecraft.net/50/509656b6d02a4491f46c686e66b615950c6c1408
wget -O sounds/geyser/entity/player/attack/crit2.ogg https://resources.download.minecraft.net/1b/1b172129daf7cd9a36d2b0f7820baf2e479e381a
wget -O sounds/geyser/entity/player/attack/crit3.ogg https://resources.download.minecraft.net/15/15bbaf7901d7abff12bb872ff88a6ad541f5227a

# Knockback Attack
wget -O sounds/geyser/entity/player/attack/knockback1.ogg https://resources.download.minecraft.net/08/08626fc2a337c28b5dfdafb6daa9ea31f9a70571
wget -O sounds/geyser/entity/player/attack/knockback2.ogg https://resources.download.minecraft.net/05/0556f8b2dc424e7368b4ab9f8a315aa26982e3fc
wget -O sounds/geyser/entity/player/attack/knockback3.ogg https://resources.download.minecraft.net/08/085a6cd2e023877254d1118c403f39e556c003cb
wget -O sounds/geyser/entity/player/attack/knockback4.ogg https://resources.download.minecraft.net/1c/1c722dfd43b06c28273bc8c56d1d02c1a6ea5e48

# Sweep Attack
wget -O sounds/geyser/entity/player/attack/sweep1.ogg https://resources.download.minecraft.net/fd/fd20e1cd8c69bc2f037de950b078a729a4b7d6a6
wget -O sounds/geyser/entity/player/attack/sweep2.ogg https://resources.download.minecraft.net/c9/c9534f4d840470b3c6efbcb84cff23c57baa3393
wget -O sounds/geyser/entity/player/attack/sweep3.ogg https://resources.download.minecraft.net/4c/4c26fd4c2774e7afcbda1e293a27595e04e87c47
wget -O sounds/geyser/entity/player/attack/sweep4.ogg https://resources.download.minecraft.net/ae/ae9bb7a332e3e3d3665f282b60b296ec01be97df
wget -O sounds/geyser/entity/player/attack/sweep5.ogg https://resources.download.minecraft.net/50/50a317f837b0604c3ebe8224951c1a0d7a94516a
wget -O sounds/geyser/entity/player/attack/sweep6.ogg https://resources.download.minecraft.net/39/39077d824a27e8040b0e1f2b4707d81149830d11
wget -O sounds/geyser/entity/player/attack/sweep7.ogg https://resources.download.minecraft.net/e8/e8d0df494880f2067bb64d08a7428a78239c9a29

# Download the client jar from mojang to extract assets
wget https://launcher.mojang.com/v1/objects/37fd3c903861eeff3bc24b71eed48f828b5269c8/client.jar
unzip client.jar -d extracted/

# Set input field seperator to space for our while loop
IFS=' '

# Copy textures defined in required_files.txt over to the pack
while read -r p || [ -n "$p" ]; do
  read -rafilesToCopy<<< "$p"
  echo "extracted/${filesToCopy[0]} ${filesToCopy[1]}"
  mkdir -p "${filesToCopy[1]}"
  cp "extracted/${filesToCopy[0]}" "${filesToCopy[1]}"
done <required_files.txt

# Create required sprites with Imagemagick
convert -append extracted/assets/minecraft/textures/particle/sweep_*.png -define png:format=png8 textures/geyser/particle/sweep_attack.png

rm client.jar
rm -r extracted

# Set our input field seperator back to new line
IFS=$'\n'

# One-line our JSON files if the script is called with -jc (./prepare_pack.sh -jc)
if [[ ${1} == "-jc" ]]
then
  for i in $(find . -type f -name "*.json")
  do
    # Note that we must use a temp file, as awk does not support in-place editting
    awk -v ORS= 'BEGIN {FS = OFS = "\""}/^[[:blank:]]*$/ {next}{for (i=1; i<=NF; i+=2) gsub(/[[:space:]]/,"",$i);sub("\r$", "")} 1' ${i} > tmp && mv tmp ${i}
  done
fi
