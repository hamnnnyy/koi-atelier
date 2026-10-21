package io.github.hamnnnyy.koiatelier;

import com.mojang.serialization.Codec;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import org.joml.Vector3f;

// 오라에 쓸 파티클 종류
public enum AuraType implements StringIdentifiable {
    FLAME("flame"),
    SOUL_FLAME("soul_flame"),
    SMALL_FLAME("small_flame"),
    END_ROD("end_rod"),
    SPARK("spark"),
    TOTEM("totem"),
    CHERRY("cherry"),
    HEART("heart"),
    DUST("dust"); // 색을 고를 수 있음

    public static final Codec<AuraType> CODEC = StringIdentifiable.createCodec(AuraType::values);

    private final String name;

    AuraType(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }

    public Text getText() {
        return Text.translatable("aura.koi-atelier.type." + name);
    }

    public boolean usesColor() {
        return this == DUST;
    }

    public ParticleEffect particle(DyeColor color) {
        return switch (this) {
            case FLAME -> ParticleTypes.FLAME;
            case SOUL_FLAME -> ParticleTypes.SOUL_FIRE_FLAME;
            case SMALL_FLAME -> ParticleTypes.SMALL_FLAME;
            case END_ROD -> ParticleTypes.END_ROD;
            case SPARK -> ParticleTypes.ELECTRIC_SPARK;
            case TOTEM -> ParticleTypes.TOTEM_OF_UNDYING;
            case CHERRY -> ParticleTypes.CHERRY_LEAVES;
            case HEART -> ParticleTypes.HEART;
            case DUST -> {
                int rgb = color.getEntityColor();
                yield new DustParticleEffect(new Vector3f((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f), 1f);
            }
        };
    }

    public static AuraType byName(String name) {
        for (AuraType type : values()) {
            if (type.name.equals(name)) return type;
        }
        return null;
    }
}
