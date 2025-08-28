package org.geysermc.optionalpack.renderers.entities;

import org.geysermc.optionalpack.renderers.JsonPatchRenderer;

public class ArrowEntityRenderer extends JsonPatchRenderer {
    public ArrowEntityRenderer() {
        super("Arrow Entity", "entity/arrow.entity.json", "{\"minecraft:client_entity\": {\"description\": {\"textures\": {\"spectral\": \"textures/geyser/entity/arrow/spectral_arrow\"}}}}");
    }
}
