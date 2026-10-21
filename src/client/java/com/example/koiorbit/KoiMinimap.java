package com.example.koiorbit;

import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

// 화면 오른쪽 위 미니맵. 지도 아이템처럼 지형 표면만 그림 (북쪽이 위)
public class KoiMinimap {
    private static final int RES = 128; // 텍스처 해상도 (픽셀)
    private static final int UPDATE_TICKS = 10;
    private static final Identifier TEXTURE = Identifier.of("koi-orbit", "minimap");
    private static final Identifier ARROW = Identifier.of("koi-orbit", "textures/minimap_arrow.png");
    private static final int UNLOADED = 0xFF181818;

    private final KoiConfig config;
    private NativeImageBackedTexture texture;
    private int ticks = 0;
    private double centerX, centerZ; // 마지막으로 그린 중심 (블록 좌표)
    private int drawnScale = 1;

    public KoiMinimap(KoiConfig config) {
        this.config = config;
    }

    public void tick(MinecraftClient mc) {
        if (!config.minimapEnabled || mc.player == null || mc.world == null) return;
        if (texture == null) {
            texture = new NativeImageBackedTexture(new NativeImage(RES, RES, true));
            mc.getTextureManager().registerTexture(TEXTURE, texture);
            ticks = 0;
        }
        if (ticks-- > 0) return;
        ticks = UPDATE_TICKS;
        redraw(mc.world, mc.player);
    }

    private void redraw(ClientWorld world, ClientPlayerEntity player) {
        NativeImage image = texture.getImage();
        int scale = (int) config.minimapScale;
        int startX = player.getBlockX() - RES / 2 * scale;
        int startZ = player.getBlockZ() - RES / 2 * scale;
        boolean ceiling = world.getDimension().hasCeiling();
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int px = 0; px < RES; px++) {
            int x = startX + px * scale;
            int northHeight = Integer.MIN_VALUE;
            WorldChunk chunk = null;
            for (int pz = 0; pz < RES; pz++) {
                int z = startZ + pz * scale;
                if (chunk == null || chunk.getPos().x != x >> 4 || chunk.getPos().z != z >> 4) {
                    chunk = world.getChunkManager().getWorldChunk(x >> 4, z >> 4, false);
                }
                if (chunk == null) {
                    image.setColor(px, pz, UNLOADED);
                    northHeight = Integer.MIN_VALUE;
                    continue;
                }
                int y = ceiling ? surfaceBelow(chunk, pos, x, player.getBlockY(), z, world.getBottomY())
                        : chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x & 15, z & 15);
                image.setColor(px, pz, colorAt(world, chunk, pos, x, y, z, northHeight));
                northHeight = y;
            }
        }
        texture.upload();
        centerX = startX + RES / 2.0 * scale;
        centerZ = startZ + RES / 2.0 * scale;
        drawnScale = scale;
    }

    // 네더처럼 천장이 있는 곳은 내 위치에서 아래로 내려가며 처음 만나는 바닥
    private static int surfaceBelow(WorldChunk chunk, BlockPos.Mutable pos, int x, int startY, int z, int bottom) {
        int y = startY + 1;
        while (y > bottom && chunk.getBlockState(pos.set(x, y, z)).isAir()) y--;
        return y;
    }

    private static int colorAt(ClientWorld world, WorldChunk chunk, BlockPos.Mutable pos, int x, int y, int z, int northHeight) {
        // 유리, 공기처럼 색이 없는 블록은 몇 칸 아래까지 내려가 봄
        BlockState state = chunk.getBlockState(pos.set(x, y, z));
        for (int i = 0; i < 4 && state.getMapColor(world, pos) == MapColor.CLEAR && y > world.getBottomY(); i++) {
            state = chunk.getBlockState(pos.set(x, --y, z));
        }
        MapColor color = state.getMapColor(world, pos);
        if (color == MapColor.CLEAR) return UNLOADED;

        MapColor.Brightness brightness;
        FluidState fluid = state.getFluidState();
        if (fluid.isIn(FluidTags.WATER)) {
            // 물은 깊을수록 어둡게
            int depth = 0;
            while (depth < 10 && chunk.getBlockState(pos.set(x, y - depth - 1, z)).getFluidState().isIn(FluidTags.WATER)) depth++;
            brightness = depth < 2 ? MapColor.Brightness.HIGH : depth < 5 ? MapColor.Brightness.NORMAL : MapColor.Brightness.LOW;
        } else if (northHeight == Integer.MIN_VALUE || y == northHeight) {
            brightness = MapColor.Brightness.NORMAL;
        } else {
            // 북쪽보다 높으면 밝게, 낮으면 어둡게 (지도처럼 입체감)
            brightness = y > northHeight ? MapColor.Brightness.HIGH : MapColor.Brightness.LOW;
        }
        return color.getRenderColor(brightness);
    }

    public void render(DrawContext ctx, MinecraftClient mc) {
        if (texture == null) return;
        ClientPlayerEntity player = mc.player;
        int size = (int) config.minimapSize;
        int x = ctx.getScaledWindowWidth() - size - 4;
        int y = 4 + effectRowsHeight(player);

        ctx.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF000000);
        // 마지막으로 그린 뒤 움직인 만큼 지도를 밀어서 부드럽게
        float perBlock = size / (float) (RES * drawnScale);
        float dx = (float) (player.getX() - centerX) * perBlock;
        float dz = (float) (player.getZ() - centerZ) * perBlock;
        MatrixStack matrices = ctx.getMatrices();
        ctx.enableScissor(x, y, x + size, y + size);
        matrices.push();
        matrices.translate(x - dx, y - dz, 0);
        ctx.drawTexture(TEXTURE, 0, 0, size, size, 0, 0, RES, RES, RES, RES);
        matrices.pop();
        ctx.disableScissor();

        // 내 위치와 바라보는 방향
        matrices.push();
        matrices.translate(x + size / 2f, y + size / 2f, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(player.getYaw() - 180));
        ctx.drawTexture(ARROW, -4, -4, 8, 8, 0, 0, 16, 16, 16, 16);
        matrices.pop();

        matrices.push();
        float scale = KoiHud.smallScale(mc);
        matrices.translate(x + size / 2f, y + 2, 0);
        matrices.scale(scale, scale, 1);
        Text north = Text.translatable("koi-orbit.hud.north");
        ctx.drawTextWithShadow(mc.textRenderer, north, -mc.textRenderer.getWidth(north) / 2, 0, 0xFFFFFFFF);
        matrices.pop();
    }

    // 오른쪽 위 상태 효과 아이콘과 겹치지 않게 그 아래로
    private static int effectRowsHeight(ClientPlayerEntity player) {
        boolean good = false, bad = false;
        for (StatusEffectInstance effect : player.getStatusEffects()) {
            if (!effect.shouldShowIcon()) continue;
            if (effect.getEffectType().value().isBeneficial()) good = true;
            else bad = true;
        }
        return (good ? 26 : 0) + (bad ? 26 : 0);
    }
}
