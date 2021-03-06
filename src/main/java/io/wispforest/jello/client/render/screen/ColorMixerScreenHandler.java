package io.wispforest.jello.client.render.screen;

import io.wispforest.jello.api.dye.registry.variants.VanillaItemVariants;
import io.wispforest.jello.item.JelloDyeItem;
import io.wispforest.jello.api.dye.registry.variants.DyeableVariantManager;
import io.wispforest.jello.api.util.ColorUtil;
import io.wispforest.jello.Jello;
import io.wispforest.jello.block.JelloBlocks;
import io.wispforest.jello.item.ArtistPaletteItem;
import io.wispforest.jello.item.JelloItems;
import io.wispforest.jello.network.ColorMixerScrollPacket;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.ValidatingSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.Locale;

public class ColorMixerScreenHandler extends ScreenHandler {

    public static final List<ItemStack> ALL_DYE_ITEMS;

    static{
        if(Jello.getConfig().addCustomJsonColors){
            ALL_DYE_ITEMS = DyeableVariantManager.getVariantMap().values().stream()
                    .filter(dyedVariantContainer -> dyedVariantContainer.dyeItem() instanceof JelloDyeItem)
                    .map(dyedVariantContainer -> dyedVariantContainer.dyeItem().getDefaultStack())
                    .sorted(ColorUtil.dyeStackHslComparator(2))
                    .sorted(ColorUtil.dyeStackHslComparator(1))
                    .sorted(ColorUtil.dyeStackHslComparator(0)).toList();
        }else{
            ALL_DYE_ITEMS = DyeableVariantManager.getVariantMap().values().stream()
                    .map(dyedVariantContainer -> dyedVariantContainer.dyeItem().getDefaultStack())
                    .sorted(ColorUtil.dyeStackHslComparator(2))
                    .sorted(ColorUtil.dyeStackHslComparator(1))
                    .sorted(ColorUtil.dyeStackHslComparator(0)).toList();
        }
    }


    public final SimpleInventory dyeInventory;
    public final SimpleInventory artistInventory;

    private final ScreenHandlerContext context;

    private String lastSearchTerm = "";
    private boolean hasPalette = false;

    public final DefaultedList<ItemStack> itemList = DefaultedList.of();

    public ColorMixerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ColorMixerScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(JelloScreenHandlerTypes.COLOR_MIXER_TYPE, syncId);

        this.dyeInventory = new SimpleInventory(30);
        this.artistInventory = new SimpleInventory(1);

        this.artistInventory.addListener(sender -> {
            boolean hadPalette = this.hasPalette;
            this.hasPalette = sender.getStack(0).isOf(JelloItems.ARTIST_PALETTE);
            if (hadPalette != this.hasPalette) this.reFilter();
        });

        this.context = context;

        this.addSlot(new ValidatingSlot(artistInventory, 0, 14, 96,
                itemStack -> itemStack.isOf(JelloItems.ARTIST_PALETTE)
                        && itemStack.getOrCreateNbt().contains("PaletteOrder", NbtElement.LIST_TYPE)));

        for (int row = 0; row < 5; ++row) {
            for (int col = 0; col < 6; ++col) {
                this.addSlot(new DyeOutputSlot(this.dyeInventory, col + row * 6, 44 + col * 18, 24 + row * 18));
            }
        }

        ScreenUtils.generatePlayerSlots(8, 122, playerInventory, this::addSlot);
        this.reFilter();
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, pos) -> {
            this.dropInventory(player, this.artistInventory);
        });
    }

    public void scrollItems(float position) {
        if (this.context == ScreenHandlerContext.EMPTY) {
            Jello.CHANNEL.clientHandle().send(new ColorMixerScrollPacket(position));
            return;
        }

        int lineCount = Math.max(1, (int) Math.ceil(this.itemList.size() / 6f) - 5);

        int currentLine = (int) Math.floor(lineCount * position);
        int baseIndex = currentLine * 6;

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 6; x++) {
                int index = y * 6 + x;
                if (baseIndex + index <= this.itemList.size() - 1) {
                    this.dyeInventory.setStack(index, this.itemList.get(baseIndex + index));
                } else {
                    this.dyeInventory.setStack(index, ItemStack.EMPTY);
                }
            }
        }
    }

    public void search(String text) {
        this.lastSearchTerm = text.toLowerCase(Locale.ROOT);
        this.reFilter();
    }

    public void reFilter() {
        this.itemList.clear();

        if (hasPalette) {
            if (this.lastSearchTerm.isBlank()) {
                this.itemList.addAll(ColorMixerScreenHandler.ALL_DYE_ITEMS);
            } else {
                for (ItemStack itemStack : ColorMixerScreenHandler.ALL_DYE_ITEMS) {
                    String itemName = itemStack.getItem().getRegistryEntry().getKey().get().getValue()
                            .getPath().replace("_", " ");

                    if (itemName.contains(this.lastSearchTerm)) {
                        this.itemList.add(itemStack.copy());
                    }
                }
            }
        }

        this.scrollItems(0.0F);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, JelloBlocks.PAINT_MIXER);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        return ScreenUtils.handleSlotTransfer(this, invSlot, 1);
    }

    private class DyeOutputSlot extends ValidatingSlot {

        public DyeOutputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y, itemStack -> false);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return ColorMixerScreenHandler.this.hasPalette;
        }

        @Override
        public ItemStack takeStack(int amount) {
            ColorMixerScreenHandler.this.artistInventory.setStack(0, ArtistPaletteItem.use(artistInventory.getStack(0)));
            return this.inventory.getStack(this.getIndex()).copy();
        }
    }
}
