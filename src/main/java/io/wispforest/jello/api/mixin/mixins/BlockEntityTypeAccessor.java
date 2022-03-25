package io.wispforest.jello.api.mixin.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {
    @Accessor("blocks")
    Set<Block> jello$getBlocks();

    @Accessor("blocks")
    @Mutable
    void jello$setBlocks(Set<Block> blocks);
}