package cn.sh1rocu.simplebedrockmodel.client.port;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;

public final class IrisCompat {
    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("iris");

    private IrisCompat() {
    }

    public static boolean isLoaded() {
        return LOADED;
    }

    public static RenderPipeline assign(RenderPipeline pipeline, String program) {
        if (LOADED) {
            Bridge.assign(pipeline, program);
        }
        return pipeline;
    }

    public static boolean isShaderPackInUse() {
        return LOADED && Bridge.shaderPackInUse();
    }

    public static boolean isRenderingShadowPass() {
        return LOADED && Bridge.shadowPass();
    }

    private static final class Bridge {
        static void assign(RenderPipeline pipeline, String program) {
            IrisApi.getInstance().assignPipeline(pipeline, IrisProgram.valueOf(program));
        }

        static boolean shaderPackInUse() {
            return IrisApi.getInstance().isShaderPackInUse();
        }

        static boolean shadowPass() {
            return IrisApi.getInstance().isRenderingShadowPass();
        }
    }
}
