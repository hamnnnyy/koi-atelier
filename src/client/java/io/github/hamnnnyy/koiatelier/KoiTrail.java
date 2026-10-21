package io.github.hamnnnyy.koiatelier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

// 걸어간 자리에 파티클 발자국을 남김 (나한테만 보임)
public class KoiTrail {
    private final KoiConfig config;
    private Vec3d lastPos;
    private double pending = 0;

    public KoiTrail(KoiConfig config) {
        this.config = config;
    }

    public void tick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || mc.isPaused()) return;
        Vec3d now = player.getPos();
        Vec3d from = lastPos;
        lastPos = now;
        if (!config.trailEnabled || from == null || player.isInvisible() || player.isSpectator()) return;

        double moved = MathHelper.hypot(now.x - from.x, now.z - from.z);
        if (moved < 0.02 || moved > 4) return; // 가만히 있거나 순간이동

        pending += moved * config.trailDensity * 3;
        int count = (int) pending;
        pending -= count;

        ParticleEffect particle = config.trailType.particle(config.trailColor);
        Random random = player.getRandom();
        for (int i = 0; i < count; i++) {
            // 지난 틱과 이번 틱 사이에 고르게 뿌림
            Vec3d p = from.lerp(now, random.nextDouble());
            mc.world.addParticle(particle,
                    p.x + (random.nextDouble() - 0.5) * 0.3, p.y + 0.05, p.z + (random.nextDouble() - 0.5) * 0.3,
                    0, 0.005, 0);
        }
    }
}
