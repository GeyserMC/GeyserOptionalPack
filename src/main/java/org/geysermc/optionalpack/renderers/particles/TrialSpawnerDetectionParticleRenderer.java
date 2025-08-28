package org.geysermc.optionalpack.renderers.particles;

import org.geysermc.optionalpack.renderers.JsonPatchRenderer;

public class TrialSpawnerDetectionParticleRenderer extends JsonPatchRenderer {
    public TrialSpawnerDetectionParticleRenderer() {
        super("trial_spawner_detection Particle", "particles/trial_spawner_detection.particle.json", "{\"particle_effect\": {\"components\": {\"minecraft:emitter_rate_instant\": {\"num_particles\": \"1\"}, \"minecraft:emitter_shape_box\": {\"offset\": [0, 0, 0]}}}}");
    }
}
