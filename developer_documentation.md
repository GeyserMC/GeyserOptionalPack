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

Above, `query.owner_identifier` returns the identifier of the entity to which the attachable is applied, and thus this attachble will only be applied when the owner identifier is `minecraft:armor_stand`.

### Iron golems

Iron golems in Java edition experience "cracking" as health decreases. Cracking overlays a texture that contains cracks on the base iron golem texture.  This occurs in four ranges, in which health values of 100-76 experience no cracking, 75-51 experience low cracking, 50-26 experience medium cracking, and 25-1 experience high cracking. The material of most bedrock vanilla entities does not allow for type of texture overlay switching. However, the material of the tropical fish, "tropicalfish", allows for this manner of texture overlay switching. Using this material, a render controller can be utilized to select textures defined in the entity definition file from an array. A position in the texture array is then determined by the following Molang expression:

```c
q.health > 99 ? 3 : math.floor(q.health / 25)
```

The trinary operator ensures that even if `max_health`, defined at 100, is overflowed, the expression will never produce a value outside the range of 0-3. As all data is derived resource pack side, this addition requires no modification by the server (though `query.is_bribed` enables the feature). Currently, the textures provided by the array are blank due to copyright concerns. Eventually, some method of obtaining these assets will be added.

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
