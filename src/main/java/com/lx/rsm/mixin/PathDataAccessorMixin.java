package com.lx.rsm.mixin;

import mtr.path.PathData;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PathData.class, remap = false)
public interface PathDataAccessorMixin {
    @Accessor()
    BlockPos getEndingPos();
}
