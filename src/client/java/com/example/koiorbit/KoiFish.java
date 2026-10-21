package com.example.koiorbit;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

// 월드에 추가하지 않고 렌더링에만 쓰는 열대어
public class KoiFish extends TropicalFishEntity {
    private FishLook look;

    public KoiFish(World world, FishLook look) {
        super(EntityType.TROPICAL_FISH, world);
        setLook(look);
    }

    public FishLook getLook() {
        return look;
    }

    public void setLook(FishLook look) {
        this.look = look;
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Variant", look.pattern().getId() | look.base().getId() << 16 | look.patternColor().getId() << 24);
        readCustomDataFromNbt(nbt);
    }

    // 물 밖에서도 옆으로 눕지 않고 헤엄치는 자세로 그리기
    @Override
    public boolean isTouchingWater() {
        return true;
    }
}
