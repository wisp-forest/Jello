package io.wispforest.jello.data.providers;

import io.wispforest.forge.LanguageProvider;
import io.wispforest.jello.Jello;
import io.wispforest.jello.api.dye.DyeColorant;
import io.wispforest.jello.api.dye.registry.DyeColorantRegistry;
import io.wispforest.jello.api.dye.registry.variants.block.DyeableBlockVariant;
import io.wispforest.jello.api.dye.registry.variants.DyeableVariantManager;
import io.wispforest.jello.block.JelloBlocks;
import io.wispforest.jello.item.JelloItems;
import io.wispforest.jello.item.SpongeItem;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JelloLangProvider extends LanguageProvider {

    public JelloLangProvider(DataGenerator gen) {
        super(gen, Jello.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {

        addBlock(() -> JelloBlocks.SLIME_SLAB);

        JelloItems.Slimeballs.SLIME_BALLS.forEach((item) -> {
            addItem(() -> item);
        });

        addItem(() -> JelloItems.JelloCups.SUGAR_CUP);

        JelloItems.JelloCups.JELLO_CUP.forEach((item) -> {
            addItem(() -> item);
        });

        addItem(() -> JelloItems.SPONGE);
        addItem(() -> JelloItems.DYE_BUNDLE);

        addItem(() -> JelloItems.ARTIST_PALETTE);
        addItem(() -> JelloItems.EMPTY_ARTIST_PALETTE, "Empty Palette");

        addBlock(() -> JelloBlocks.PAINT_MIXER);

        add(SpongeItem.DIRTINESS_TRANSLATION_KEY, "Dirty Sponge");

        addACToolTipAndNameEntry("enableGrayScalingOfEntities", "Enable GrayScaling of Entities", "[Warning: Will break texturepacks!] Used to allow for true color when a entity is dyed or color.");

        addACToolTipAndNameEntry("enableDyeingEntities", "Enable Dyeing of Entities", "Allow for the dyeing of entities using any dye.");
        addACToolTipAndNameEntry("enableDyeingPlayers", "Enable Dyeing of Players", "Allow for the dyeing of players using any dye.");
        addACToolTipAndNameEntry("enableDyeingBlocks", "Enable Dyeing of Blocks", "Allow for the dyeing of blocks using any vanilla dye.");

        addACToolTipAndNameEntry("addCustomJsonColors", "Enable Json Colors", "Whether or not Jello will add it's included 1822 colors to Minecraft internally.");
        addACToolTipAndNameEntry("enableTransparencyFixCauldrons", "Enable Transparency Fix for Cauldrons", "Enables a fix for water within cauldrons just being Opaque rather than translucent.");

        addACCategoryName("common", "Main Config");
        addACCategoryName("client", "Client Config");

        add("text.jello.dye_bundle_pattern", "%1$s [%2$s]");

        add("itemGroup.misc.tab.dyes", "Custom Dyes");
        add("itemGroup.misc.tab.block_vars", "Colored Block Variants");

        add("item.jello.sponge.desc", "Use on a block to remove dye");
        add("item.jello.sponge.desc.dirty", "Clean by using on water cauldron");

        add("vanilla_slime_slabs_condensed", "Slime Slabs");
        add("vanilla_slime_blocks_condensed", "Slime Blocks");

        add("tooltip.vanilla_slime_slabs_condensed", "Only contains Vanilla Colors");
        add("tooltip.vanilla_slime_blocks_condensed", "Only contains Vanilla Colors");

        add("itemGroup.jello.jello_group", "Jello");

        add("itemGroup.jello.jello_group.tab.jello_tools", "Jello Stuff");
        add("itemGroup.jello.jello_group.tab.dyed_item_variants", "Jello Item Variants");
        add("itemGroup.jello.jello_group.tab.dyed_block_variants", "Jello Block Variants");

        DyeableBlockVariant.getAllBlockVariants().stream().filter(dyeableBlockVariant -> !dyeableBlockVariant.alwaysReadOnly() && dyeableBlockVariant.createBlockItem()).forEach(dyeableBlockVariant -> {
            add(dyeableBlockVariant.variantIdentifier.getPath() + "_condensed", capitalizeEachWord(dyeableBlockVariant.variantIdentifier.getPath()) + "s");
        });

        for (DyeableVariantManager.DyeColorantVariantData dyedVariant : DyeableVariantManager.getVariantMap().values()) {
            for (Block block : dyedVariant.dyedBlocks().values()) {
                addBlock(() -> block);
            }

            addItem(dyedVariant::dyeItem);
        }
    }

    public static String capitalizeEachWord(String path){
        return  Arrays.stream(path.split("_")).map(WordUtils::capitalize).collect(Collectors.joining(" "));
    }

    //-----------------------------------------------//

    private void addItem(Supplier<? extends Item> item) {
        addItem(item, getAutomaticNameItem(item));
    }

    private void addBlock(Supplier<? extends Block> block) {
        addBlock(block, getAutomaticNameBlock(block));
    }

    private void addEntityType(Supplier<? extends EntityType<?>> entity) {
        addEntityType(entity, getAutomaticNameEntityType(entity));
    }

    public static String getAutomaticNameDyeColorant(Supplier<? extends DyeColorant> sup) {
        return toEnglishName(DyeColorantRegistry.DYE_COLOR.getId(sup.get()).getPath());
    }

    public static String getAutomaticNameItem(Supplier<? extends Item> sup) {
        return toEnglishName(Registry.ITEM.getId(sup.get()).getPath());
    }

    public static String getAutomaticNameBlock(Supplier<? extends Block> sup) {
        return toEnglishName(Registry.BLOCK.getId(sup.get()).getPath());
    }

    private static String getAutomaticNameEntityType(Supplier<? extends EntityType<?>> sup) {
        return toEnglishName(Registry.ENTITY_TYPE.getId(sup.get()).getPath());
    }

    public static final String toEnglishName(String internalName) {
        return Arrays.stream(internalName.toLowerCase(Locale.ROOT).split("_"))
                .map(WordUtils::capitalize)
                .collect(Collectors.joining(" "));
    }

    private void addACCategoryName(String keyName, String nameTranslation) {
        add("text.autoconfig.jello.category." + keyName, nameTranslation);
    }

    private void addACToolTipAndNameEntry(String keyName, String nameTranslation, String tooltipTranslation) {
        add("text.autoconfig.jello.option." + keyName + ".@Tooltip", tooltipTranslation);
        add("text.autoconfig.jello.option." + keyName, nameTranslation);
    }
}
