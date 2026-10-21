package io.github.hamnnnyy.koiatelier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.option.Perspective;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

// 설정 화면. page가 null이면 페이지 고르는 첫 화면.
// 꾸미기 페이지는 열려 있는 동안 3인칭 정면으로 보여주고 게임을 멈추지 않아서 바꾸는 즉시 미리보기 됨
public class KoiConfigScreen extends GameOptionsScreen {
    private final KoiConfig config;
    private final KoiOptions.Page page;
    private Perspective previousPerspective;
    private boolean previousHudHidden;

    public KoiConfigScreen(Screen parent, KoiConfig config, KoiOptions.Page page) {
        super(parent, MinecraftClient.getInstance().options,
                page == null ? Text.translatable("koi-atelier.screen.title") : Text.translatable("koi-atelier.screen.page_title", page.getText()));
        this.config = config;
        this.page = page;
    }

    private boolean previews() {
        return page != null && page.previewsPlayer() && client.world != null;
    }

    @Override
    protected void init() {
        // 미리보기용: 3인칭 정면 + 채팅/핫바 숨기기 (닫으면 원래대로)
        if (previousPerspective == null && previews()) {
            previousPerspective = client.options.getPerspective();
            previousHudHidden = client.options.hudHidden;
            client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
            client.options.hudHidden = true;
        }
        super.init();
    }

    @Override
    protected void addOptions() {
        if (page == null) {
            KoiOptions.Page[] pages = KoiOptions.Page.values();
            for (int i = 0; i < pages.length; i += 2) {
                body.addWidgetEntry(pageButton(pages[i]), i + 1 < pages.length ? pageButton(pages[i + 1]) : null);
            }
            return;
        }
        // 두 개씩 한 줄. 새 기능 묶음(startsRow)은 앞줄이 한 칸 남아도 새 줄에서 시작
        ClickableWidget left = null;
        for (KoiOptions.Opt<?> opt : KoiOptions.on(page)) {
            if (opt.startsRow() && left != null) {
                body.addWidgetEntry(left, null);
                left = null;
            }
            ClickableWidget widget = opt.widget(config).createWidget(client.options, 0, 0, 150);
            if (left == null) {
                left = widget;
            } else {
                body.addWidgetEntry(left, widget);
                left = null;
            }
        }
        if (left != null) body.addWidgetEntry(left, null);
    }

    private ButtonWidget pageButton(KoiOptions.Page target) {
        return ButtonWidget.builder(target.getText(), b -> client.setScreen(new KoiConfigScreen(this, config, target))).width(150).build();
    }

    @Override
    protected void initFooter() {
        DirectionalLayoutWidget row = layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        Text resetText = Text.translatable(page == null ? "koi-atelier.screen.reset_all" : "koi-atelier.screen.reset_page");
        row.add(ButtonWidget.builder(resetText, button -> {
            if (page == null) config.reset();
            else KoiOptions.reset(config, page);
            client.setScreen(new KoiConfigScreen(parent, config, page));
        }).width(150).build());
        row.add(ButtonWidget.builder(ScreenTexts.DONE, button -> close()).width(150).build());
    }

    @Override
    public void removed() {
        super.removed();
        config.save();
        if (previousPerspective != null) {
            client.options.setPerspective(previousPerspective);
            client.options.hudHidden = previousHudHidden;
            previousPerspective = null;
        }
    }

    // 게임을 멈추지 않아야 물고기가 계속 돌고 오라가 계속 나옴
    @Override
    public boolean shouldPause() {
        return false;
    }

    // 페이지 안에서는 블러 없이 살짝만 어둡게 해서 뒤의 캐릭터(HUD 페이지는 HUD)가 비쳐 보이게
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (client.world == null || page == null) {
            super.renderBackground(context, mouseX, mouseY, delta);
        } else {
            context.fill(0, 0, width, height, 0x55000000);
        }
    }
}
