package io.github.hamnnnyy.koiatelier.mixin;

import io.github.hamnnnyy.koiatelier.KoiConfig;
import io.github.hamnnnyy.koiatelier.KoiAtelierClient;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// 망토: 내 캐릭터의 망토 텍스처를 바꿔치기. 그리기는 바닐라 망토/엘리트라가 그대로 함 (나한테만 보임)
@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
    @ModifyReturnValue(method = "getSkinTextures", at = @At("RETURN"))
    private SkinTextures koi$cape(SkinTextures original) {
        KoiAtelierClient koi = KoiAtelierClient.get();
        if (koi == null || (Object) this != MinecraftClient.getInstance().player) return original;
        KoiConfig config = koi.config();
        if (!config.capeEnabled) return original;
        return new SkinTextures(original.texture(), original.textureUrl(), config.capeDesign.texture,
                original.elytraTexture(), original.model(), original.secure());
    }
}
