package io.github.hamnnnyy.koiatelier;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

// 왼쪽 어깨 옆을 둥실둥실 따라다니는 펫 (월드에 추가하지 않고 렌더링만, 나한테만 보임)
public class KoiPet {
    private final KoiConfig config;
    private LivingEntity pet;
    private PetType type;
    private Vec3d pos, prevPos;
    private float yaw, prevYaw;

    public KoiPet(KoiConfig config) {
        this.config = config;
    }

    public void tick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (!config.petEnabled || player == null || mc.world == null) {
            pet = null;
            return;
        }
        if (pet == null || type != config.petType || pet.getWorld() != mc.world) {
            type = config.petType;
            pet = type.entityType.create(mc.world);
            pos = null;
            if (pet == null) return;
        }

        float bodyYaw = player.bodyYaw * MathHelper.RADIANS_PER_DEGREE;
        double rightX = -MathHelper.cos(bodyYaw), rightZ = -MathHelper.sin(bodyYaw);
        double bob = Math.sin(player.age * 0.1) * 0.1;
        // 왼쪽 어깨 옆 (뒤에 두면 정면에서 몸에 가려 안 보임)
        Vec3d target = player.getPos().add(-rightX * 0.9, player.getHeight() * 0.7 + bob, -rightZ * 0.9);

        // 부드럽게 따라가고, 순간이동 등으로 멀어지면 바로 옆으로
        if (pos == null || pos.squaredDistanceTo(target) > 64) {
            pos = prevPos = target;
            yaw = prevYaw = player.bodyYaw;
        } else {
            prevPos = pos;
            pos = pos.add(target.subtract(pos).multiply(0.25));
            prevYaw = yaw;
            yaw = MathHelper.lerpAngleDegrees(0.3f, yaw, player.bodyYaw);
        }

        pet.age++;
        // 움직이면 걷는 애니메이션
        pet.limbAnimator.updateLimbs((float) Math.min(pos.distanceTo(prevPos) * 6, 1), 0.4f);
    }

    public void render(WorldRenderContext ctx, EntityRenderDispatcher dispatcher, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (pet == null || pos == null || !config.petEnabled) return;
        if (!config.firstPerson && mc.options.getPerspective().isFirstPerson()) return;

        Vec3d p = prevPos.lerp(pos, tickDelta);
        float renderYaw = MathHelper.lerpAngleDegrees(tickDelta, prevYaw, yaw);
        pet.setPosition(p);
        pet.prevX = p.x;
        pet.prevY = p.y;
        pet.prevZ = p.z;
        pet.setYaw(renderYaw);
        pet.prevYaw = renderYaw;
        pet.bodyYaw = pet.prevBodyYaw = renderYaw;
        pet.headYaw = pet.prevHeadYaw = renderYaw;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack matrices = ctx.matrixStack();
        float scale = (float) config.petSize * type.scale;
        matrices.push();
        matrices.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
        matrices.scale(scale, scale, scale);
        dispatcher.render(pet, 0, 0, 0, renderYaw, tickDelta, matrices, ctx.consumers(), dispatcher.getLight(pet, tickDelta));
        matrices.pop();
    }
}
