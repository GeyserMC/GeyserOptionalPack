package org.geysermc.optionalpack.renderers.entities;

import org.geysermc.optionalpack.renderers.JsonPatchRenderer;

public class EvocationIllagerEntityRenderer extends JsonPatchRenderer {
    public EvocationIllagerEntityRenderer() {
        super("Evocation Illager Entity", "entity/evocation_illager.entity.json", "{\"minecraft:client_entity\": {\"description\": {\"textures\": {\"illusioner\": \"textures/geyser/entity/illager/illusioner\"}}}}");
    }
}
