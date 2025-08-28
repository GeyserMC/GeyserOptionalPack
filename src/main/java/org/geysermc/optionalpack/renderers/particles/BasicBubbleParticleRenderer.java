package org.geysermc.optionalpack.renderers.particles;

import org.geysermc.optionalpack.renderers.JsonPatchRenderer;

public class BasicBubbleParticleRenderer extends JsonPatchRenderer {
    public BasicBubbleParticleRenderer() {
        super("Basic Bubble Particle", "particles/basic_bubble_manual.json", "{\"particle_effect\": {\"components\": {\"minecraft:particle_expire_if_not_in_blocks\": []}}}");
    }
}
