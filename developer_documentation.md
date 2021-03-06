<!--ts-->
   * [Introduction](#Introduction)
   * [Armor stands](#Armor-stands)
      * [Part visibility and rotation encoding](#Part-visibility-and-rotation-encoding)
      * [Geometry and attachables](#Geometry-and-attachables)
   * [Iron golems](#Iron-golems)
      * [Cracking](#Cracking)
      * [Materials](#Materials)
      * [Render controller](Render-controllers)
   * [Killer bunnies](#Killer-bunnies)
   * [Shulkers](#Shulkers)
   * [Spectral arrow entities](#Spectral-arrow-entities)
<!--te-->

### Introduction

Entity data and entity flags (known as queries in Molang) are pieces of metadata that store various pieces of information about an entity on the Bedrock Edition of Minecraft. You have a query for an entity's health, for example (a number query, or an entity data), and you have a query for is an entity is angry (an entity flag, which is either 1.0 or 0.0 in Molang). Not all entities use every query, but every entity has access to most queries, though Bedrock by default ignores these. We use this to our advantage in this resource pack.

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

Iron golems in Java edition experience "cracking" as health decreases. Cracking overlays a texture that contains cracks on the base iron golem texture.  This occurs in four ranges, in which health values of 100-76 experience no cracking, 75-51 experience low cracking, 50-26 experience medium cracking, and 25-1 experience high cracking. Using this material, a render controller can be utilized to select textures defined in the entity definition file from an array.

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

Unfortunately, the "USE_COLOR_MASK" and "MULTIPLICATIVE_TINT_COLOR" definitions rely on certain entity data being sent. Geyser does not send all of this data for every entity, as under vanilla conditions, it does not server a purpose. For more information, refer to [EntityData.java](https://github.com/CloudburstMC/Protocol/blob/develop/bedrock/bedrock-common/src/main/java/com/nukkitx/protocol/bedrock/data/entity/EntityData.java) in the [CloudburstMC/Protocol](https://github.com/CloudburstMC/Protocol) repository. The lack of this data likely results in a null value being interpreted by the material, leading to a rendering with entirely black pixels. Originally, custom materials were defined that removed the "USE_COLOR_MASK" and "MULTIPLICATIVE_TINT_COLOR" parameters. However, custom materials have been largely broken on Windows 10 devices by the introduction of Render Dragon, which seems to completely remove the ability to define or edit materials in a data-driven fashion. Therefore, Geyser was modified to send the entity component "COLOR_2". This allows for the use of the tropical fish material over Geyser. Due to Render Dragon, this pack will not be able to move ahead with features that require custom materials unless Render Dragon is changed to allow for modification of shaders and material assets.

#### Render controller

In order to utilize multiple textures, a render controller containing a texture array was defined. A position in the texture array is then determined by the following Molang expression:

```json
"textures": [
    "(q.health > 99 || !q.is_bribed) ? 3 : math.floor(q.health / 25)"
    ]
```

The trinary operator ensures that even if `max_health`, defined at 100, is overflowed, the expression will never produce a value outside the range of 0-3. As all data is derived resource pack side, this addition requires no modification by the server (though `query.is_bribed` enables the feature). The textures required for this to display can be retrieved during the build process.

### Killer bunnies

The killer bunny does not exist in Bedrock Edition. Nonetheless, this is primarily a simple texture swap. The "caerbannog" texture is the name of the texture in Java Edition, so that name has been used for consistency. This texture is added to the pack and the rabbit entity definition file. In order to construct the Molang query, the "Toast" rabbit must also be considered. In the event a rabbit is named "Toast", the texture is always overridden as the texture "Toast", including in the case of the killer bunny. Therefore, the query to select the texture is constructed as follows, with `q.is_bribed` being determined by Geyser:

```json
"textures": [
    "q.get_name == 'Toast' ? Texture.toast : (q.is_bribed ? Texture.caerbannog : Array.skins[q.variant])"
]
```

The texture required for this to be displayed can be retrieved during the build process.

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

The glowing effect and the spectral arrow item and entities do not exist on Bedrock Edition. However, as the spectral arrow entity is just a retexture of a normal arrow, so by defining a new texture for the arrow entity and setting a query we can tell Bedrock to replace the texture in the render controller:

```json
"textures": [
    "q.is_bribed ? texture.spectral : texture.default"
]
```

The texture required for this to be displayed can be retrieved during the build process.
