package io.github.hamnnnyy.koiatelier.mixin;

import io.github.hamnnnyy.koiatelier.KoiAtelierClient;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// 풀 브라이트: 밝기(감마) 값을 아주 크게 읽게 함. 설정 파일의 밝기 값은 건드리지 않음
@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    // update() 안에서 두 번째 SimpleOption.getValue() 호출이 밝기 옵션 (첫 번째는 어둠 효과 강도)
    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 1))
    private Object koi$fullbright(Object gamma) {
        KoiAtelierClient koi = KoiAtelierClient.get();
        return koi != null && koi.config().fullbright ? (Object) 15.0 : gamma;
    }
}
