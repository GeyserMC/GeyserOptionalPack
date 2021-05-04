<!--ts-->
   * [Introduction](#Introduction)
   * [Armor stands](#Armor-stands)
      * [Part visibility and rotation encoding](#Part-visibility-and-rotation-encoding)
      * [Geometry and attachables](#Geometry-and-attachables)
   * [Illusioners](#Illusioners)
   * [Iron golems](#Iron-golems)
      * [Cracking](#Cracking)
      * [Materials](#Materials)
      * [Render controller](Render-controllers)
   * [Killer bunnies](#Killer-bunnies)
   * [Offhand Animation](#Offhand-animation)
   * [Particles](#Particles)
      * [Sweep Attack](#Sweep-attack)
   * [Player skin parts](#Player-skin-parts)
   * [Shulkers](#Shulkers)
   * [Spectral arrow entities](#Spectral-arrow-entities)
<!--te-->

### Introduction

Entity data and entity flags (known as queries in Molang) are pieces of metadata that store various pieces of information about an entity on the Bedrock Edition of Minecraft. You have a query for an entity's health, for example (a number query or an entity data), and you have a query for is an entity is angry (an entity flag, which is either 1.0 or 0.0 in Molang). Not all entities use every query, but every entity has access to most queries, though Bedrock by default ignores these. We use this to our advantage in this resource pack.

### Armor stands

#### Part visibility and rotation encoding

Two flags are designated for toggling an armor stand baseplate and arms. If `query.is_angry` is set to true, the render controller will not render arms on an armor stand. If `query.is_admiring` is set to true, then the armor stand will not render its baseplate. Bedrock without resource packs does not care about these values, so any setup without this resource pack will not break.

In order to easily compress and send over rotation values over the network, we cut off the float rotation values in favor of integer values. The original implementation for each rotation of a limb was set up like the following:

```c
query.example = XXXYYYZZZ
```

The first three digits were designated for the X, the second three for Y, and the final three for Z. Each one went from 0 to 360 - allowing a full rotation on each axis. Unfortunately, Bedrock has some unknown integer limit that cuts off such a large number, so a new system was designed. The final product is as follows:

```c
query.example = BXXYYZZ;
query.example_flag_one = 1.0;
query.example_flag_two = 1.0;
query.example_flag_three = 0.0;
```

B (the first digit of the number) is set up like binary - if 4 is added, then the X rotation should be added by 100; same for Y (2) and Z (1). The following numbers have the remaining value, up to 180 degrees, and are added on top of whatever B determines for that rotation. In order to compensate for the lack of range, three flags are set aside for each rotation to determine if a number is negative. If the corresponding flag of each axis is true, then the number is toggled as negative. This could also have been implemented as binary in the number, but was not implemented due to precision concerns.

#### Geometry and attachables

Bedrock entity animations and entity geometry are data driven. The geometry of the armor stand from Java to Bedrock varies. The pivot of the head is placed differently, which has been corrected for in the included armor stand geometry. Additionally, the Bedrock geometry of the armor stand is not structured to support independent animation of the chestplate area. This is due to the lack of a separate bone for the chestplate, which is also added to the armor stand geometry by this pack.

Bedrock armor is defined via attachables, which contain geometry that utilize the bone structure of the entity to which the attachable attaches. Resultantly, items placed and rendered on the armor stand via attachables do not support rotation of the chestplate either, as attachable structure depends on the geometry of the entity to which the attachable is attached. Therefore, any attachables utilizing the newly added chestplate slot must be redefined. This includes all chestplates, leggings (which place their uppermost portion on the chestplate group), and the elytra. To ensure the redefined attachable is only used in the case of armor stands, `minecraft:attachable.description.item` is set as follows:

```json
{ "minecraft:chainmail_chestplate": "query.owner_identifier == 'minecraft:armor_stand'" }
```

Above, `query.owner_identifier` returns the identifier of the entity to which the attachable is applied, and thus this attachable will only be applied when the owner identifier is `minecraft:armor_stand`.

### Iron golems

#### Cracking

Iron golems in Java edition, experience "cracking" as their health decreases. Cracking overlays a texture that contains cracks on the base iron golem texture.  This occurs in four ranges, in which health values of 100-76 experience no cracking, 75-51 experience low cracking, 50-26 experience medium cracking, and 25-1 experience high cracking. Using this material, a render controller can be utilized to select textures defined in the entity definition file from an array.

#### Materials

The material of most bedrock vanilla entities does not allow for type of texture overlay switching. However, the certain materials, such as the tropical fish, "tropicalfish", allow for this manner of texture overlay switching. Materials are defined in the materials folder, which is not included in Mojang's default assets, and thus must be extracted from the game assets. The tropical fish material is defined in entity.material as follows:

```jsonc
//material definitions take the form child:parent, so tropicalfish essentially inherits all its definitions from entity_multitexture_multiplicative_blend
{"tropicalfish:entity_multitexture_multiplicative_blend": {}}
```

Looking at the fully defined entity_multitexture_multiplicative_blend material, the following is seen:

```jsonc
{
    "entity_multitexture_multiplicative_blend:entity": {
      "+states": [ "DisableCulling" ],
      "+samplerStates": [
        {
          "samplerIndex": 0,
          "textureWrap": "Clamp"
        },
        {
          "samplerIndex": 1,
          "textureWrap": "Clamp"
        }
      ],
      "+defines": [
        "ALPHA_TEST",
        "USE_COLOR_MASK",
        "MULTIPLICATIVE_TINT",
        "MULTIPLICATIVE_TINT_COLOR",
        "USE_OVERLAY"
      ]
    }
}
```

Unfortunately, the "USE_COLOR_MASK" and "MULTIPLICATIVE_TINT_COLOR" definitions rely on certain entity data being sent. Geyser does not send all of this data for every entity, as under vanilla conditions, it does not serve any purpose. For more information, refer to [EntityData.java](https://github.com/CloudburstMC/Protocol/blob/develop/bedrock/bedrock-common/src/main/java/com/nukkitx/protocol/bedrock/data/entity/EntityData.java) in the [CloudburstMC/Protocol](https://github.com/CloudburstMC/Protocol) repository. The lack of this data likely results in a null value being interpreted by the material, leading to a rendering with entirely black pixels. Originally, custom materials were defined that removed the "USE_COLOR_MASK" and "MULTIPLICATIVE_TINT_COLOR" parameters. However, custom materials have been largely broken on Windows 10 devices by the introduction of Render Dragon, which seems to completely remove the ability to define or edit materials in a data-driven fashion. Therefore, Geyser was modified to send the entity component "COLOR_2". This allows for the use of the tropical fish material over Geyser. Due to Render Dragon, this pack will not be able to move ahead with features that require custom materials unless Render Dragon is changed to allow for modification of shaders and material assets.

#### Render controller

In order to utilize multiple textures, a render controller containing a texture array was defined. A position in the texture array is then determined by the following Molang expression:

```json
"textures": [
    "(q.health > 99 || !q.is_bribed) ? 3 : math.floor(q.health / 25)"
    ]
```

The trinary operator ensures that even if `max_health`, defined at 100, is overflowed, the expression will never produce a value outside the range of 0-3. As all data is derived resource pack side, this addition requires no modification by the server (though `query.is_bribed` enables the feature). The textures required for this to display can be retrieved during the build process.

### Illusioners

The illusioner does not exist in Bedrock Edition. Full implementation, however, would require more than a simple texture swap. This is due to the illusioner's special attack, which creates four duplicate false illusioners, which lack a hit box. The actual illusioner remains invisible during this attack. Implementing this would likely be possible from a technical perspective, but it would require either some kind of helper entity attached to the illusioner by Geyser, such as an invisible armor stand, or the removal of invisibility during the illusioner's special attack. The former would be preferable, as it would maintain some degree of functionality for users without the pack.

Currently, the optional pack uses a render controller to perform a simple texture swap on the illusioner. This is accomplished by replacing the evocation illager with the illusioner when the evocation illager returns true for the Molang query `q.is_bribed`. The following texture array is defined in the render controller:

```json
{
  "arrays": {
    "textures": {
      "Array.skins": [
        "Texture.default",
        "Texture.illusioner"
      ]
    }
  }
}
```

The position used in the array for the texture is then defined by `Array.skins[q.is_bribed]`.

The geometry of the evoker was also slightly modified to include the hat of the illusioner. Since Bedrock edition uses the textures of Java edition for all illagers, and the evoker has an unused hat on its Java edition texture, the render controller is also utilized to hide the the render controller is set to hide the helmet layer unless `q.is_bribed` is true. 

### Killer bunnies

The killer bunny does not exist in Bedrock Edition. Nonetheless, this is primarily a simple texture swap. The "caerbannog" texture is the name of the texture in Java Edition, so that name has been used for consistency. This texture is added to the pack and the rabbit entity definition file. In order to construct the Molang query, the "Toast" rabbit must also be considered. In the event a rabbit is named "Toast", the texture is always overridden as the texture "Toast", including in the case of the killer bunny. Therefore, the query to select the texture is constructed with `q.is_bribed` being determined by Geyser:

```json
"textures": [
    "q.get_name == 'Toast' ? Texture.toast : (q.is_bribed ? Texture.caerbannog : Array.skins[q.variant])"
]
```

The texture required for this to be displayed can be retrieved during the build process.

### Offhand Animation

Both Java Edition and Bedrock Edition have offhand support, though Bedrock is very limited in its support. This includes the offhand attack animation. However, Bedrock has support for both custom animations and triggering custom animations from the server using the [AnimateEntityPacket](https://wiki.vg/Bedrock_Protocol#Animate_Entity). Whenever the server indicates that a player entity has swung their offhand, Geyser will indicate that Bedrock should play the offhand animation.

### Particles

The pack replaces many particles that are not displayed for various reasons. Some cannot be displayed due to Bedrock's lack of ability to spawn particles with data from required builtin variables. Others simply do not exist in Bedrock edition. The table below summarizes the particle changes implemented by this pack.

|   Java (`minecraft:`)   |  Bedrock (`minecraft:`) | Optional Pack (`geyseropt:`) |                                           Notes                                          |
|:-----------------------:|:-----------------------:|:----------------------------:|:----------------------------------------------------------------------------------------:|
|          `ash`          |            -            |             `ash`            |                              Not present in Bedrock Edition                              |
|        `barrier`        |            -            |           `barrier`          |                     Present in Bedrock Edition, but not as a particle                    |
|         `bubble`        |  `basic_bubble_manual`  |               -              | Modified version of the basic_bubble_manual particle is used to spawn in all block types |
|     `crimson_spore`     |            -            |        `crimson_spore`       |                              Not present in Bedrock Edition                              |
|    `damage_indicator`   |            -            |      `damage_indicator`      |                              Not present in Bedrock Edition                              |
|     `enchanted_hit`     |            -            |    `enchanted_hit_single`    |                              Not present in Bedrock Edition                              |
|            -            |            -            |   `enchanted_hit_multiple`   |                  Used for playing multiple scattered particles on attack                 |
|         `flash`         |            -            |            `flash`           |                              Not present in Bedrock Edition                              |
|     `landing_honey`     |            -            |        `landing_honey`       |                              Not present in Bedrock Edition                              |
|      `landing_lava`     |            -            |        `landing_lava`        |                              Not present in Bedrock Edition                              |
| `landing_obsidian_tear` |            -            |    `landing_obsidian_tear`   |                              Not present in Bedrock Edition                              |
|        `nautilus`       |            -            |          `nautilus`          |                              Not present in Bedrock Edition                              |
|         `sneeze`        |            -            |           `sneeze`           |  Part of Bedrock Edition as a variant of redstone dust (local use only in optional pack) |
|      `sweep_attack`     |            -            |        `sweep_attack`        |                              Not present in Bedrock Edition                              |
|       `underwater`      |            -            |         `underwater`         |                              Not present in Bedrock Edition                              |
|      `warped_spore`     |            -            |        `warped_spore`        |                              Not present in Bedrock Edition                              |
|       `white_ash`       |            -            |          `white_ash`         |                              Not present in Bedrock Edition                              |

#### Sweep attack

Of note, the texture for the sweep attack particle is built on the CI using [Imagemagick](https://imagemagick.org/script/index.php). This creates what is effectively a sprite sheet, and then the UV of the particle is animated from the particle definition. The UV animation in the particle definition is defined as follows:

```jsonc
{
  "uv": {
		"texture_width": 32, // defines texture width for UV purposes
		"texture_height": 256, // defines texture height for UV purpose
		"flipbook": {
			"base_UV": [0, 0], // notes the point on the UV map at which to start
			"size_UV": [32, 32], // notes the size of the UV emanating from the [+X, +Y] direction of the base_UV point 
			"step_UV": [0, 32], // defines the value by which the UV will be translated on each step, effectively our frame size
			"max_frame": 8, // defines the number of frames in our sprite
			"stretch_to_lifetime": true // defines the length of our UV animation as being equal to that of the particle's lifetime
}
```

The Imagemagick command used to create the sprite on the CI is `convert`, which is the subset of Imagemagick that deals with image manipulation. The command is as follows:

```sh
convert -append extracted/assets/minecraft/textures/particle/sweep_*.png -define png:format=png8 textures/particle/sweep_attack.png
```

The `-append` flag is used to join the input images which match the defined globular expression (`.../sweep_*.png`). The image format is defined for safety as by default Imagemagick will attempt to change the color mode of the image to grayscale, which Minecraft will not interpret correctly. The image is then placed in the pack at the defined path.

### Player skin parts

On Java Edition, you are able to toggle your cape and second skin layers. Bedrock Edition does not share this property. We're able to solve this by applying Java's player skin parts mask to the `q.mark_variant` query and checking the visibility with this formula in the player render controller and cape render controller:

```
math.mod(math.floor(q.mark_variant / 32), 2) != 1
```

Do note that Geyser does invert the bits - that way, on other servers without the GeyserOptionalPack, `q.mark_variant` being 0 means that all parts should be shown. Java interprets 0 to mean all parts are invisible.

Also note that capes are technically possible to implement without the OptionalPack, but this requires re-sending the skin data to Bedrock Edition which would be costly on performance and network usage.

### Shulkers

See https://github.com/GeyserMC/Geyser/issues/1412 for more context.

In Java Edition, when a shulker is invisible, their "box" will be invisible. In Bedrock Edition, no rendering changes occur. In this pack, this is simply fixed by adding a new render controller that toggles the part visibilities of the `base` and `lid` sections if the entity is invisible and `query.is_bribed` is enabled as a server-authoritative flag:

```json
"part_visibility": [
    { "*": true },
    { "lid": "!(q.is_invisible && q.is_bribed)" },
    { "base": "!(q.is_invisible && q.is_bribed)" }
]
```

### Spectral arrow entities

The glowing effect and the spectral arrow item and entities do not exist on Bedrock Edition. However, as the spectral arrow entity is just a retexture of a normal arrow, so by defining a new texture for the arrow entity and setting a query, we can tell Bedrock to replace the texture in the render controller:

```json
"textures": [
    "q.is_bribed ? texture.spectral : texture.default"
]
```

The texture required for this to be displayed can be retrieved during the build process.
