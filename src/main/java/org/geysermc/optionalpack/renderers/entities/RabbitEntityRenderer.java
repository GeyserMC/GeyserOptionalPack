package org.geysermc.optionalpack.renderers.entities;

import org.geysermc.optionalpack.renderers.JsonPatchRenderer;

public class RabbitEntityRenderer extends JsonPatchRenderer {
    public RabbitEntityRenderer() {
        super("Rabbit Entity", "entity/rabbit.entity.json", "{\"minecraft:client_entity\": {\"description\": {\"textures\": {\"caerbannog\": \"textures/geyser/entity/rabbit/caerbannog\"}}}}");
    }
}
