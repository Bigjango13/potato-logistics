package deboni.potatologistics;

import deboni.potatologistics.blocks.BlockAutoBasket;
import deboni.potatologistics.blocks.BlockFilter;
import deboni.potatologistics.blocks.BlockPipe;
import deboni.potatologistics.blocks.BlockPotato;
import deboni.potatologistics.blocks.entities.TileEntityAutoBascket;
import deboni.potatologistics.blocks.entities.TileEntityFilter;
import deboni.potatologistics.blocks.entities.TileEntityPipe;
import deboni.potatologistics.items.Potato;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemPlaceable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.helper.*;

public class PotatoLogisticsMod implements ModInitializer {
    public static final String MOD_ID = "potatologistics";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Item itemPotato;
    public static Item itemWrench;

    public static Item itemAutoBasket;
    public static Block blockPotato;
    public static Block blockPipe;
    public static Block blockDirectionalPipe;
    public static Block blockFilter;
    public static Block blockAutoBasket;

    @Override
    public void onInitialize() {
        LOGGER.info("PotatoLogistics initialized.");

        int blockNum = 999 + 1000;
        //potatoBlock = BlockHelper.createBlock(MOD_ID, new Block("crop.potato", blockNum++, Material.plant), "potato.png", "potato.png", null, 0.0f, 0.0f, 0.0f);
        blockPotato = new BlockBuilder(MOD_ID)
                .setTextures("potato.png")
                .build(new BlockPotato("block.potato", blockNum++, Material.wood));
        blockPipe = new BlockBuilder(MOD_ID)
                .setTextures("pipe.png")
                .setLightOpacity(0)
                .build(new BlockPipe("pipe", blockNum++, Material.glass, false));
        blockDirectionalPipe = new BlockBuilder(MOD_ID)
                .setTextures("directional_pipe.png")
                .setLightOpacity(0)
                .build(new BlockPipe("directional_pipe", blockNum++, Material.glass, true));

        blockFilter = new BlockBuilder(MOD_ID)
                .setTextures("block_filter.png")
                .setLightOpacity(0)
                .build(new BlockFilter("block.filter", blockNum++, Material.wood));

        blockAutoBasket = new BlockBuilder(MOD_ID)
                .setTopTexture(4, 9)
                .setBottomTexture("auto_basket_bottom.png")
                .setSides("auto_basket_sides.png")
                .setLightOpacity(0)
                .build(new BlockAutoBasket("block.auto_basket", blockNum++, Material.cloth));

        int itemNum = 16999 + 1000;
        itemPotato = ItemHelper.createItem(MOD_ID, new Potato("Potato", itemNum++, 5, true), "potato", "potato.png");
        itemWrench = ItemHelper.createItem(MOD_ID, new Item("Wrench", itemNum++), "wrench", "wrench.png");
        itemWrench.setMaxStackSize(1);
        itemAutoBasket = ItemHelper.createItem(MOD_ID, new ItemPlaceable("Auto Basket", itemNum++, blockAutoBasket), "auto_basket", "auto_basket.png");

        EntityHelper.createSpecialTileEntity(TileEntityPipe.class, new TileEntityRendererPipe(), "pipe.tile");
        EntityHelper.createTileEntity(TileEntityFilter.class, "filter.tile");
        EntityHelper.createTileEntity(TileEntityAutoBascket.class, "auto_basket.tile");

        RecipeHelper.Crafting.createShapelessRecipe(itemPotato, 1, new Object[]{Item.clay, Item.dustSugar, Item.dustGlowstone});
        RecipeHelper.Crafting.createShapelessRecipe(itemPotato, 9, new Object[]{blockPotato});
        RecipeHelper.Crafting.createRecipe(blockPotato, 1, new Object[]{"AAA", "AAA", "AAA", 'A', itemPotato});

        RecipeHelper.Crafting.createRecipe(blockPipe, 16, new Object[]{"   ", "ABA", "   ", 'A', Item.ingotIron, 'B', Block.glass});
        RecipeHelper.Crafting.createRecipe(blockDirectionalPipe, 16, new Object[]{"   ", "ABC", "   ", 'A', Item.ingotIron, 'B', Block.glass, 'C', Item.ingotGold});

        RecipeHelper.Crafting.createRecipe(itemWrench, 1, new Object[]{" A ", "AA ", "  A", 'A', Item.ingotIron, 'B', Block.glass});

        RecipeHelper.Crafting.createRecipe(blockFilter, 1, new Object[]{"ABA", "BCB", "ABA", 'A', Block.planksOak, 'B', Item.dustRedstone, 'C', Block.mesh});
        RecipeHelper.Crafting.createRecipe(blockAutoBasket, 1, new Object[]{"AAA", "CBC", "CCC", 'A', Item.leather, 'B', Item.dustRedstone, 'C', Item.wheat});
    }
}