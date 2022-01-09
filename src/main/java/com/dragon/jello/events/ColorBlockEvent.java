package com.dragon.jello.events;

import com.dragon.jello.mixin.ducks.DyeableEntity;
import com.dragon.jello.mixin.ducks.RainbowEntity;
import com.dragon.jello.tags.JelloBlockTags;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ColorBlockEvent implements UseBlockCallback {

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        Item mainHandItem = player.getMainHandStack().getItem();
        BlockState blockState = world.getBlockState(hitResult.getBlockPos());

        if(player.shouldCancelInteraction()){
            if(mainHandItem instanceof DyeItem dyeItem){
                String suffix;
                SoundEvent blockChangeSound;

                if(blockState.isIn(BlockTags.WOOL)){
                    suffix = "_wool";
                    blockChangeSound = SoundEvents.BLOCK_WOOL_PLACE;
                }
                else if(blockState.isIn(BlockTags.CARPETS)){
                    suffix = "_carpet";
                    blockChangeSound = SoundEvents.BLOCK_WOOL_PLACE;
                }
                else if(blockState.isIn(BlockTags.TERRACOTTA)){
                    suffix = "_terracotta";
                    blockChangeSound = SoundEvents.BLOCK_STONE_PLACE;
                }
                else if(blockState.isIn(JelloBlockTags.CONCRETE)){
                    suffix = "_concrete";
                    blockChangeSound = SoundEvents.BLOCK_STONE_PLACE;
                }
                else if(blockState.isIn(BlockTags.CANDLES)){
                    suffix = "_candle";
                    blockChangeSound = SoundEvents.BLOCK_CANDLE_PLACE;
                }
                else if(blockState.isIn(BlockTags.IMPERMEABLE)){
                    suffix = "_stained_glass";
                    blockChangeSound = SoundEvents.BLOCK_GLASS_PLACE;
                }
                else if(blockState.isIn(JelloBlockTags.COLORED_GLASS_PANES)){
                    suffix = "_stained_glass_pane";
                    blockChangeSound = SoundEvents.BLOCK_GLASS_PLACE;
                }
                else{
                    return ActionResult.PASS;
                }

                Identifier id = Registry.BLOCK.getId(blockState.getBlock());

                if(id.getNamespace().equals("minecraft")){
                    if(getColorPrefix(id).equals(dyeItem.getColor().getName())){
                        return ActionResult.FAIL;
                    }

                    Block changedBlock = Registry.BLOCK.get(new Identifier(dyeItem.getColor().getName() + suffix));

                    if(!world.isClient){
                        world.setBlockState(hitResult.getBlockPos(), changedBlock.getStateWithProperties(blockState));
                    }

                    world.playSound(player, hitResult.getBlockPos(), blockChangeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    decrementPlayerHandItemCC(player, hand);

                    return ActionResult.SUCCESS;
                }
            }else if (mainHandItem instanceof BucketItem bucketItem) {
                Identifier id = Registry.BLOCK.getId(blockState.getBlock());

                if(id.getNamespace().equals("minecraft")) {
                    return bucketItemEvent(player, world, blockState, hitResult, player.getMainHandStack(), bucketItem);
                }

            }
        }

        return ActionResult.PASS;
    }

    private ActionResult bucketItemEvent(PlayerEntity player, World world, BlockState blockState, BlockHitResult hitResult, ItemStack mainHandStack, BucketItem bucketItem){
        if (bucketItem.fluid == Fluids.WATER) {
            if(isAlreadyDefault(blockState.getBlock())){
                return ActionResult.FAIL;
            }

            String defaultBlockName;
            SoundEvent blockChangeSound;

            if(blockState.isIn(BlockTags.TERRACOTTA)){
                defaultBlockName = "terracotta";
                blockChangeSound = SoundEvents.BLOCK_STONE_PLACE;
            }
            else if(blockState.isIn(BlockTags.CANDLES)){
                defaultBlockName = "candle";
                blockChangeSound = SoundEvents.BLOCK_CANDLE_PLACE;
            }
            else if(blockState.isIn(BlockTags.IMPERMEABLE)){
                defaultBlockName = "glass";
                blockChangeSound = SoundEvents.BLOCK_GLASS_PLACE;
            }
            else if(blockState.isIn(JelloBlockTags.COLORED_GLASS_PANES)){
                defaultBlockName = "glass_pane";
                blockChangeSound = SoundEvents.BLOCK_GLASS_PLACE;
            }
            else{
                return ActionResult.PASS;
            }


            Block changedBlock = Registry.BLOCK.get(new Identifier(defaultBlockName));
            if(!world.isClient){
                world.setBlockState(hitResult.getBlockPos(), changedBlock.getStateWithProperties(blockState));
            }

            world.playSound(player, hitResult.getBlockPos(), blockChangeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
            player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1.0F, 1.0F);

            if(!player.getAbilities().creativeMode){
                ItemUsage.exchangeStack(mainHandStack, player, BucketItem.getEmptiedStack(mainHandStack, player));
            }
            return ActionResult.SUCCESS;

        }

        return ActionResult.PASS;
    }

    private void decrementPlayerHandItemCC(PlayerEntity player, Hand hand){
        if(!player.getAbilities().creativeMode){
            ItemOps.decrementPlayerHandItem(player, hand);
        }
    }

    private String getColorPrefix(Identifier identifier){
        String[] splitName = identifier.getPath().split("_");
        if(splitName.length >= 2){
            return splitName[0];
        }else{
            return "";
        }
    }

    private boolean isAlreadyDefault(Block block){
        if(block == Blocks.GLASS || block == Blocks.TINTED_GLASS){
            return true;
        }else if(block == Blocks.TERRACOTTA){
            return true;
        }else if(block == Blocks.CANDLE){
            return true;
        }else{
            return false;
        }

    }
}
