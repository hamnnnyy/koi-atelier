package com.example.koiorbit;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

// 모든 설정 항목의 정의. 설정 화면과 /koi 명령어가 이 목록에서 자동으로 만들어짐
public final class KoiOptions {
    public enum Page {
        FISH, AURA, STYLE, HUD;

        public Text getText() {
            return Text.translatable("koi-orbit.page." + name().toLowerCase());
        }

        // 꾸미기 페이지들은 열려 있는 동안 3인칭 미리보기
        public boolean previewsPlayer() {
            return this != HUD;
        }
    }

    private static final KoiConfig DEFAULTS = new KoiConfig();
    private static final DynamicCommandExceptionType UNKNOWN_VALUE =
            new DynamicCommandExceptionType(value -> Text.translatable("koi-orbit.command.unknown_value", value));

    public static final List<Opt<?>> ALL = new ArrayList<>();

    static {
        Page p = Page.FISH;
        bool(p, "enabled", c -> c.enabled, (c, v) -> c.enabled = v);
        bool(p, "firstperson", c -> c.firstPerson, (c, v) -> c.firstPerson = v);
        num(p, "size", 0.1, 5, 0.1, c -> c.size, (c, v) -> c.size = v);
        num(p, "radius", 0, 10, 0.1, c -> c.radius, (c, v) -> c.radius = v);
        num(p, "height", -3, 5, 0.1, c -> c.height, (c, v) -> c.height = v);
        num(p, "speed", -10, 10, 0.1, c -> c.speed, (c, v) -> c.speed = v);
        num(p, "bob", 0, 2, 0.05, c -> c.bob, (c, v) -> c.bob = v);
        // 화면에서 왼쪽 열 1번, 오른쪽 열 2번 물고기가 되도록 번갈아 추가
        for (String part : new String[]{"pattern", "base", "pattern_color"}) {
            for (int i = 0; i < 2; i++) fishOption(i, part);
        }

        p = Page.AURA;
        bool(p, "aura", c -> c.auraEnabled, (c, v) -> c.auraEnabled = v);
        choice(p, "aura_type", AuraType.values(), AuraType.CODEC, AuraType::getText, c -> c.auraType, (c, v) -> c.auraType = v);
        choice(p, "aura_position", AuraPosition.values(), AuraPosition.CODEC, AuraPosition::getText, c -> c.auraPosition, (c, v) -> c.auraPosition = v);
        color(p, "aura_color", c -> c.auraColor, (c, v) -> c.auraColor = v);
        num(p, "aura_density", 0.1, 5, 0.1, c -> c.auraDensity, (c, v) -> c.auraDensity = v);
        bool(p, "trail", c -> c.trailEnabled, (c, v) -> c.trailEnabled = v).newRow();
        choice(p, "trail_type", AuraType.values(), AuraType.CODEC, AuraType::getText, c -> c.trailType, (c, v) -> c.trailType = v);
        color(p, "trail_color", c -> c.trailColor, (c, v) -> c.trailColor = v);
        num(p, "trail_density", 0.1, 5, 0.1, c -> c.trailDensity, (c, v) -> c.trailDensity = v);

        p = Page.STYLE;
        bool(p, "pet", c -> c.petEnabled, (c, v) -> c.petEnabled = v);
        choice(p, "pet_type", PetType.values(), PetType.CODEC, PetType::getText, c -> c.petType, (c, v) -> c.petType = v);
        num(p, "pet_size", 0.2, 3, 0.1, c -> c.petSize, (c, v) -> c.petSize = v);
        bool(p, "halo", c -> c.haloEnabled, (c, v) -> c.haloEnabled = v).newRow();
        color(p, "halo_color", c -> c.haloColor, (c, v) -> c.haloColor = v);
        num(p, "halo_size", 0.3, 3, 0.1, c -> c.haloSize, (c, v) -> c.haloSize = v);
        bool(p, "head_text", c -> c.headTextEnabled, (c, v) -> c.headTextEnabled = v).withTooltip().newRow();
        color(p, "head_text_color", c -> c.headTextColor, (c, v) -> c.headTextColor = v);
        bool(p, "cape", c -> c.capeEnabled, (c, v) -> c.capeEnabled = v).withTooltip().newRow();
        choice(p, "cape_design", CapeDesign.values(), CapeDesign.CODEC, CapeDesign::getText, c -> c.capeDesign, (c, v) -> c.capeDesign = v);
        bool(p, "wings", c -> c.wingsEnabled, (c, v) -> c.wingsEnabled = v).newRow();
        choice(p, "wings_type", WingType.values(), WingType.CODEC, WingType::getText, c -> c.wingsType, (c, v) -> c.wingsType = v);
        color(p, "wings_color", c -> c.wingsColor, (c, v) -> c.wingsColor = v);

        p = Page.HUD;
        bool(p, "coords", c -> c.coordsEnabled, (c, v) -> c.coordsEnabled = v);
        bool(p, "coords_biome", c -> c.coordsBiome, (c, v) -> c.coordsBiome = v);
        bool(p, "minimap", c -> c.minimapEnabled, (c, v) -> c.minimapEnabled = v).newRow();
        num(p, "minimap_size", 40, 160, 4, c -> c.minimapSize, (c, v) -> c.minimapSize = v);
        num(p, "minimap_scale", 1, 4, 1, c -> c.minimapScale, (c, v) -> c.minimapScale = v);
        bool(p, "durability", c -> c.durabilityEnabled, (c, v) -> c.durabilityEnabled = v).newRow();
        num(p, "durability_percent", 1, 50, 1, c -> c.durabilityPercent, (c, v) -> c.durabilityPercent = v);
        num(p, "zoom", 1.5, 10, 0.5, c -> c.zoomAmount, (c, v) -> c.zoomAmount = v).withTooltip().newRow();
        bool(p, "fullbright", c -> c.fullbright, (c, v) -> c.fullbright = v).withTooltip();
    }

    private KoiOptions() {
    }

    public static List<Opt<?>> on(Page page) {
        return ALL.stream().filter(o -> o.page == page).toList();
    }

    public static void validate(KoiConfig config) {
        for (Opt<?> opt : ALL) opt.validate(config);
    }

    public static void reset(KoiConfig config, Page page) {
        for (Opt<?> opt : on(page)) opt.reset(config);
    }

    // 1.5000 -> "1.5"
    public static String format(double value) {
        double rounded = Math.round(value * 100) / 100.0;
        return rounded == Math.rint(rounded) ? String.valueOf((long) rounded) : String.valueOf(rounded);
    }

    public static Text colorText(DyeColor color) {
        return Text.translatable("color.minecraft." + color.asString());
    }

    private static BoolOpt bool(Page page, String id, Function<KoiConfig, Boolean> getter, BiConsumer<KoiConfig, Boolean> setter) {
        return add(new BoolOpt(page, id, getter, setter));
    }

    private static NumOpt num(Page page, String id, double min, double max, double step,
                              Function<KoiConfig, Double> getter, BiConsumer<KoiConfig, Double> setter) {
        return add(new NumOpt(page, id, min, max, step, getter, setter));
    }

    private static <T extends StringIdentifiable> ChoiceOpt<T> choice(Page page, String id, T[] values, Codec<T> codec, Function<T, Text> label,
                                                                     Function<KoiConfig, T> getter, BiConsumer<KoiConfig, T> setter) {
        return add(new ChoiceOpt<>(page, id, List.of(values), codec, label, getter, setter));
    }

    private static ChoiceOpt<DyeColor> color(Page page, String id, Function<KoiConfig, DyeColor> getter, BiConsumer<KoiConfig, DyeColor> setter) {
        return choice(page, id, DyeColor.values(), DyeColor.CODEC, KoiOptions::colorText, getter, setter);
    }

    private static void fishOption(int i, String part) {
        String id = "fish" + (i + 1) + "_" + part;
        switch (part) {
            case "pattern" -> {
                ChoiceOpt<TropicalFishEntity.Variety> opt = choice(Page.FISH, id, TropicalFishEntity.Variety.values(),
                        TropicalFishEntity.Variety.CODEC, TropicalFishEntity.Variety::getText,
                        c -> c.fish[i].pattern(), (c, v) -> c.fish[i] = new FishLook(v, c.fish[i].base(), c.fish[i].patternColor()));
                if (i == 0) opt.newRow(); // 물고기 무늬/색은 1번, 2번이 한 줄에 나란히
            }
            case "base" -> color(Page.FISH, id, c -> c.fish[i].base(),
                    (c, v) -> c.fish[i] = new FishLook(c.fish[i].pattern(), v, c.fish[i].patternColor()));
            default -> color(Page.FISH, id, c -> c.fish[i].patternColor(),
                    (c, v) -> c.fish[i] = new FishLook(c.fish[i].pattern(), c.fish[i].base(), v));
        }
    }

    private static <O extends Opt<?>> O add(O opt) {
        ALL.add(opt);
        return opt;
    }

    // 설정 항목 하나
    public abstract static class Opt<T> {
        public final Page page;
        public final String id;
        private final Function<KoiConfig, T> getter;
        private final BiConsumer<KoiConfig, T> setter;
        private boolean tooltip;
        private boolean newRow;

        Opt(Page page, String id, Function<KoiConfig, T> getter, BiConsumer<KoiConfig, T> setter) {
            this.page = page;
            this.id = id;
            this.getter = getter;
            this.setter = setter;
        }

        // koi-orbit.option.<id>.tooltip 설명을 화면에 표시
        @SuppressWarnings("unchecked")
        <O extends Opt<T>> O withTooltip() {
            tooltip = true;
            return (O) this;
        }

        // 설정 화면에서 이 항목부터 새 줄에 놓기 (기능 묶음이 줄을 맞추도록)
        @SuppressWarnings("unchecked")
        <O extends Opt<T>> O newRow() {
            newRow = true;
            return (O) this;
        }

        public boolean startsRow() {
            return newRow;
        }

        public T get(KoiConfig config) {
            return getter.apply(config);
        }

        public void set(KoiConfig config, T value) {
            setter.accept(config, sanitize(value));
        }

        T sanitize(T value) {
            return value;
        }

        void validate(KoiConfig config) {
            T value = get(config);
            set(config, value == null ? get(DEFAULTS) : value);
        }

        void reset(KoiConfig config) {
            set(config, get(DEFAULTS));
        }

        public String key() {
            return "koi-orbit.option." + id;
        }

        public Text label() {
            return Text.translatable(key());
        }

        public abstract Text valueText(T value);

        public Text valueText(KoiConfig config) {
            return valueText(get(config));
        }

        SimpleOption.TooltipFactory<T> tooltipFactory() {
            return tooltip ? SimpleOption.constantTooltip(Text.translatable(key() + ".tooltip")) : SimpleOption.emptyTooltip();
        }

        public abstract SimpleOption<T> widget(KoiConfig config);

        public abstract ArgumentType<?> argumentType();

        public abstract T parse(CommandContext<FabricClientCommandSource> ctx, String arg) throws CommandSyntaxException;

        public SuggestionProvider<FabricClientCommandSource> suggestions() {
            return null;
        }
    }

    public static final class BoolOpt extends Opt<Boolean> {
        BoolOpt(Page page, String id, Function<KoiConfig, Boolean> getter, BiConsumer<KoiConfig, Boolean> setter) {
            super(page, id, getter, setter);
        }

        @Override
        public Text valueText(Boolean value) {
            return Text.translatable(value ? "options.on" : "options.off");
        }

        @Override
        public SimpleOption<Boolean> widget(KoiConfig config) {
            return SimpleOption.ofBoolean(key(), tooltipFactory(), get(config), v -> set(config, v));
        }

        @Override
        public ArgumentType<?> argumentType() {
            return BoolArgumentType.bool();
        }

        @Override
        public Boolean parse(CommandContext<FabricClientCommandSource> ctx, String arg) {
            return BoolArgumentType.getBool(ctx, arg);
        }
    }

    public static final class NumOpt extends Opt<Double> {
        final double min, max, step;

        NumOpt(Page page, String id, double min, double max, double step, Function<KoiConfig, Double> getter, BiConsumer<KoiConfig, Double> setter) {
            super(page, id, getter, setter);
            this.min = min;
            this.max = max;
            this.step = step;
        }

        @Override
        Double sanitize(Double value) {
            return MathHelper.clamp(value, min, max);
        }

        // step 단위로 맞추고, 1.2000000000000002 같은 부동소수점 오차는 잘라냄
        private double snap(double value) {
            double snapped = Math.round(value / step) * step;
            return Math.round(snapped * 1e6) / 1e6;
        }

        @Override
        public Text valueText(Double value) {
            return Text.literal(format(value));
        }

        @Override
        public SimpleOption<Double> widget(KoiConfig config) {
            return new SimpleOption<>(key(), tooltipFactory(),
                    (optionText, v) -> GameOptions.getGenericValueText(optionText, valueText(v)),
                    SimpleOption.DoubleSliderCallbacks.INSTANCE.withModifier(
                            progress -> snap(min + progress * (max - min)),
                            v -> (v - min) / (max - min)),
                    get(config), v -> set(config, v));
        }

        @Override
        public ArgumentType<?> argumentType() {
            return DoubleArgumentType.doubleArg(min, max);
        }

        @Override
        public Double parse(CommandContext<FabricClientCommandSource> ctx, String arg) {
            return DoubleArgumentType.getDouble(ctx, arg);
        }
    }

    public static final class ChoiceOpt<T extends StringIdentifiable> extends Opt<T> {
        private final List<T> values;
        private final Codec<T> codec;
        private final Function<T, Text> label;

        ChoiceOpt(Page page, String id, List<T> values, Codec<T> codec, Function<T, Text> label,
                  Function<KoiConfig, T> getter, BiConsumer<KoiConfig, T> setter) {
            super(page, id, getter, setter);
            this.values = values;
            this.codec = codec;
            this.label = label;
        }

        @Override
        public Text valueText(T value) {
            return label.apply(value);
        }

        @Override
        public SimpleOption<T> widget(KoiConfig config) {
            return new SimpleOption<>(key(), tooltipFactory(),
                    (optionText, v) -> label.apply(v), // 버튼이 "이름: " 을 알아서 붙임
                    new SimpleOption.PotentialValuesBasedCallbacks<>(values, codec), get(config), v -> set(config, v));
        }

        @Override
        public ArgumentType<?> argumentType() {
            return StringArgumentType.word();
        }

        @Override
        public T parse(CommandContext<FabricClientCommandSource> ctx, String arg) throws CommandSyntaxException {
            String name = StringArgumentType.getString(ctx, arg);
            for (T value : values) {
                if (value.asString().equals(name)) return value;
            }
            throw UNKNOWN_VALUE.create(name);
        }

        @Override
        public SuggestionProvider<FabricClientCommandSource> suggestions() {
            return (ctx, builder) -> CommandSource.suggestMatching(values.stream().map(StringIdentifiable::asString), builder);
        }
    }
}
