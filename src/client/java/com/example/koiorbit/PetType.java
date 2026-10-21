package com.example.koiorbit;

import com.mojang.serialization.Codec;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

// 따라다니는 펫으로 쓸 수 있는 몹. scale은 몹마다 크기가 비슷해 보이도록 맞춘 값
public enum PetType implements StringIdentifiable {
    ALLAY("allay", EntityType.ALLAY, 1f),
    BEE("bee", EntityType.BEE, 1f),
    PARROT("parrot", EntityType.PARROT, 1f),
    BAT("bat", EntityType.BAT, 0.9f),
    VEX("vex", EntityType.VEX, 1f),
    CAT("cat", EntityType.CAT, 0.7f),
    FOX("fox", EntityType.FOX, 0.6f),
    WOLF("wolf", EntityType.WOLF, 0.55f),
    RABBIT("rabbit", EntityType.RABBIT, 0.9f),
    CHICKEN("chicken", EntityType.CHICKEN, 0.8f),
    FROG("frog", EntityType.FROG, 0.9f),
    AXOLOTL("axolotl", EntityType.AXOLOTL, 0.8f);

    public static final Codec<PetType> CODEC = StringIdentifiable.createCodec(PetType::values);

    private final String name;
    public final EntityType<? extends LivingEntity> entityType;
    public final float scale;

    PetType(String name, EntityType<? extends LivingEntity> entityType, float scale) {
        this.name = name;
        this.entityType = entityType;
        this.scale = scale;
    }

    @Override
    public String asString() {
        return name;
    }

    public Text getText() {
        return entityType.getName();
    }
}
