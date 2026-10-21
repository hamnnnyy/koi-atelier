package io.github.hamnnnyy.koiatelier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

// 내 캐릭터 주변에 파티클을 뿌리는 오라 (나한테만 보임)
public class KoiAura {
    private final KoiConfig config;
    private double pending = 0; // 소수점 밀도를 틱마다 누적

    public KoiAura(KoiConfig config) {
        this.config = config;
    }

    public void tick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || mc.isPaused()) return;
        if (!config.enabled || !config.auraEnabled || player.isInvisible() || player.isSpectator()) return;
        if (!config.firstPerson && mc.options.getPerspective().isFirstPerson()) return;

        pending += config.auraDensity;
        if (pending < 1) return;
        int count = (int) pending;
        pending -= count;

        ParticleEffect particle = config.auraType.particle(config.auraColor);
        Random random = player.getRandom();
        float yawRad = player.bodyYaw * MathHelper.RADIANS_PER_DEGREE;
        // 캐릭터 오른쪽 방향
        double sideX = -MathHelper.cos(yawRad);
        double sideZ = -MathHelper.sin(yawRad);
        double h = player.getHeight();

        for (int i = 0; i < count; i++) {
            double x = player.getX(), y = player.getY(), z = player.getZ();
            switch (config.auraPosition) {
                case SHOULDERS -> {
                    double side = random.nextBoolean() ? 0.32 : -0.32;
                    x += sideX * side + (random.nextDouble() - 0.5) * 0.15;
                    y += h * 0.78 + random.nextDouble() * 0.1;
                    z += sideZ * side + (random.nextDouble() - 0.5) * 0.15;
                }
                case BODY -> {
                    double a = random.nextDouble() * Math.PI * 2;
                    x += Math.cos(a) * 0.45;
                    y += random.nextDouble() * h * 0.9;
                    z += Math.sin(a) * 0.45;
                }
                case FEET -> {
                    double a = random.nextDouble() * Math.PI * 2;
                    double r = 0.2 + random.nextDouble() * 0.3;
                    x += Math.cos(a) * r;
                    y += 0.05;
                    z += Math.sin(a) * r;
                }
            }
            // 살짝 위로 피어오르게
            mc.world.addParticle(particle, x, y, z,
                    (random.nextDouble() - 0.5) * 0.01, 0.02 + random.nextDouble() * 0.03, (random.nextDouble() - 0.5) * 0.01);
        }
    }
}
