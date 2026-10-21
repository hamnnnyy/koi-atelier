package com.example.koiorbit;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

// 도트 그림을 한 픽셀씩 1픽셀 두께의 작은 블록으로 쌓아 그림 (손에 든 아이템처럼 입체로 보임)
// 문자 하나 = 픽셀 하나, '.'은 빈칸. 맞닿은 면은 안 그림
public final class KoiSprite {
    private static final Identifier WHITE = Identifier.of("koi-orbit", "textures/white.png");
    public static final RenderLayer LAYER = RenderLayer.getEntityCutoutNoCull(WHITE);

    // W L M D: 색을 입히는 밝기 단계 (밝음 -> 어두움), K: 검은 테두리, S: 흰색 고정
    private static final String TINTED = "WLMD";

    public final int width, height;
    private final String[] rows;

    public KoiSprite(String... rows) {
        this.rows = rows;
        this.height = rows.length;
        int w = 0;
        for (String row : rows) w = Math.max(w, row.length());
        this.width = w;
    }

    private char at(int x, int y) {
        if (y < 0 || y >= height || x < 0 || x >= rows[y].length()) return '.';
        return rows[y].charAt(x);
    }

    private boolean filled(int x, int y) {
        return at(x, y) != '.';
    }

    private static int baseColor(char c) {
        return switch (c) {
            case 'W' -> 0xFFFFFF;
            case 'L' -> 0xD7D7DE;
            case 'M' -> 0xAAAAB6;
            case 'D' -> 0x787887;
            case 'K' -> 0x231E28;
            default -> 0xFFFFFF;
        };
    }

    private static int color(char c, int tint) {
        int base = baseColor(c);
        if (TINTED.indexOf(c) < 0) return 0xFF000000 | base;
        int r = (base >> 16 & 255) * (tint >> 16 & 255) / 255;
        int g = (base >> 8 & 255) * (tint >> 8 & 255) / 255;
        int b = (base & 255) * (tint & 255) / 255;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /**
     * 그림의 왼쪽 위가 원점, 오른쪽이 +x, 아래가 +y (모델 좌표계와 같음), 두께는 +z 방향.
     * mirror면 -x 쪽으로 뒤집어서 그림 (반대쪽 날개).
     */
    public void render(MatrixStack.Entry entry, VertexConsumer vc, float px, boolean mirror, int tint, int light) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!filled(x, y)) continue;
                int c = color(at(x, y), tint);
                float lx = mirror ? -(x + 1) * px : x * px, rx = lx + px;
                float y0 = y * px, y1 = y0 + px, z0 = 0, z1 = px;

                quad(vc, entry, c, light, 0, 0, -1, lx, y0, z0, lx, y1, z0, rx, y1, z0, rx, y0, z0); // 앞
                quad(vc, entry, c, light, 0, 0, 1, rx, y0, z1, rx, y1, z1, lx, y1, z1, lx, y0, z1);  // 뒤
                // 그림에서 왼쪽/오른쪽 이웃이 비었으면 옆면 (뒤집으면 실제 방향이 반대)
                float leftX = mirror ? rx : lx, rightX = mirror ? lx : rx;
                float leftN = mirror ? 1 : -1;
                if (!filled(x - 1, y)) quad(vc, entry, c, light, leftN, 0, 0, leftX, y0, z1, leftX, y1, z1, leftX, y1, z0, leftX, y0, z0);
                if (!filled(x + 1, y)) quad(vc, entry, c, light, -leftN, 0, 0, rightX, y0, z0, rightX, y1, z0, rightX, y1, z1, rightX, y0, z1);
                if (!filled(x, y - 1)) quad(vc, entry, c, light, 0, -1, 0, lx, y0, z1, lx, y0, z0, rx, y0, z0, rx, y0, z1);
                if (!filled(x, y + 1)) quad(vc, entry, c, light, 0, 1, 0, lx, y1, z0, lx, y1, z1, rx, y1, z1, rx, y1, z0);
            }
        }
    }

    private static void quad(VertexConsumer vc, MatrixStack.Entry e, int color, int light, float nx, float ny, float nz,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4) {
        vertex(vc, e, color, light, nx, ny, nz, x1, y1, z1, 0, 0);
        vertex(vc, e, color, light, nx, ny, nz, x2, y2, z2, 0, 1);
        vertex(vc, e, color, light, nx, ny, nz, x3, y3, z3, 1, 1);
        vertex(vc, e, color, light, nx, ny, nz, x4, y4, z4, 1, 0);
    }

    private static void vertex(VertexConsumer vc, MatrixStack.Entry e, int color, int light, float nx, float ny, float nz,
                               float x, float y, float z, float u, float v) {
        vc.vertex(e, x, y, z).color(color).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, nx, ny, nz);
    }
}
