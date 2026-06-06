package com.megu.neferpitou.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PitouDataProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<PitouData> PITOU_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    private PitouData data = null;
    private final LazyOptional<PitouData> optional = LazyOptional.of(this::getOrCreate);

    private PitouData getOrCreate() {
        if (data == null) data = new PitouData();
        return data;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PITOU_DATA) return optional.cast();
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        getOrCreate().saveNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreate().loadNBT(nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
