package com.example.koiorbit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.DyeColor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

// config/koi-orbit.json 에 저장되는 설정. 값의 범위와 화면/명령어는 KoiOptions에서 정의
public class KoiConfig {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("koi-orbit.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 물고기
    public boolean enabled = true;
    public boolean firstPerson = false; // 1인칭에서도 물고기/오라/펫이 보일지
    public double size = 1.5;
    public double radius = 1.2;
    public double height = 0.6;
    public double speed = 1.5;          // 음수면 반대 방향
    public double bob = 0.1;
    public FishLook[] fish = {FishLook.GOLD, FishLook.BLACK};

    // 오라
    public boolean auraEnabled = true;
    public AuraType auraType = AuraType.FLAME;
    public AuraPosition auraPosition = AuraPosition.SHOULDERS;
    public DyeColor auraColor = DyeColor.YELLOW; // dust 타입일 때만 사용
    public double auraDensity = 1;               // 틱당 파티클 수

    // 발자국
    public boolean trailEnabled = false;
    public AuraType trailType = AuraType.CHERRY;
    public DyeColor trailColor = DyeColor.PINK;
    public double trailDensity = 1;

    // 펫
    public boolean petEnabled = false;
    public PetType petType = PetType.ALLAY;
    public double petSize = 1;

    // 머리 위 장식
    public boolean haloEnabled = false;
    public DyeColor haloColor = DyeColor.YELLOW;
    public double haloSize = 1;
    public boolean headTextEnabled = false;
    public String headText = "";
    public DyeColor headTextColor = DyeColor.WHITE;

    // 망토 / 날개
    public boolean capeEnabled = false;
    public CapeDesign capeDesign = CapeDesign.KOI;
    public boolean wingsEnabled = false;
    public WingType wingsType = WingType.ANGEL;
    public DyeColor wingsColor = DyeColor.WHITE;

    // HUD
    public boolean coordsEnabled = true;
    public boolean coordsBiome = true;
    public boolean minimapEnabled = true;
    public double minimapSize = 80;  // 화면에서의 크기 (GUI 단위)
    public double minimapScale = 1;  // 픽셀당 블록 수
    public boolean durabilityEnabled = true;
    public double durabilityPercent = 10;
    public double zoomAmount = 4;
    public boolean fullbright = false;

    // 모든 값을 기본값으로
    public void reset() {
        copyFrom(new KoiConfig(), false);
    }

    // onlyNulls가 true면 비어 있는(null) 값만 채움
    private void copyFrom(KoiConfig other, boolean onlyNulls) {
        try {
            for (Field field : KoiConfig.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (!onlyNulls || field.get(this) == null) {
                    Object value = field.get(other);
                    field.set(this, value instanceof FishLook[] looks ? looks.clone() : value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static KoiConfig load() {
        KoiConfig config = new KoiConfig();
        if (!Files.exists(PATH)) return config;
        try (Reader reader = Files.newBufferedReader(PATH)) {
            KoiConfig loaded = GSON.fromJson(reader, KoiConfig.class);
            if (loaded == null) return config;
            // 모르는 이름의 enum 값 등은 null로 들어오므로 기본값으로 채움
            loaded.copyFrom(config, true);
            // 잘못된 물고기 값이 있으면 그 물고기만 기본값으로
            FishLook[] fish = config.fish.clone();
            if (loaded.fish.length == 2) {
                for (int i = 0; i < 2; i++) {
                    if (loaded.fish[i] != null && loaded.fish[i].isValid()) fish[i] = loaded.fish[i];
                }
            }
            loaded.fish = fish;
            // 파일을 직접 고쳐서 범위를 벗어난 값이 들어가도 범위 안으로
            KoiOptions.validate(loaded);
            return loaded;
        } catch (Exception e) {
            KoiOrbitClient.LOGGER.warn("Failed to read {}, using defaults", PATH, e);
            return config;
        }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(PATH)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            KoiOrbitClient.LOGGER.warn("Failed to save {}", PATH, e);
        }
    }
}
