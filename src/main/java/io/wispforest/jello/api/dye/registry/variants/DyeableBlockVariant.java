package io.wispforest.jello.api.dye.registry.variants;

import io.wispforest.jello.Jello;
import io.wispforest.jello.api.dye.DyeColorant;
import io.wispforest.jello.api.dye.registry.DyeColorantRegistry;
import io.wispforest.jello.data.tags.JelloTags;
import io.wispforest.jello.item.ColoredBlockItem;
import io.wispforest.jello.api.registry.ColorBlockRegistry;
import io.wispforest.jello.api.item.JelloItemSettings;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.util.TagInjector;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A {@link DyeableBlockVariant} is a way to add your own
 * Dyed Block Variants, like Minecraft's Wool and Concrete,
 * to Jello's System so that any {@link DyeColorant}
 * created gets made with your Variant.
 */
public class DyeableBlockVariant {

    public static final Set<DyeableBlockVariant> ALL_BLOCK_VARIANTS = new HashSet<>();

    public static final Set<DyeableBlockVariant> ADDITION_BLOCK_VARIANTS = new HashSet<>();

    /**
     * None: The type telling the builder that is a single block and has no other variants that depend on this block to build
     * Chain: The type telling the builder that this {@link BlockMaker} needs a block to be created first
     */
    public enum RecursiveType {
        NONE(),
        CHAINED()
    }

    public final Identifier variantIdentifier;
    public final int wordCount;

    private Identifier defaultBlock;

    public final RecursiveType recursiveType;
    public final @Nullable Supplier<DyeableBlockVariant> childVariant;

    private final BlockMaker blockMaker;
    public BlockItemSettings defaultSettings;

    public final boolean createBlockItem;
    private BlockItemMaker blockItemMaker;

    private boolean addCustomDefaultBlockToTag;

    public final TagKey<Block> primaryBlockTag;
    public final Set<TagKey<Block>> secondaryBlockTags = new HashSet<>();

    public final TagKey<Item> primaryItemTag;
    public final Set<TagKey<Item>> secondaryItemTags = new HashSet<>();

    private DyeableBlockVariant(Identifier variantIdentifier, @Nullable Supplier<DyeableBlockVariant> possibleChildVariant, boolean noBlockItem, @Nullable ItemGroup defaultGroup, BlockMaker blockMaker) {
        this.variantIdentifier = variantIdentifier;
        this.blockMaker = blockMaker;
        this.createBlockItem = noBlockItem;

        String[] partParts = variantIdentifier.getPath().split("_");
        this.wordCount = partParts.length;

        this.defaultSettings = defaultGroup != null ? BlockItemSettings.of(defaultGroup) : BlockItemSettings.of();

        if (possibleChildVariant != null) {
            this.recursiveType = RecursiveType.CHAINED;
            this.childVariant = possibleChildVariant;
        } else {
            this.recursiveType = RecursiveType.NONE;
            this.childVariant = null;
        }

        this.defaultBlock = new Identifier(variantIdentifier.getNamespace(), "white_" + variantIdentifier.getPath());
        this.blockItemMaker = BlockItemMaker.DEFAULT;

        this.primaryBlockTag = TagKey.of(Registry.BLOCK_KEY, Jello.id(variantIdentifier.getPath()));
        this.primaryItemTag = TagKey.of(Registry.ITEM_KEY, Jello.id(variantIdentifier.getPath()));
    }

    //---------------------------------------------------------------------------------------------------

    public static DyeableBlockVariant of(Identifier variantIdentifier, Supplier<DyeableBlockVariant> possibleChildVariant, boolean noBlockItem, ItemGroup defaultGroup, BlockMaker blockMaker) {
        return new DyeableBlockVariant(variantIdentifier, possibleChildVariant, noBlockItem, defaultGroup, blockMaker);
    }

    public static DyeableBlockVariant of(Identifier variantIdentifier, Supplier<DyeableBlockVariant> possibleChildVariant, ItemGroup defaultGroup, BlockMaker blockMaker) {
        return new DyeableBlockVariant(variantIdentifier, possibleChildVariant, true, defaultGroup, blockMaker);
    }

    public static DyeableBlockVariant of(Identifier variantIdentifier, boolean noBlockItem, ItemGroup defaultGroup, BlockMaker blockMaker) {
        return new DyeableBlockVariant(variantIdentifier, null, noBlockItem, defaultGroup, blockMaker);
    }

    public static DyeableBlockVariant of(Identifier variantIdentifier, ItemGroup defaultGroup, BlockMaker blockMaker) {
        return new DyeableBlockVariant(variantIdentifier, null, true, defaultGroup, blockMaker);
    }

    //---------------------------------------------------------------------------------------------------

    /**
     * Sets the stack count of the for the {@link BlockItem} if such will be created
     *
     * @param maxCount
     */
    public DyeableBlockVariant stackCount(int maxCount) {
        this.defaultSettings.setItemStackCount(maxCount);

        return this;
    }

    /**
     * Sets the BlockItem will be a Fire Proof Item
     */
    public DyeableBlockVariant fireproof() {
        this.defaultSettings.fireproof = true;

        return this;
    }

    /**
     * Adds a {@link FoodComponent} to the {@link BlockItem} if such will be created
     *
     * @param foodComponent The FoodComponent being added to the BlockItem
     */
    public DyeableBlockVariant setFoodComponent(FoodComponent foodComponent) {
        this.defaultSettings.foodComponent = foodComponent;

        return this;
    }

    /**
     * Manually change the {@link #defaultBlock} Identifier
     *
     * @param identifier The identifier of the block
     */
    public final DyeableBlockVariant setDefaultBlock(Identifier identifier) {
        this.defaultBlock = identifier;
        this.addCustomDefaultBlockToTag = true;

        return this;
    }

    /**
     * Manually change the {@link #defaultBlock} Identifier by combining the Block's path and the variant's MODID
     *
     * @param path The Block's default path
     */
    public final DyeableBlockVariant setDefaultBlock(String path) {
        return this.setDefaultBlock(new Identifier(variantIdentifier.getNamespace(), path));
    }

    /**
     * Manually change the {@link #blockItemMaker} if a custom one is needed
     *
     * @param blockItemMaker Custom BlockItemMaker
     */
    public final DyeableBlockVariant setBlockItemMaker(BlockItemMaker blockItemMaker) {
        this.blockItemMaker = blockItemMaker;

        return this;
    }

    /**
     * Add all tags needed for this Block to be added too.
     * You will need at least one Tag which this block variant is linked too or the {@link #addToBlockTags} will throw a {@link NullPointerException}
     *
     * @param tags Tags to be added to when the block is built
     */
    @SafeVarargs
    public final DyeableBlockVariant setBlockTags(TagKey<Block>... tags) {
        secondaryBlockTags.addAll(Arrays.asList(tags));

        return this;
    }

    /**
     * Add all tags needed for the Created {@link BlockItem} if such is made
     *
     * @param tags Tags to be added to when the {@link BlockItem} is built
     */
    @SafeVarargs
    public final DyeableBlockVariant setItemTags(TagKey<Item>... tags) {
        secondaryItemTags.addAll(Arrays.asList(tags));

        return this;
    }

    /**
     * Method must be called when the Variant is finished being edited
     * Will add your variant to the {@link #ADDITION_BLOCK_VARIANTS} and
     * retroactively add this {@link DyeableBlockVariant} and {@link DyedVariantContainer#updateExistingContainers}
     */
    public final DyeableBlockVariant register() {
        if (!DyeableBlockVariant.ADDITION_BLOCK_VARIANTS.contains(this)) {
            DyedVariantContainer.updateExistingContainers(this);
        }

        ColorBlockRegistry.registerBlockTypeWithRecursion(this);
        DyeableBlockVariant.ADDITION_BLOCK_VARIANTS.add(this);

        return this;
    }

    //---------------------------------------------------------------------------------------------------

    public final TagKey<Block> getPrimaryBlockTag() {
        return this.primaryBlockTag;
    }

    public final TagKey<Item> getPrimaryItemTag() {
        return this.primaryItemTag;
    }

    public final TagKey<Block> getCommonBlockTag() {
        return TagKey.of(Registry.BLOCK_KEY, new Identifier("c", primaryBlockTag.id().getPath()));
    }

    public final TagKey<Item> getCommonItemTag() {
        return TagKey.of(Registry.ITEM_KEY, new Identifier("c", primaryItemTag.id().getPath()));
    }

    public Block getBlockVariant(DyeColorant dyeColorant) {
        String nameSpace = this.variantIdentifier.getNamespace();

        if(!dyeColorant.isIn(JelloTags.DyeColor.VANILLA_DYES)) {
            if (Objects.equals(nameSpace, "minecraft")) {
                nameSpace = dyeColorant.getId().getNamespace();
            }
        }

        return Registry.BLOCK.get(new Identifier(nameSpace, getBlockVariantPath(dyeColorant)));
    }

    public Block getDefaultBlockVariant() {
        return Registry.BLOCK.get(this.defaultBlock);
    }

    public String getBlockVariantPath(DyeColorant dyeColorant) {
        return dyeColorant.getName() + "_" + this.variantIdentifier.getPath();
    }

    public boolean isIdentifierAVariant(Block block, boolean isItem) {
        Identifier identifier = Registry.BLOCK.getId(block);

        return this.isIdentifierAVariant(identifier.getPath(), isItem);
    }

    public boolean isIdentifierAVariant(BlockItem blockItem) {
        Identifier identifier = Registry.ITEM.getId(blockItem);

        return this.isIdentifierAVariant(identifier.getPath(), true);
    }

    public boolean isIdentifierAVariant(Identifier identifier, boolean isItem) {
        return this.isIdentifierAVariant(identifier.getPath(), isItem);
    }

    private boolean isIdentifierAVariant(String blockPath, boolean isItem) {
        if (isItem && !this.createBlockItem) {
            return false;
        }

        if(Objects.equals(blockPath, defaultBlock.getPath())){
            return true;
        }

        String[] pathParts = blockPath.split("_");

        if (pathParts.length <= wordCount) {
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = pathParts.length - wordCount; i < pathParts.length; i++) {
            stringBuilder.append(pathParts[i]);

            if (i < pathParts.length - 1) {
                stringBuilder.append("_");
            }
        }

        return stringBuilder.toString().equals(this.variantIdentifier.getPath());
    }

    public DyeColorant getDyeColorantFromBlockVariant(BlockItem blockItem){
        Identifier identifier = Registry.ITEM.getId(blockItem);

        String[] pathParts = identifier.getPath().split("_");

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < pathParts.length - wordCount; i++) {
            stringBuilder.append(pathParts[i]);

            if (i < pathParts.length - wordCount - 1) {
                stringBuilder.append("_");
            }
        }

        return DyeColorantRegistry.DYE_COLOR.get(new Identifier(identifier.getNamespace(), stringBuilder.toString()));
    }

    @Nullable
    public static DyeableBlockVariant getVariantFromBlock(BlockItem blockItem){
        String blockPath = Registry.ITEM.getId(blockItem).getPath();
        return getVariantFromBlock(blockPath);
    }

    @Nullable
    public static DyeableBlockVariant getVariantFromBlock(Block block){
        String blockPath = Registry.BLOCK.getId(block).getPath();
        return getVariantFromBlock(blockPath);
    }

    @Nullable
    private static DyeableBlockVariant getVariantFromBlock(String blockPath){
        for(DyeableBlockVariant variant : getAllVariants()){
            if(variant.isIdentifierAVariant(blockPath, false)){
                return variant;
            }
        }

        return null;
    }

    public static Set<DyeableBlockVariant> getAllVariants(){
        if(ALL_BLOCK_VARIANTS.isEmpty()){
            ALL_BLOCK_VARIANTS.addAll(VanillaBlockVariants.VANILLA_VARIANTS);
            ALL_BLOCK_VARIANTS.addAll(ADDITION_BLOCK_VARIANTS);
        }

        return ALL_BLOCK_VARIANTS;
    }

    //---------------------------------------------------------------------------------------------------

    @ApiStatus.Internal
    protected static DyeableBlockVariant of(Identifier variantIdentifier, Supplier<DyeableBlockVariant> possibleChildVariant, BlockMaker blockMaker) {
        return new DyeableBlockVariant(variantIdentifier, possibleChildVariant, true, null, blockMaker);
    }

    @ApiStatus.Internal
    protected static DyeableBlockVariant of(Identifier variantIdentifier, boolean noBlockItem, BlockMaker blockMaker) {
        return new DyeableBlockVariant(variantIdentifier, null, noBlockItem, null, blockMaker);
    }

    @ApiStatus.Internal
    protected static DyeableBlockVariant of(Identifier variantIdentifier, BlockMaker blockMaker) {
        return new DyeableBlockVariant(variantIdentifier, null, true, null, blockMaker);
    }

    @ApiStatus.Internal
    protected final void addToItemTags(Item item, boolean readOnly) {
        if(item == Blocks.AIR.asItem()){
            return;
        }

        TagInjector.injectItems(JelloTags.Items.ALL_COLORED_VARIANTS.id(), item);
        TagInjector.injectItems(primaryItemTag.id(), item);

        if(!readOnly) {
            for (TagKey<Item> tagKey : secondaryItemTags) {
                TagInjector.injectItems(tagKey.id(), item);
            }
        }

        if(addCustomDefaultBlockToTag && item != this.getDefaultBlockVariant().asItem()){
            this.addToItemTags(this.getDefaultBlockVariant().asItem(), true);
        }
    }

    @ApiStatus.Internal
    protected final void addToBlockTags(Block block) {
        this.addToBlockTags(block, false);
    }

    @ApiStatus.Internal
    protected final void addToBlockTags(Block block, boolean readOnly) {
        TagInjector.injectBlocks(primaryBlockTag.id(), block);

        if(!readOnly) {
            for (TagKey<Block> tagKey : secondaryBlockTags) {
                TagInjector.injectBlocks(tagKey.id(), block);
            }
        }

        if(addCustomDefaultBlockToTag && block != this.getDefaultBlockVariant()) {
            this.addToBlockTags(this.getDefaultBlockVariant(), true);
        }
    }

    @ApiStatus.Internal
    protected RegistryInfo makeBlock(DyeColorant dyeColorant) {
        return this.makeChildBlock(dyeColorant, null);
    }

    @ApiStatus.Internal
    protected RegistryInfo makeChildBlock(DyeColorant dyeColorant, @Nullable Block parentBlock) {
        Block returnBlock = blockMaker.createBlockFromDyeColor(dyeColorant, parentBlock);

        if (!createBlockItem) {
            return RegistryInfo.of(returnBlock, null);
        } else {
            return RegistryInfo.of(returnBlock, defaultSettings);
        }
    }

    @ApiStatus.Internal
    protected BlockItem makeBlockItem(DyeColorant dyeColorant, Block block, Item.Settings settings) {
        return this.blockItemMaker.createBlockItemFromDyeColor(dyeColorant, block, settings);
    }

    public interface BlockMaker {
        Block createBlockFromDyeColor(DyeColorant dyeColorant, @Nullable Block parentBlock);
    }

    public interface BlockItemMaker {
        BlockItemMaker DEFAULT = (dyeColorant, block, settings) -> new ColoredBlockItem(block, settings);

        BlockItem createBlockItemFromDyeColor(DyeColorant dyeColorant, Block block, Item.Settings settings);
    }

    //---------------------------------------------------------------------------------------------------

    private static class BlockItemSettings {
        public int maxCount;
        public boolean fireproof;
        @Nullable public FoodComponent foodComponent;
        public ItemGroup group;

        private BlockItemSettings(int maxCount, boolean fireproof, FoodComponent foodComponent, ItemGroup group) {
            this.maxCount = maxCount;
            this.fireproof = fireproof;
            this.foodComponent = foodComponent;
            this.group = group;
        }

        private static BlockItemSettings of() {
            return new BlockItemSettings(64, false, null, null);
        }

        private static BlockItemSettings of(ItemGroup group) {
            return new BlockItemSettings(64, false, null, group);
        }

        private void setItemStackCount(int count) {
            this.maxCount = count;
        }
    }

    //---------------------------------------------------------------------------------------------------

    protected static class RegistryInfo {
        public final Block block;
        public final boolean noBlockItem;
        private final BlockItemSettings settings;

        protected OwoItemSettings overrideSettings = null;

        private RegistryInfo(Block block, boolean noBlockItem, BlockItemSettings blockItemSettings) {
            this.block = block;
            this.noBlockItem = noBlockItem;
            this.settings = blockItemSettings;
        }

        protected boolean noBlockItem() {
            return this.noBlockItem;
        }

        private static RegistryInfo of(Block block, @Nullable BlockItemSettings blockItemSettings) {
            if (blockItemSettings == null) {
                return new RegistryInfo(block, true, null);
            }

            return new RegistryInfo(block, false, blockItemSettings);
        }

        protected void setOverrideSettings(OwoItemSettings owoItemSettings) {
            this.overrideSettings = owoItemSettings;
        }

        protected Item.Settings getItemSettings() {
            Item.Settings settings = overrideSettings == null ? new Item.Settings() : JelloItemSettings.copyFrom(this.overrideSettings);

            settings.maxCount(this.settings.maxCount);

            if (this.settings.group != null) {
                settings.group(this.settings.group);
            }

            if (this.settings.foodComponent != null) {
                settings.food(this.settings.foodComponent);
            }

            if (this.settings.fireproof) {
                settings.fireproof();
            }

            return settings;
        }
    }
}