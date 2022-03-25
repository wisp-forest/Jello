package io.wispforest.jello.api.dye.client;

import io.wispforest.jello.api.dye.block.ColoredCandleBlock;
import io.wispforest.jello.api.dye.block.ColoredCandleCakeBlock;
import io.wispforest.jello.api.dye.block.ColoredGlassPaneBlock;
import io.wispforest.jello.api.dye.registry.DyeColorantJsonTest;
import io.wispforest.jello.api.dye.registry.DyeColorantRegistry;
import io.wispforest.jello.api.dye.registry.builder.BaseBlockBuilder;
import io.wispforest.jello.api.dye.registry.builder.BlockType;
import io.wispforest.jello.api.dye.registry.builder.VanillaBlockBuilder;
import io.wispforest.jello.api.registry.ColorBlockRegistry;
import io.wispforest.jello.api.util.MessageUtil;
import io.wispforest.jello.main.common.Jello;
import io.wispforest.jello.main.common.blocks.SlimeBlockColored;
import io.wispforest.jello.main.common.blocks.SlimeSlabColored;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.data.client.Model;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

public class BlockModelRedirect implements ModelVariantProvider {

    private static final MessageUtil MESSAGE_TOOL = new MessageUtil("Block Model Redirect");

    private static final Set<BaseBlockBuilder> ALL_BUILDERS = new HashSet<>();

    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
        if(ALL_BUILDERS.isEmpty()){
            ALL_BUILDERS.addAll(VanillaBlockBuilder.VANILLA_BUILDERS);
            ALL_BUILDERS.addAll(BaseBlockBuilder.ADDITIONAL_BUILDERS);
        }

        if (DyeColorantRegistry.shouldRedirectModelResource(new Identifier(modelId.getNamespace(), modelId.getPath()))) {
            //if(Objects.equals(modelId.getNamespace(), DyeColorantJsonTest.JSON_NAMESPACE)){
            String[] stringParts = modelId.getPath().split("_");

            //MESSAGE_TOOL.infoMessage(Arrays.toString(stringParts));



            if (Objects.equals(stringParts[stringParts.length - 1], "dye")) {
                return context.loadModel(new Identifier("jello", "item/dynamic_dye"));
            }

            String loadFromDirectory;
            if (Objects.equals(modelId.getVariant(), "inventory")) {
                loadFromDirectory = "item";
            } else {
                loadFromDirectory = "block";
            }

//            for(BaseBlockBuilder builder : ALL_BUILDERS){
//                List<BlockType> blockTypes = builder.getBlockTypes();
//
//                for(BlockType type : blockTypes) {
//                    if (type.isVariantType(modelId)) {
//                        return context.loadModel(new Identifier(builder.modid, loadFromDirectory + "/" + type.blockType));
//                    }
//                }
//            }

            if (Objects.equals(stringParts[stringParts.length - 1], "terracotta")) {
                return context.loadModel(new Identifier("jello", loadFromDirectory + "/terracotta"));
            } else if (Objects.equals(stringParts[stringParts.length - 1], "carpet")) {
                return context.loadModel(new Identifier("jello", loadFromDirectory + "/carpet"));
            } else if (Objects.equals(stringParts[stringParts.length - 1], "concrete")) {
                return context.loadModel(new Identifier("jello", loadFromDirectory + "/concrete"));
            } else if (Objects.equals(stringParts[stringParts.length - 1], "powder")) {
                return context.loadModel(new Identifier("jello", loadFromDirectory + "/concrete_powder"));
            } else if (Objects.equals(stringParts[stringParts.length - 1], "wool")) {
                return context.loadModel(new Identifier("jello", loadFromDirectory + "/wool"));
            } else if (Objects.equals(stringParts[stringParts.length - 1], "box")) {
                return context.loadModel(new Identifier(loadFromDirectory + "/shulker_box"));
            } else if (Objects.equals(stringParts[stringParts.length - 1], "bed")) {
                if (loadFromDirectory.equals("item")) {
                    return context.loadModel(new Identifier(loadFromDirectory + "/template_bed"));
                }
                return context.loadModel(new Identifier(loadFromDirectory + "/bed"));
            } else if (Objects.equals(stringParts[stringParts.length - 1], "glass")) {
                return context.loadModel(new Identifier("jello", loadFromDirectory + "/stained_glass"));
            } else if (Objects.equals(stringParts[stringParts.length - 1], "pane")) {
                if (loadFromDirectory.equals("item")) {
                    return context.loadModel(new Identifier("jello", loadFromDirectory + "/stained_glass_pane"));
                }

                return null;
            }else if(Objects.equals(stringParts[stringParts.length - 1], "candle")){
                if (loadFromDirectory.equals("item")) {
                    return context.loadModel(new Identifier(loadFromDirectory + "/white_candle"));
                }

                return null;
            }else if(Objects.equals(stringParts[stringParts.length - 1], "cake")){
                return null;
            }else if(Objects.equals(stringParts[stringParts.length - 1], "block")){
                if (loadFromDirectory.equals("item")) {
                    return context.loadModel(new Identifier("jello", loadFromDirectory + "/slime_block_multicolor"));
                }
            }else if(Objects.equals(stringParts[stringParts.length - 1], "slab")){
                if (loadFromDirectory.equals("item")) {
                    return context.loadModel(new Identifier("jello", loadFromDirectory + "/slime_slab_multicolor"));
                }
            }

//            UnbakedModel possibleModel = context.loadModel(modelId);
//            if(MISSING_MODEL == null){
//                MISSING_MODEL = context.loadModel(ModelLoader.MISSING_ID);
//            }
//
//            if(possibleModel == MISSING_MODEL){
//                MESSAGE_TOOL.infoMessage(Arrays.toString(stringParts));
//                MESSAGE_TOOL.failMessage("{Deetz Nuts} Failed to find model for " + modelId.toString());
//            }
        }

        return null;
    }

    public static class ResourceRedirectEntryPredicate implements Predicate<Block> {

        public static final Map<ResourceRedirectEntryPredicate, Identifier> BLOCKSTATE_PREDICATES = new HashMap<>();

        private static final ResourceRedirectEntryPredicate GLASS_PANE_PREDICATE = new ResourceRedirectEntryPredicate(ColoredGlassPaneBlock.class, new Identifier(Jello.MODID, "stained_glass_pane"));
        private static final ResourceRedirectEntryPredicate CANDLE_PREDICATE = new ResourceRedirectEntryPredicate(ColoredCandleBlock.class, new Identifier(Jello.MODID, "candle"));
        private static final ResourceRedirectEntryPredicate CANDLE_CAKE_PREDICATE = new ResourceRedirectEntryPredicate(ColoredCandleCakeBlock.class, new Identifier(Jello.MODID, "candle_cake"));

        private static final ResourceRedirectEntryPredicate SLIME_BLOCK_PREDICATE = new ResourceRedirectEntryPredicate(SlimeBlockColored.class, new Identifier(Jello.MODID, "slime_block"));
        private static final ResourceRedirectEntryPredicate SLIME_SLAB_PREDICATE = new ResourceRedirectEntryPredicate(SlimeSlabColored.class, new Identifier(Jello.MODID, "colored_slime_slab"));

        private final Class<? extends Block> klazz;

        public ResourceRedirectEntryPredicate(Class<? extends Block> klazz, Identifier resourceLocation){
            this.klazz = klazz;

            BLOCKSTATE_PREDICATES.put(this, resourceLocation);
        }

        @Override
        public boolean test(Block t) {
            return klazz.isInstance(t);
        }
    }
}