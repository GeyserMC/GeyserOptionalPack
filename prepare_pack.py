from PIL import Image
import os
import shutil
import zipfile
import requests
import sys
import json

# Download the client jar from mojang to extract assets
clientJar = requests.get(
    "https://launcher.mojang.com/v1/objects/37fd3c903861eeff3bc24b71eed48f828b5269c8/client.jar"
)
with open("client.jar.temp", "w") as client:
    client.buffer.write(clientJar.content)
    with zipfile.ZipFile("client.jar.temp", "r") as zObject:
        zObject.extractall("extracted/")

# Copy textures defined in required_files.txt over to the pack
with open("required_files.txt", "r") as assetsNeeded:
    for files in assetsNeeded.readlines():
        filesToCopy = files.replace("\n", "").split()
        try:
            os.makedirs(filesToCopy[1])
        except:
            print("extracted/" + filesToCopy[0] + " " + filesToCopy[1])
        try:
            shutil.copy("extracted/" + filesToCopy[0], filesToCopy[1])
        except:
            pass


# Sweep Attack
# Create required sprites with Pillow (pip install pillow)
particles = os.listdir("extracted/assets/minecraft/textures/particle/")
sweepArray = []
for fileName in particles:
    if "sweep" in fileName:
        sweepArray.append(fileName)
frames = []
for current_file in sweepArray:
    try:
        with Image.open(
            "extracted/assets/minecraft/textures/particle/" + current_file
        ) as im:
            frames.append(im.getdata())
    except:
        pass

tile_width = frames[0].size[0]
tile_height = frames[0].size[1]

spriteSheetHeight = frames[0].size[1] * frames.__len__()
spritesheet = Image.new("RGBA", (tile_width, spriteSheetHeight))
for sprite in frames:
    spritesheet.paste(
        sprite,
        (
            0,
            tile_height * frames.index(sprite),
            tile_width,
            tile_height * frames.index(sprite) + tile_height,
        ),
    )
spritesheet.quantize(256).save("textures/geyser/particle/sweep_attack.png", "PNG")

os.remove("client.jar.temp")
shutil.rmtree("extracted")

# One-line our JSON files if the script is called with -jc (python prepare_pack.py -jc)
if sys.argv.__contains__("-jc"):
    for path, subdirs, files in os.walk("."):
        for name in files:
            formattedName = (
                os.path.join(path, name).replace(".\\", "").replace("\\", "/")
            )
            if formattedName.endswith(".json"):
                minified = ""
                with open(formattedName, "r") as jsonFile:
                    minified = json.dumps(
                        json.loads(jsonFile.read()), separators=(",", ":")
                    )
                os.remove(formattedName)
                with open(formattedName, "w") as jsonFile:
                    jsonFile.write(minified)
