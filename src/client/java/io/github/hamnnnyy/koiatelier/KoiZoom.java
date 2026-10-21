package io.github.hamnnnyy.koiatelier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.MathHelper;

// 키를 누르고 있는 동안 확대 (옵티파인 줌처럼 부드러운 카메라 사용)
public class KoiZoom {
    private final KoiConfig config;
    private final KeyBinding key;
    private double factor = 1, prevFactor = 1;
    private boolean zooming;
    private boolean savedSmoothCamera;

    public KoiZoom(KoiConfig config, KeyBinding key) {
        this.config = config;
        this.key = key;
    }

    public void tick(MinecraftClient mc) {
        boolean held = key.isPressed() && mc.currentScreen == null;
        if (held != zooming) {
            if (held) {
                savedSmoothCamera = mc.options.smoothCameraEnabled;
                mc.options.smoothCameraEnabled = true;
            } else {
                mc.options.smoothCameraEnabled = savedSmoothCamera;
            }
            zooming = held;
        }
        prevFactor = factor;
        double target = held ? config.zoomAmount : 1;
        factor += (target - factor) * 0.5;
        if (Math.abs(factor - target) < 0.01) factor = target;
    }

    // 시야각을 이 값으로 나눔 (1이면 그대로)
    public double fovDivisor(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevFactor, factor);
    }
}
