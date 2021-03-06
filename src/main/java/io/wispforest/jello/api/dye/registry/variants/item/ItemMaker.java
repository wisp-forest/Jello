package io.wispforest.jello.api.dye.registry.variants.item;

import io.wispforest.jello.api.dye.DyeColorant;
import io.wispforest.jello.api.dye.registry.variants.block.DyeableBlockVariant;
import io.wispforest.jello.item.ColoredBlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import org.jetbrains.annotations.ApiStatus;

/**
 * Used internally within {@link DyeableBlockVariant#makeBlockItem} and {@link DyeableItemVariant#makeItem} to create variant items (If the variant has such).
 */
@ApiStatus.NonExtendable
public interface ItemMaker {
    ItemMaker BLOCK_DEFAULT = (dyeColorant, block, settings) -> new ColoredBlockItem((Block)block, settings);

    Item createItemFromDyeColor(DyeColorant dyeColorant, ItemConvertible parentEntry, Item.Settings settings);
}
