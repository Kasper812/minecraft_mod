package cn.sh1rocu.simplebedrockmodel.client.port;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.particle.v1.ParticleGroupRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

import java.util.LinkedHashMap;
import java.util.Map;

public class LegacyParticleGroup extends ParticleGroup<LegacyParticle> {
    public static final ParticleRenderType TYPE = new ParticleRenderType("simplebedrockmodel:legacy", "SBML");
    private static final PoseStack IDENTITY = new PoseStack();
    private static boolean registered;

    public LegacyParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    public static synchronized void register() {
        if (!registered) {
            registered = true;
            ParticleGroupRegistry.register(TYPE, LegacyParticleGroup::new);
        }
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
        Map<RenderType, RecordedGeometry> geometry = new LinkedHashMap<>();
        for (LegacyParticle particle : this.particles) {
            RenderType type = particle.getLegacyRenderType();
            if (type == null) {
                continue;
            }
            particle.render(geometry.computeIfAbsent(type, t -> new RecordedGeometry()), camera, partialTickTime);
        }
        return new State(geometry);
    }

    private record State(Map<RenderType, RecordedGeometry> geometry) implements ParticleGroupRenderState {
        @Override
        public void submit(SubmitNodeCollector collector, CameraRenderState camera) {
            for (Map.Entry<RenderType, RecordedGeometry> entry : geometry.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    collector.submitCustomGeometry(IDENTITY, entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
