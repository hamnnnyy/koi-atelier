package io.github.hamnnnyy.koiatelier;

import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

// 물고기 한 마리의 생김새: 무늬 + 바탕색 + 무늬색
public record FishLook(TropicalFishEntity.Variety pattern, DyeColor base, DyeColor patternColor) {
    public static final FishLook GOLD = new FishLook(TropicalFishEntity.Variety.SPOTTY, DyeColor.YELLOW, DyeColor.ORANGE);
    public static final FishLook BLACK = new FishLook(TropicalFishEntity.Variety.SPOTTY, DyeColor.BLACK, DyeColor.YELLOW);

    public boolean isValid() {
        return pattern != null && base != null && patternColor != null;
    }

    // 예: 점박이 (노란색 / 주황색)
    public Text describe() {
        return Text.translatable("koi-atelier.command.look", pattern.getText(),
                KoiOptions.colorText(base), KoiOptions.colorText(patternColor));
    }
}
