package com.example.koiorbit;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

// 오라가 피어오르는 위치
public enum AuraPosition implements StringIdentifiable {
    SHOULDERS("shoulders"), // 양쪽 어깨
    BODY("body"),           // 몸 둘레
    FEET("feet");           // 발밑

    public static final Codec<AuraPosition> CODEC = StringIdentifiable.createCodec(AuraPosition::values);

    private final String name;

    AuraPosition(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }

    public Text getText() {
        return Text.translatable("aura.koi-orbit.position." + name);
    }

    public static AuraPosition byName(String name) {
        for (AuraPosition position : values()) {
            if (position.name.equals(name)) return position;
        }
        return null;
    }
}
