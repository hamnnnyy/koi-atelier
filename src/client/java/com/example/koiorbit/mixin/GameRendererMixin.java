package com.example.koiorbit.mixin;

import com.example.koiorbit.KoiOrbitClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 줌: 월드를 그릴 때의 시야각을 줄임 (손은 그대로)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void koi$zoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        KoiOrbitClient koi = KoiOrbitClient.get();
        if (changingFov && koi != null) cir.setReturnValue(cir.getReturnValue() / koi.zoom().fovDivisor(tickDelta));
    }
}
