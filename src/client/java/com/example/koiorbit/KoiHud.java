package com.example.koiorbit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// 화면 표시: 알림 메시지, 좌표/방향/바이옴, 내구도 경고, 미니맵
public class KoiHud {
    private final KoiConfig config;
    private final KoiMinimap minimap;

    private Text message;
    private int messageColor;
    private int messageTicks = 0;
    private int messageDuration = 1;

    // 칸마다 이미 경고한 아이템 (같은 아이템으로 계속 울리지 않게)
    private final Map<EquipmentSlot, ItemStack> warned = new EnumMap<>(EquipmentSlot.class);

    public KoiHud(KoiConfig config) {
        this.config = config;
        this.minimap = new KoiMinimap(config);
    }

    // 핫바 위에 잠깐 띄우는 알림
    public void show(Text text, int color, int ticks) {
        message = text;
        messageColor = color;
        messageTicks = messageDuration = ticks;
    }

    public void tick(MinecraftClient mc) {
        if (messageTicks > 0) messageTicks--;
        minimap.tick(mc);
        tickDurability(mc);
    }

    private void tickDurability(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null || !config.durabilityEnabled) {
            warned.clear();
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty() || !stack.isDamageable()) {
                warned.remove(slot);
                continue;
            }
            int left = stack.getMaxDamage() - stack.getDamage();
            boolean low = left * 100.0 / stack.getMaxDamage() <= config.durabilityPercent;
            ItemStack last = warned.get(slot);
            if (!low) {
                warned.remove(slot);
            } else if (last == null || !ItemStack.areItemsEqual(last, stack)) {
                warned.put(slot, stack.copy());
                show(Text.translatable("koi-orbit.hud.durability", stack.getName(), left), 0xFF5555, 60);
                mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0.6f));
            }
        }
    }

    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null || mc.interactionManager == null) return;
        boolean debug = mc.getDebugHud().shouldShowDebugHud();
        if (config.coordsEnabled && !debug) renderCoords(ctx, mc);
        if (config.minimapEnabled && !debug) minimap.render(ctx, mc);
        renderMessage(ctx, mc, tickCounter);
    }

    // GUI 배율보다 한 단계 작게 (정수 픽셀이라 글자가 안 깨짐, 배율 2 이하는 그대로)
    static float smallScale(MinecraftClient mc) {
        int guiScale = (int) mc.getWindow().getScaleFactor();
        return guiScale > 2 ? (guiScale - 1f) / guiScale : 1f;
    }

    private void renderCoords(DrawContext ctx, MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        BlockPos pos = player.getBlockPos();
        Direction facing = player.getHorizontalFacing();
        List<Text> lines = new ArrayList<>();
        lines.add(Text.translatable("koi-orbit.hud.xyz", pos.getX(), pos.getY(), pos.getZ()));
        lines.add(Text.translatable("koi-orbit.hud.facing", Text.translatable("koi-orbit.direction." + facing.asString()),
                Text.literal((facing.getDirection() == Direction.AxisDirection.POSITIVE ? "+" : "-") + facing.getAxis().asString().toUpperCase())));
        if (config.coordsBiome) {
            lines.add(mc.world.getBiome(pos).getKey()
                    .map(key -> Text.translatable("koi-orbit.hud.biome", Text.translatable(Util.createTranslationKey("biome", key.getValue()))))
                    .orElse(Text.empty()));
        }

        TextRenderer tr = mc.textRenderer;
        float scale = smallScale(mc);
        MatrixStack matrices = ctx.getMatrices();
        matrices.push();
        matrices.translate(4, 4, 0);
        matrices.scale(scale, scale, 1);
        int y = 0;
        for (Text line : lines) {
            ctx.fill(-2, y - 1, tr.getWidth(line) + 2, y + 9, 0x60000000);
            ctx.drawTextWithShadow(tr, line, 0, y, 0xFFFFFFFF);
            y += 10;
        }
        matrices.pop();
    }

    // 알림: 핫바 바로 위에 작게, 사라질 때 서서히 흐려짐
    private void renderMessage(DrawContext ctx, MinecraftClient mc, RenderTickCounter tickCounter) {
        if (messageTicks <= 0 || message == null) return;
        float fade = Math.min(1f, (messageTicks - tickCounter.getTickDelta(false)) / 10f);
        int alpha = (int) (fade * 255);
        if (alpha < 8) return; // 너무 투명하면 불투명하게 그려지는 버그 방지

        // 크리에이티브는 핫바 바로 위, 서바이벌은 체력/방어구와 바닐라 아이템 이름(h-59) 위
        int offset = mc.interactionManager.hasStatusBars() ? 70 : 32;
        float scale = smallScale(mc);
        TextRenderer tr = mc.textRenderer;
        MatrixStack matrices = ctx.getMatrices();
        matrices.push();
        matrices.translate(ctx.getScaledWindowWidth() / 2f, ctx.getScaledWindowHeight() - offset, 0);
        matrices.scale(scale, scale, 1);
        ctx.drawTextWithShadow(tr, message, -tr.getWidth(message) / 2, 0, messageColor | alpha << 24);
        matrices.pop();
    }
}
