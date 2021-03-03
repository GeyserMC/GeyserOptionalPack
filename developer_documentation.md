### Armor stands

Entity data and entity flags (known as queries in Molang) are pieces of metadata that store various pieces of information about an entity on the Bedrock Edition of Minecraft. You have a query for an entity's health, for example (a number query, or an entity data), and you have a query for is an entity is angry (an entity flag, which is either 1.0 or 0.0 in Molang). Not all entities use every query, but every entity has access to most queries, though Bedrock by default ignores these. We use this to our advantage in this resource pack.

Two flags are designated for toggling an armor stand baseplate and arms. If `query.is_angry` is set to true, the render controller will not render arms on an armor stand. If `query.is_admiring` is set to true, then the armor stand will not render its baseplate. Bedrock without resource packs does not care about these values, so any setup without this resource pack will not break.

In order to easily compress and send over rotation values over the network, we cut off the float rotation values in favor of integer values. The original implementation for each rotation of a limb was set up like the following:

```
query.example = XXXYYYZZZ
```

The first three digits were designated for the X, the second three for Y, and the final three for Z. Each one went from 0 to 360 - allowing a full rotation on each axis. Unfortunately, Bedrock has some unknown integer limit that cuts off such a large number, so a new system was designed. The final product is as follows:

```
query.example = BXXYYZZ
query.example_flag_one = 1.0;
query.example_flag_two = 1.0;
query.example_flag_three = 0.0;
```

B (the first digit of the number) is set up like binary - if 4 is added, then the X rotation should be added by 100; same for Y (2) and Z (1). The following numbers have the remaining value, up to 180 degrees, and are added on top of whatever B determines for that rotation. In order to compensate for the lack of range, three flags are set aside for each rotation to determine if a number is negative. If the corresponding flag of each axis is true, then the number is toggled as negative. This could also have been implemented as binary in the number, but was not implemented due to precision concerns.
