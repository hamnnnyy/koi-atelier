package com.example.koiorbit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

// /koi                                      전체 설정 보기
// /koi menu                                 설정 화면 열기
// /koi <설정 이름> [값]                       설정 하나 보기/바꾸기 (설정 이름은 KoiOptions 참고)
// /koi <1|2|all> <무늬> <바탕색> <무늬색>      물고기 모양 한 번에 바꾸기
// /koi head_text <문구|clear>                머리 위 문구
// /koi on|off                               물고기 켜기/끄기
// /koi reset                                전부 기본값으로
public class KoiCommand {
    private final KoiConfig config;
    private final Runnable openMenu;

    public KoiCommand(KoiConfig config, Runnable openMenu) {
        this.config = config;
        this.openMenu = openMenu;
    }

    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> root = literal("koi")
                .executes(ctx -> show(ctx.getSource()))
                .then(literal("menu").executes(ctx -> {
                    openMenu.run();
                    return 1;
                }))
                .then(literal("reset").executes(ctx -> {
                    config.reset();
                    config.save();
                    ctx.getSource().sendFeedback(Text.translatable("koi-orbit.command.reset"));
                    return 1;
                }))
                .then(literal("on").executes(ctx -> setFishEnabled(ctx.getSource(), true)))
                .then(literal("off").executes(ctx -> setFishEnabled(ctx.getSource(), false)))
                .then(fishSetter("1", 0, 0))
                .then(fishSetter("2", 1, 1))
                .then(fishSetter("all", 0, 1))
                .then(literal("head_text")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(Text.translatable("koi-orbit.command.option",
                                    Text.translatable("koi-orbit.option.head_text"), Text.literal(config.headText)));
                            return 1;
                        })
                        .then(literal("clear").executes(ctx -> setHeadText(ctx.getSource(), "")))
                        .then(argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> setHeadText(ctx.getSource(), StringArgumentType.getString(ctx, "text")))));
        for (KoiOptions.Opt<?> opt : KoiOptions.ALL) root.then(optionNode(opt));
        dispatcher.register(root);
    }

    // 값 없이 쓰면 현재 값 보기, 값을 주면 바꾸기
    private <T> LiteralArgumentBuilder<FabricClientCommandSource> optionNode(KoiOptions.Opt<T> opt) {
        RequiredArgumentBuilder<FabricClientCommandSource, ?> value = argument("value", opt.argumentType());
        if (opt.suggestions() != null) value.suggests(opt.suggestions());
        value.executes(ctx -> {
            opt.set(config, opt.parse(ctx, "value"));
            config.save();
            return feedback(ctx.getSource(), opt);
        });
        return literal(opt.id).executes(ctx -> feedback(ctx.getSource(), opt)).then(value);
    }

    private int feedback(FabricClientCommandSource source, KoiOptions.Opt<?> opt) {
        source.sendFeedback(Text.translatable("koi-orbit.command.option", opt.label(), opt.valueText(config)));
        return 1;
    }

    private int setFishEnabled(FabricClientCommandSource source, boolean value) {
        config.enabled = value;
        config.save();
        source.sendFeedback(Text.translatable(value ? "koi-orbit.message.fish_on" : "koi-orbit.message.fish_off"));
        return 1;
    }

    private int setHeadText(FabricClientCommandSource source, String text) {
        config.headText = text;
        config.headTextEnabled = !text.isBlank();
        config.save();
        source.sendFeedback(Text.translatable("koi-orbit.command.option", Text.translatable("koi-orbit.option.head_text"), Text.literal(text)));
        return 1;
    }

    // /koi 1 betty red white: 무늬, 바탕색, 무늬색 옵션을 그대로 재사용
    private LiteralArgumentBuilder<FabricClientCommandSource> fishSetter(String name, int from, int to) {
        KoiOptions.Opt<?> pattern = option("fish1_pattern"), base = option("fish1_base");
        return literal(name).then(argument("pattern", pattern.argumentType()).suggests(pattern.suggestions())
                .then(argument("base", base.argumentType()).suggests(base.suggestions())
                        .then(argument("patternColor", base.argumentType()).suggests(base.suggestions())
                                .executes(ctx -> setFish(ctx, from, to)))));
    }

    private int setFish(CommandContext<FabricClientCommandSource> ctx, int from, int to) throws CommandSyntaxException {
        for (int i = from; i <= to; i++) {
            String prefix = "fish" + (i + 1) + "_";
            parseInto(ctx, option(prefix + "pattern"), "pattern");
            parseInto(ctx, option(prefix + "base"), "base");
            parseInto(ctx, option(prefix + "pattern_color"), "patternColor");
            ctx.getSource().sendFeedback(Text.translatable("koi-orbit.command.fish_changed", i + 1, config.fish[i].describe()));
        }
        config.save();
        return 1;
    }

    private <T> void parseInto(CommandContext<FabricClientCommandSource> ctx, KoiOptions.Opt<T> opt, String arg) throws CommandSyntaxException {
        opt.set(config, opt.parse(ctx, arg));
    }

    private static KoiOptions.Opt<?> option(String id) {
        return KoiOptions.ALL.stream().filter(o -> o.id.equals(id)).findFirst().orElseThrow();
    }

    private int show(FabricClientCommandSource source) {
        for (KoiOptions.Page page : KoiOptions.Page.values()) {
            MutableText line = Text.empty().append(page.getText().copy().formatted(Formatting.GOLD)).append(" ");
            boolean first = true;
            for (KoiOptions.Opt<?> opt : KoiOptions.on(page)) {
                if (!first) line.append(Text.literal(" · ").formatted(Formatting.DARK_GRAY));
                line.append(opt.label().copy().formatted(Formatting.GRAY)).append(" ").append(opt.valueText(config));
                first = false;
            }
            source.sendFeedback(line);
        }
        source.sendFeedback(Text.translatable("koi-orbit.command.usage"));
        return 1;
    }
}
