package com.lx.rsm.mixin;

import mtr.data.Train;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Mixin(value = Train.class, remap = false)
public interface TrainAccessorMixin {
    @Accessor
    boolean getReversed();

    @Accessor
    boolean getIsManualAllowed();

    @Accessor
    boolean getIsCurrentlyManual();

    @Accessor
    int getManualNotch();

    @Accessor
    int getManualToAutomaticTime();

    @Accessor
    Set<UUID> getRidingEntities();

    @Accessor
    SimpleInventory getInventory();

    @Accessor
    boolean getIsOnRoute();

    @Accessor
    float getSpeed();

    @Accessor
    List<Double> getDistances();

    @Invoker("getRoutePosition")
    Vec3d getTheRoutePosition(int car, int trainSpacing);
}
