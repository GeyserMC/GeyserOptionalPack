<!--ts-->
   * [Introduction](#Introduction)
   * [Armor stands](#Armor-stands)
      * [Part visibility and rotation encoding](#Part-visibility-and-rotation-encoding)
      * [Geometry and attachables](#Geometry-and-attachables)
   * [Illusioners](#Illusioners)
   * [Killer bunnies](#Killer-bunnies)
   * [Offhand Animation](#Offhand-animation)
   * [Particles](#Particles)
      * [Sweep Attack](#Sweep-attack)
   * [Phantoms](#Phantoms)
   * [Player skin parts](#Player-skin-parts)
   * [Shulkers](#Shulkers)
   * [Spectral arrow entities](#Spectral-arrow-entities)
   * [Spyglass animations](#Spyglass-animations)
   * [Zombie villager textures](#Zombie-villager-textures)
   * [UI modifications](#ui-modifications)
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

Geyser scales down small armor stands to 0.5. However, this has the unintended side effect of making the head slot appear smaller than it does on Java Edition. Consequently, the query `q.is_baby` is applied when an armor stand is small, which is then used to trigger an animation which upscales the head slot by 1.3984. This value was derived from the [attached chart](https://cdn.discordapp.com/attachments/338267383904993291/417695114249371648/unknown.png) linked in the Blockbench Discord. Since large armor stand head slots have a scale of 0.625 to one block, downscaling by half gives a scale of 0.3125 to one block. The proper scale, per the chart, is 0.437. Thus, the slot must be upscaled by 1.3984 to compensate, which is derived by dividing 0.437 by 0.3125. Behavior pack scale is not applied to heads, pumpkins, and banners, which do render in the head slot in vanilla Bedrock. Because the downscaling is not applied to these, a Molang expression is used to set the scale to use. When a head or carved pumpkin is placed in the head slot, the scaling is done at half of 1.3984 to account for the fact that the initial downscaling was never applied. Banner scaling for small armor stands cannot be fixed at this time due to their attachment occurring above the root bone of the model, as they must use the chestplate slot on Bedrock.

```c
v.head_scale = q.is_item_name_any('slot.armor.head', 0, 'minecraft:skull', 'minecraft:carved_pumpkin') ? 0.6992 : 1.3984;
```

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

### Phantoms

Because Geyser does not utilize the 1.17.40 subchunk system, phantom trail particles are attached to the player entity and fail to despawn when a phantom spawner is present. This seems to be related to how the client perceive the phantom "entity" attached to the spawner. This can be mitigated by adding an additional state to the phantom's animation controller, which ensures its trail particle is only played when the phantom is in motion.

```json
{
  "controller.animation.phantom.base_pose": {
    "initial_state": "default",
    "states": {
        "default": {
            "transitions": [
                {
                    "moving": "q.is_moving"
                }
            ],
            "animations": [ "phantom_base_pose" ]
        },
        "moving": {
            "animations": [ "phantom_base_pose" ],
            "particle_effects": [
                {
                    "effect": "wing_dust",
                    "locator": "left_wing"
                },
                {
                    "effect": "wing_dust",
                    "locator": "right_wing"
                }
            ]
        }
    }
  }
}
```

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

### Spyglass animations

The spyglass does not display at all in the offhand on Bedrock edition. Consequently, when using /geyser offhand, the spyglass simply appears in the main hand. This is also true of Bedrock players viewing Java players with an offhand spyglass. The animation for offhand use of the spyglass also does not play properly. Multiple changes are required to remedy this.

The spyglass effectively functions as an attachable, but has no user-facing attachable definition. Nonetheless, many of the principles of attachables can still be applied.

Firstly, the spyglass geometry must be bound to offhand of the player when proper. This is possible with the 1.16.0 geometry format, which has the field "binding". The original spyglass binding expression only allows it to be bound to the mainhand or head. It is replaced with the following expression. The query `q.is_emerging` is set by Geyser specifically for OptionalPack when the client is using an offand item, and is therefore effectively a proxy for `q.main_hand_item_use_duration > 0`. The query `q.is_item_name_any` is then used to check which hand slot the spyglass resides in.

```json
{
  "binding": "q.item_slot_to_bone_name((q.main_hand_item_use_duration || q.is_emerging) > 0.0f ? 'head' : (q.is_item_name_any('slot.weapon.mainhand', 0, 'minecraft:spyglass') ? 'main_hand' : 'off_hand'))"
}
```

Next, the vanilla player animation controller must be modified to account for the possibility the spyglass is in the offhand.When in the mainhand, the hardcoded variable `variable.is_holding_spyglass` is used to check for the use of the spyglass in the mainhand, as a proxy for this in the offhand, using the Geyser specific query defined above, the following Molang expression is used to check for the use of the spyglass in the offhand. It is used at various points in the animation controller, including to engage the spyglass animation, cancel the bobbing animation, and cancel other item use animations.

```c
(q.is_item_name_any('slot.weapon.offhand', 0, 'minecraft:spyglass') && q.is_emerging)
```

The player animation itself must be modified to account for the axis flip of the players arm holding the spyglass on the opposite side. This simply requires flipping the Y and Z axis of rotation, as well as ensuring the slots are use of mainhand and offhand is properly distinguished.

```json

{
  "rightarm" : {
    "rotation" : [ 
      "q.is_item_name_any('slot.weapon.mainhand', 0, 'minecraft:spyglass') ? math.clamp(query.target_x_rotation - 105 - (v.is_sneaking ? 15 : 0), -170, 180)", 
      "q.is_item_name_any('slot.weapon.mainhand', 0, 'minecraft:spyglass') ? math.clamp(q.target_y_rotation - 15, -60, 90)", 
      "q.is_item_name_any('slot.weapon.mainhand', 0, 'minecraft:spyglass') ? 5.0"
      ]
  },
  "leftarm" : {
    "rotation" : [ 
      "q.is_item_name_any('slot.weapon.offhand', 0, 'minecraft:spyglass') ? math.clamp(query.target_x_rotation - 105 - (v.is_sneaking ? 15 : 0), -170, 180)", 
      "q.is_item_name_any('slot.weapon.offhand', 0, 'minecraft:spyglass') ? math.clamp(q.target_y_rotation + 15, -60, 90)", 
      "q.is_item_name_any('slot.weapon.offhand', 0, 'minecraft:spyglass') ? -5.0"
      ]
  }
}
```

Lastly, the spyglass animation must be modified to account for the spyglass being on the opposite side of the player's head. Unfortunately, the scoping animation cannot be used for this, as its triggering appears to be hardcoded on the client side. Instead, the holding animation is conditionally modified when `q.is_emerging` is true, meaning the spyglass is in use.

```json
{
  "spyglass": {
    "position": [ 
      "c.is_first_person ? 2.0 : (q.is_emerging ? 3.0 : 1.0)", 
      "c.is_first_person ? 25.0 : (q.is_emerging ? 27.0 : 22.0)", 
      "c.is_first_person ? -1.0 : (q.is_emerging ? -3.0 : 0.0)" 
      ],
    "rotation": [ 
      "c.is_first_person ? 58.0 : 0.0", 
      "c.is_first_person ? -48.0 : -90.0", 
      "c.is_first_person ? -44.0 : 0.0" 
      ]
  }
}
```

Unfortunately, the spyglass cannot actually be used in the offhand by Bedrock players, as the triggering of the first person "animation" for it is hardcoded on the client side. However, these changes allow the spyglass to be properly displayed when in the offhand of a Bedrock player, as well as when used in the offhand of a Java player. Furthermore, `q.is_emerging` could be utilized by other resource pack creators working with Geyser to identify if an item is being used in the offhand.


### Zombie villager textures

Like villagers, zombie villagers in Java Edition have visible biome and profession variants. It appears that initial implementation of this was started in the Bedrock vanilla resources, given the presence of the entity with the identifier `minecraft:zombie_villager_v2`. However, the textures specified in this vanilla entity definition appear to be entirely blank TGA files. Luckily, the profession textures of zombie villagers and villagers are essentially identical, so the entity definition was updated to reference the villager profession textures. 
Zombie villagers, like villagers, have a profession level. This is implemented by adding the same vanilla render controller used to create this effect in the villager entity, `controller.render.villager_v2_level`. The remainder of the entity definition is unchanged.

### UI modifications
Some inventories have added functionality on Bedrock, that does not exist on Java edition. For example, this includes:
- 2x2 crafting grid while in creative mode
- An option to rename maps in the cartography table
- Command block renaming or enabling/setting command block execution delays

To resolve this issue, this pack uses Json UI modification on these inventory UIs. Here's how:

`cartography_screen.json`
```json
{
  "text_box_panel": {
    "ignored": true
  }
}
```

This hides the renaming field in the cartography table that cannot be used. This does not modify the textures or functionality, 
but instead just alters the visual appearance for Bedrock players.

Hiding the 2x2 crafting grid is a bit more involved. We have to use bindings to only conditionally hide the 2x2 grid when we are in creative mode:
`inventory_screen.json`
```json
{
  "crafting_panel_2x2": {
    "modifications": [
      {
        "array_name": "bindings",
        "operation": "insert_front",
        "value": {
          "binding_name": "(not #is_creative_mode)",
          "binding_name_override": "#visible"
        }
      }
    ]
  }
}
```

This uses the `#is_creative_mode` binding, and applies it to the crafting panel. Note that we insert this modification into the bindings array
instead of directly modifying the UI - this allows the GeyserOptionalPack to stay compatible with other resource packs that modify this screen.