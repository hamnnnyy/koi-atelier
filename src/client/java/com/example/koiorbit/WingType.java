package com.example.koiorbit;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

// 날개 모양 (KoiSprites의 도트 그림)
public enum WingType implements StringIdentifiable {
    ANGEL("angel", KoiSprites.ANGEL, 18f),     // 천천히 크게
    BUTTERFLY("butterfly", KoiSprites.BUTTERFLY, 30f),
    DRAGON("dragon", KoiSprites.DRAGON, 14f);

    public static final Codec<WingType> CODEC = StringIdentifiable.createCodec(WingType::values);

    private final String name;
    public final KoiSprite sprite;
    public final float flapAngle; // 퍼덕이는 각도

    WingType(String name, KoiSprite sprite, float flapAngle) {
        this.name = name;
        this.sprite = sprite;
        this.flapAngle = flapAngle;
    }

    @Override
    public String asString() {
        return name;
    }

    public Text getText() {
        return Text.translatable("koi-orbit.wings." + name);
    }
}
