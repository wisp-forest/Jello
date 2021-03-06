package io.wispforest.jello.misc.behavior;

import io.wispforest.jello.api.ducks.DyeItemStorage;
import io.wispforest.jello.api.ducks.entity.DyeableEntity;
import io.wispforest.jello.misc.dye.JelloGameEvents;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class ColorEntityBehavior extends FallibleItemDispenserBehavior {

    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        World world = pointer.getWorld();
        if (!world.isClient()) {
            if(stack.getItem() instanceof DyeItemStorage dyeItemStorage) {
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                this.setSuccess(tryColorEntity((ServerWorld) world, blockPos, dyeItemStorage));
                if (this.isSuccess()) {
                    stack.decrement(1);
                }
            }

        }

        return stack;
    }

    private static boolean tryColorEntity(ServerWorld world, BlockPos pos, DyeItemStorage dyeItemStorage) {
        if(!dyeItemStorage.isDyeItem()){
            return false;
        }

        for (LivingEntity livingEntity : world.getEntitiesByClass(LivingEntity.class, new Box(pos), EntityPredicates.EXCEPT_SPECTATOR)) {
            if (livingEntity instanceof DyeableEntity dyeableEntity && dyeableEntity.getDyeColor() != dyeItemStorage.getDyeColorant()) {
                dyeableEntity.setDyeColor(dyeItemStorage.getDyeColorant());

                livingEntity.world.playSoundFromEntity((PlayerEntity) null, livingEntity, SoundEvents.ITEM_DYE_USE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                world.emitGameEvent((Entity) null, JelloGameEvents.DYE_ENTITY, pos);

                return true;
            }
        }

        return false;
    }
}
