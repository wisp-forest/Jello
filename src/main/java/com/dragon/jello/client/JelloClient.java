package com.dragon.jello.client;

import com.dragon.jello.blocks.BlockRegistry;
import com.dragon.jello.items.ItemRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.render.RenderLayer;

public class JelloClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRegistry.SlimeBlockRegistry.SLIME_BLOCKS.forEach((block)->{
            ColorProviderRegistry.BLOCK.register((BlockColorProvider)block, block);
            ColorProviderRegistry.ITEM.register((ItemColorProvider)block.asItem(), block.asItem());

            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getTranslucent());
        });

        BlockRegistry.SlimeSlabRegistry.SLIME_SLABS.forEach((block)->{
            ColorProviderRegistry.BLOCK.register((BlockColorProvider)block, block);
            ColorProviderRegistry.ITEM.register((ItemColorProvider)block.asItem(), block.asItem());

            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getTranslucent());
        });

        ItemRegistry.SlimeBlockItemRegistry.SLIME_BALLS.forEach((item) -> ColorProviderRegistry.ITEM.register((ItemColorProvider)item, item));

    }
}