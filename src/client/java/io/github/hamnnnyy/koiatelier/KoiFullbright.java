package io.github.hamnnnyy.koiatelier;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.lang.reflect.Method;

// 풀 브라이트. 보통은 LightmapTextureManagerMixin이 밝기(감마)를 올려서 처리하지만,
// 셰이더(Iris)는 감마를 무시하고 빛을 직접 계산하므로 그때는 내 화면에만 야간 투시 효과를 걸어서 밝게 함
public class KoiFullbright {
    private final KoiConfig config;
    private final Method shaderInUse = findIrisCheck();
    private boolean applied;

    public KoiFullbright(KoiConfig config) {
        this.config = config;
    }

    public void tick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            applied = false;
            return;
        }
        boolean want = config.fullbright && shadersActive();
        StatusEffectInstance current = player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (want && current == null) {
            // 무한 지속, 입자와 화면 아이콘 없음. 서버는 모름 (클라이언트에만 있음)
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 0, true, false, false));
            applied = true;
        } else if (!want && applied) {
            // 우리가 건 것만 지움 (물약으로 받은 진짜 야간 투시는 그대로)
            if (current != null && current.isInfinite() && !current.shouldShowIcon()) player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            applied = false;
        }
    }

    private boolean shadersActive() {
        if (shaderInUse == null) return false;
        try {
            Object api = shaderInUse.getDeclaringClass().getMethod("getInstance").invoke(null);
            return (boolean) shaderInUse.invoke(api);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    // Iris가 설치돼 있을 때만 IrisApi.isShaderPackInUse() 를 씀 (컴파일 의존성 없이)
    private static Method findIrisCheck() {
        if (!FabricLoader.getInstance().isModLoaded("iris")) return null;
        try {
            return Class.forName("net.irisshaders.iris.api.v0.IrisApi").getMethod("isShaderPackInUse");
        } catch (ReflectiveOperationException e) {
            KoiAtelierClient.LOGGER.warn("Iris found but its API is missing, fullbright will not work with shaders", e);
            return null;
        }
    }
}
