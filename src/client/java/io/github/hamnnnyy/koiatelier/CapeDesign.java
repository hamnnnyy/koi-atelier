package io.github.hamnnnyy.koiatelier;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

// 망토 디자인 (assets/koi-atelier/textures/cape/<name>.png, 엘리트라에도 같은 무늬가 쓰임)
public enum CapeDesign implements StringIdentifiable {
    KOI("koi"),
    FLAME("flame"),
    SAKURA("sakura"),
    GALAXY("galaxy"),
    OCEAN("ocean");

    public static final Codec<CapeDesign> CODEC = StringIdentifiable.createCodec(CapeDesign::values);

    private final String name;
    public final Identifier texture;

    CapeDesign(String name) {
        this.name = name;
        this.texture = Identifier.of("koi-atelier", "textures/cape/" + name + ".png");
    }

    @Override
    public String asString() {
        return name;
    }

    public Text getText() {
        return Text.translatable("koi-atelier.cape." + name);
    }
}
