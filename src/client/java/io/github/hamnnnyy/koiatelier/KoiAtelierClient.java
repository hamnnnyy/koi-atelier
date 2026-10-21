package io.github.hamnnnyy.koiatelier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KoiAtelierClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("koi-atelier");
    private static final int MESSAGE_TICKS = 40; // 알림 표시 시간 (2초)

    private static KoiAtelierClient instance;

    private final KoiConfig config = KoiConfig.load();
    private final KoiAura aura = new KoiAura(config);
    private final KoiTrail trail = new KoiTrail(config);
    private final KoiPet pet = new KoiPet(config);
    private final KoiHud hud = new KoiHud(config);
    private final KoiFish[] fish = new KoiFish[2];
    private ClientWorld fishWorld;
    private KeyBinding toggleKey, menuKey, fullbrightKey;
    private KoiZoom zoom;

    // 믹스인에서 쓰는 접근자 (모드가 초기화되기 전에는 null)
    public static KoiAtelierClient get() {
        return instance;
    }

    public KoiConfig config() {
        return config;
    }

    public KoiZoom zoom() {
        return zoom;
    }

    public static Screen createConfigScreen(Screen parent) {
        return new KoiConfigScreen(parent, instance.config, null);
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        // 설정 > 조작에서 바꿀 수 있음. 설정 화면은 다른 모드와 겹치지 않게 기본 키 없음
        toggleKey = register("toggle", GLFW.GLFW_KEY_K);
        menuKey = register("menu", GLFW.GLFW_KEY_UNKNOWN);
        fullbrightKey = register("fullbright", GLFW.GLFW_KEY_B);
        // C는 바닐라 "단축바 저장" 키와 겹쳐서 Z
        zoom = new KoiZoom(config, register("zoom", GLFW.GLFW_KEY_Z));

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(this::render);
        HudRenderCallback.EVENT.register(hud::render);
        // 후광, 날개: 플레이어 모델에 붙임
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
            if (renderer instanceof PlayerEntityRenderer player) helper.register(new KoiPlayerFeature(player, config));
        });

        // 채팅창이 닫힌 다음에 열리도록 한 프레임 미룸
        KoiCommand command = new KoiCommand(config,
                () -> MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(createConfigScreen(null))));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> command.register(dispatcher));
    }

    private static KeyBinding register(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.koi-atelier." + name, InputUtil.Type.KEYSYM, key, "category.koi-atelier"));
    }

    private void tick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            config.enabled = !config.enabled;
            config.save();
            hud.show(Text.translatable(config.enabled ? "koi-atelier.message.fish_on" : "koi-atelier.message.fish_off"),
                    config.enabled ? 0x55FF55 : 0xAAAAAA, MESSAGE_TICKS);
        }
        while (fullbrightKey.wasPressed()) {
            config.fullbright = !config.fullbright;
            config.save();
            hud.show(Text.translatable(config.fullbright ? "koi-atelier.message.fullbright_on" : "koi-atelier.message.fullbright_off"),
                    config.fullbright ? 0xFFFF55 : 0xAAAAAA, MESSAGE_TICKS);
        }
        while (menuKey.wasPressed()) {
            if (client.currentScreen == null) client.setScreen(createConfigScreen(null));
        }
        zoom.tick(client);
        hud.tick(client);

        if (client.world == null) {
            fishWorld = null;
            return;
        }
        // 월드/서버를 옮기면 새로 만들기
        if (client.world != fishWorld) {
            fishWorld = client.world;
            for (int i = 0; i < fish.length; i++) fish[i] = new KoiFish(fishWorld, config.fish[i]);
        }
        for (int i = 0; i < fish.length; i++) {
            if (!fish[i].getLook().equals(config.fish[i])) fish[i].setLook(config.fish[i]);
            fish[i].age++; // age가 올라가야 꼬리가 움직임
        }
        aura.tick(client);
        trail.tick(client);
        pet.tick(client);
    }

    private void render(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || fishWorld != mc.world || ctx.matrixStack() == null || ctx.consumers() == null) return;
        float tickDelta = ctx.tickCounter().getTickDelta(true);
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        dispatcher.setRenderShadows(false);
        renderFish(ctx, mc, dispatcher, tickDelta);
        pet.render(ctx, dispatcher, tickDelta);
        dispatcher.setRenderShadows(true);
        renderHeadText(ctx, mc, tickDelta);
    }

    private void renderFish(WorldRenderContext ctx, MinecraftClient mc, EntityRenderDispatcher dispatcher, float tickDelta) {
        if (!config.enabled || (!config.firstPerson && mc.options.getPerspective().isFirstPerson())) return;
        MatrixStack matrices = ctx.matrixStack();
        Vec3d cam = ctx.camera().getPos();
        Vec3d p = mc.player.getLerpedPos(tickDelta);
        double t = (mc.world.getTime() + tickDelta) / 20.0;

        for (int i = 0; i < fish.length; i++) {
            double angle = t * config.speed + i * Math.PI; // 두 마리는 서로 반대편
            double x = p.x + Math.cos(angle) * config.radius;
            double y = p.y + config.height + Math.sin(t * 2 + i) * config.bob; // 위아래로 출렁
            double z = p.z + Math.sin(angle) * config.radius;
            // 진행 방향을 보게 (속도가 음수면 반대로 돔) + 몸을 좌우로 살짝 흔들기
            float yaw = (float) Math.toDegrees(angle) + (config.speed < 0 ? 180f : 0f) + (float) Math.sin(t * 6 + i) * 10f;
            float size = (float) config.size;

            KoiFish f = fish[i];
            f.setPosition(x, y, z);
            f.prevX = x; f.prevY = y; f.prevZ = z;
            f.setYaw(yaw); f.prevYaw = yaw;
            f.bodyYaw = yaw; f.prevBodyYaw = yaw;
            f.headYaw = yaw; f.prevHeadYaw = yaw;

            matrices.push();
            matrices.translate(x - cam.x, y - cam.y, z - cam.z);
            matrices.scale(size, size, size);
            dispatcher.render(f, 0, 0, 0, yaw, tickDelta, matrices, ctx.consumers(),
                    LightmapTextureManager.MAX_LIGHT_COORDINATE); // 항상 밝게
            matrices.pop();
        }
    }

    // 머리 위 문구: 바닐라 이름표처럼 항상 카메라를 바라봄
    private void renderHeadText(WorldRenderContext ctx, MinecraftClient mc, float tickDelta) {
        ClientPlayerEntity player = mc.player;
        if (!config.headTextEnabled || config.headText.isBlank() || player.isInvisible()) return;
        if (mc.options.getPerspective().isFirstPerson()) return;

        Vec3d cam = ctx.camera().getPos();
        Vec3d p = player.getLerpedPos(tickDelta);
        double y = p.y + player.getHeight() + (config.haloEnabled ? 0.75 : 0.5);
        Text text = Text.literal(config.headText);
        TextRenderer tr = mc.textRenderer;
        int background = (int) (mc.options.getTextBackgroundOpacity(0.25f) * 255) << 24;

        MatrixStack matrices = ctx.matrixStack();
        matrices.push();
        matrices.translate(p.x - cam.x, y - cam.y, p.z - cam.z);
        matrices.multiply(ctx.camera().getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);
        tr.draw(text, -tr.getWidth(text) / 2f, 0, 0xFF000000 | (config.headTextColor.getEntityColor() & 0xFFFFFF), false,
                matrices.peek().getPositionMatrix(), ctx.consumers(), TextRenderer.TextLayerType.NORMAL, background,
                LightmapTextureManager.MAX_LIGHT_COORDINATE);
        matrices.pop();
    }
}
