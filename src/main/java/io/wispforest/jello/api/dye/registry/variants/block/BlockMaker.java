package io.wispforest.jello.api.dye.registry.variants.block;

import io.wispforest.jello.api.dye.DyeColorant;
import net.minecraft.block.Block;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

/**
 * Used internally within {@link DyeableBlockVariant#makeChildBlock} to create variant blocks.
 */
@ApiStatus.NonExtendable
public interface BlockMaker {
    Block createBlockFromDyeColor(DyeColorant dyeColorant, @Nullable Block parentBlock);
}
