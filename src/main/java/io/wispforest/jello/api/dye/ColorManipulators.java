package io.wispforest.jello.api.dye;

import io.wispforest.jello.api.ducks.entity.DyeableEntity;
import io.wispforest.jello.api.ducks.entity.RainbowEntity;
import io.wispforest.jello.api.dye.registry.DyeColorantRegistry;
import io.wispforest.jello.api.dye.registry.variants.block.DyeableBlockVariant;
import io.wispforest.jello.misc.dye.JelloStats;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

public class ColorManipulators {

    /**
     * Main method used to change the Color of Dyeable Block Variant based on the {@link DyeColorant} given
     *
     * @param world Current World
     * @param blockPos The Block being dyed position
     * @param dyeColorant
     * @param player The player Coloring the Block
     * @return True if the Block was found to be a DyeableBlockVariant and was Dyed
     */
    public static boolean changeBlockColor(World world, BlockPos blockPos, DyeColorant dyeColorant, PlayerEntity player, boolean playBlockSound) {
        BlockState currentBlockState = world.getBlockState(blockPos);

        Pair<Block, DyeableBlockVariant> coloredPair = DyeableBlockVariant.attemptToGetColoredBlockPair(currentBlockState.getBlock(), dyeColorant);

        if (coloredPair == null || coloredPair.getLeft() == currentBlockState.getBlock())
            return false;

        if(playBlockSound){
            world.playSound(player, blockPos, currentBlockState.getBlock().getSoundGroup(currentBlockState).getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        return coloredPair.getRight().getAlterColorMethod().changeState(world, blockPos, currentBlockState, coloredPair.getLeft(), player);
    }

    /**
     * Method used to change the color of a item used on a Cauldron
     */
    @ApiStatus.Experimental
    @ApiStatus.Internal
    public static boolean changeBlockItemOrItemColor(World world, BlockPos cauldronPos, ItemStack oldStack, ItemConvertible changedBlock, PlayerEntity player, Hand hand, boolean washingBlock) {
        ItemConvertible oldEntry;

        if(oldStack.getItem() instanceof BlockItem blockItem && changedBlock instanceof Block){
            oldEntry = blockItem;

            if (changedBlock == blockItem.getBlock())
                return false;

        } else {
            oldEntry = oldStack.getItem();

            if (changedBlock.asItem() == oldEntry)
                return false;
        }

        if (!world.isClient) {
            int stackDecrementAmount = 1;

            if (oldEntry.asItem().getMaxCount() > 1)
                stackDecrementAmount = Math.min(oldStack.getCount(), 8);

            ItemStack changedItemStack = new ItemStack(changedBlock, stackDecrementAmount);

            if (oldStack.hasNbt())
                changedItemStack.setNbt(oldStack.getNbt().copy());

            if (!player.getAbilities().creativeMode || (oldEntry instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock))
                oldStack.decrement(stackDecrementAmount);

            BlockPos blockPos = cauldronPos.up();
            ItemScatterer.spawn(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), changedItemStack);

            if (washingBlock) {
                player.incrementStat(JelloStats.CLEAN_BLOCK);
            } else {
                player.incrementStat(JelloStats.DYE_BLOCK);
            }
        }

        return true;

    }


    /**
     * Interface that allows for {@link DyeableBlockVariant} to allow for custom data to be transfered when a variant Block is colored
     */
    public interface AlterBlockColor {
        AlterBlockColor DEFAULT = (world, blockPos, currentState, newBlock, player) -> {
            if(!world.isClient) world.setBlockState(blockPos, newBlock.getStateWithProperties(currentState));

            return true;
        };

        boolean changeState(World world, BlockPos blockPos, BlockState currentState, Block newBlock, PlayerEntity player);
    }

    /**
     * Interface that allows for {@link DyeableBlockVariant} to allow for custom data to be transfered when a variant Item is colored
     */
    public interface AlterItemColor {
        AlterItemColor DEFAULT_BLOCK = (world, blockPos, oldStack, newConvertible, player) -> {
            if(oldStack.getItem() instanceof BlockItem blockItem && newConvertible instanceof Block block){
                return block != blockItem.getBlock();
            }

            return true;
        };

        AlterItemColor DEFAULT_ITEM = (world, blockPos, oldStack, newConvertible, player) -> newConvertible.asItem() != oldStack.getItem();

        boolean changeStack(World world, BlockPos blockPos, ItemStack oldStack, ItemConvertible newConvertible, PlayerEntity player);
    }

    //--------------------------------------------------------------

    /**
     * Event used when an {@link DyeableEntity} will Change its color based upon the given {@link DyeColorant}
     *
     * @param dyeableEntity Entity to be Dyed
     * @param dyeColor Color to be applyed to the Entity
     * @return True if the Entity's color was changed
     */
    public static boolean dyeEntityEvent(DyeableEntity dyeableEntity, DyeColorant dyeColor) {
        if ((dyeableEntity.isRainbowTime()) || dyeColor == dyeableEntity.getDyeColor()) {//|| dyeItem.getColor().getId() == DyeColor.WHITE.getId()) {
            return false;
        }

        dyeableEntity.setDyeColor(dyeColor);

        return true;
    }

    /**
     * Event used when an {@link RainbowEntity} is to be Rainbowed
     *
     * @param rainbowEntity Entity to be Dyed
     * @return True if the Entity's was Raindowed
     */
    public static boolean rainbowEntityEvent(RainbowEntity rainbowEntity) {
        if (rainbowEntity.isRainbowTime() || (rainbowEntity instanceof DyeableEntity dyeableEntity && dyeableEntity.isDyed())) {
            return false;
        }

        rainbowEntity.setRainbowTime(true);

        return true;
    }

    /**
     * Event used when an {@link DyeableEntity} is to be DeColored or De-Rainbowed
     *
     * @param dyeableEntity Entity to be cleaned
     * @return True if the Entity's was Cleaned
     */
    public static boolean washEntityEvent(DyeableEntity dyeableEntity) {
        boolean washedEntity = false;

        if (dyeableEntity.isDyed()) {
            dyeableEntity.setDyeColor(DyeColorantRegistry.NULL_VALUE_NEW);
            washedEntity = true;
        }

        if (dyeableEntity.isRainbowTime()) {
            dyeableEntity.setRainbowTime(false);
            washedEntity = true;
        }

        return washedEntity;
    }

    //--------------------------------------------------------------

    public static void decrementPlayerHandItemCC(PlayerEntity player, Hand hand) {
        if (!player.getAbilities().creativeMode)
            ItemOps.decrementPlayerHandItem(player, hand);
    }
}
