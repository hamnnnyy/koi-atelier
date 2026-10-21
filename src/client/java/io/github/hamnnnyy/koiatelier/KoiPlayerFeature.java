package io.github.hamnnnyy.koiatelier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.RotationAxis;

// 내 캐릭터 모델에 붙는 후광과 날개 (도트 그림을 입체 블록으로). 모델 좌표계라서 웅크리기, 고개 돌리기를 그대로 따라감
// (모델 좌표: 목 위치가 원점, y가 아래쪽으로 +, 1 = 1블록, 등 쪽이 +z)
public class KoiPlayerFeature extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private static final float PIXEL = 1 / 16f; // 마크 모델과 같은 픽셀 크기

    private final KoiConfig config;

    public KoiPlayerFeature(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, KoiConfig config) {
        super(context);
        this.config = config;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, AbstractClientPlayerEntity player,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (player != MinecraftClient.getInstance().player || player.isInvisible()) return;
        VertexConsumer vc = consumers.getBuffer(KoiSprite.LAYER);
        if (config.haloEnabled) renderHalo(matrices, vc, animationProgress);
        if (config.wingsEnabled) renderWings(matrices, vc, light, player, animationProgress);
    }

    private void renderHalo(MatrixStack matrices, VertexConsumer vc, float time) {
        KoiSprite halo = KoiSprites.HALO;
        float px = PIXEL * 0.85f * (float) config.haloSize;
        matrices.push();
        getContextModel().head.rotate(matrices); // 고개를 따라 기울어짐
        float bob = (float) Math.sin(time * 0.08) * 0.03f;
        matrices.translate(0, -0.7 + bob, 0);     // 머리 꼭대기(-0.5) 조금 위
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * 1.5f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90)); // 눕히기
        matrices.translate(-halo.width * px / 2, -halo.height * px / 2, -px / 2);
        // 빛나는 느낌으로 항상 최대 밝기
        halo.render(matrices.peek(), vc, px, false, tint(config.haloColor), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        matrices.pop();
    }

    private void renderWings(MatrixStack matrices, VertexConsumer vc, int light, AbstractClientPlayerEntity player, float time) {
        WingType type = config.wingsType;
        KoiSprite sprite = type.sprite;
        // 날거나 떨어질 때는 빠르게 퍼덕임
        boolean airborne = player.getAbilities().flying || player.isFallFlying() || !player.isOnGround();
        float flap = (float) Math.sin(time * (airborne ? 0.45 : 0.12)) * type.flapAngle;
        float open = (airborne ? 15f : 30f) + flap; // 0도면 활짝 펴짐, 클수록 뒤로 접힘
        int color = tint(config.wingsColor);

        matrices.push();
        getContextModel().body.rotate(matrices);
        matrices.translate(0, 0.25, 0.14); // 등 가운데, 날개뼈 높이
        for (int side : new int[]{1, -1}) {
            matrices.push();
            matrices.translate(side * 0.06, 0, 0);
            // 바깥쪽 끝이 뒤(+z)로 가게 회전
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-side * open));
            matrices.translate(0, -sprite.height * PIXEL / 2, 0); // 그림 세로 가운데가 날개뼈 높이
            sprite.render(matrices.peek(), vc, PIXEL, side < 0, color, light);
            matrices.pop();
        }
        matrices.pop();
    }

    private static int tint(DyeColor color) {
        return color.getEntityColor() & 0xFFFFFF;
    }
}
