package deboni.potatologistics.blocks;

import deboni.potatologistics.Util;
import deboni.potatologistics.PotatoLogisticsMod;
import deboni.potatologistics.blocks.entities.TileEntityChute;
import net.minecraft.core.block.BlockTileEntity;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.enums.EnumDropCause;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;

public class BlockChute extends BlockTileEntity {

    public BlockChute(String blockName, int id, Material material) {
        super(blockName, id, material);
    }
    @Override
    protected TileEntity getNewBlockEntity() {
        return new TileEntityChute();
    }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityplayer) {
        TileEntityChute te = (TileEntityChute)Util.getBlockTileEntity(world, x, y, z);
        if (te.getNumUnitsInside() > 0) {
            te.givePlayerAllItems(world, entityplayer);
            return true;
        }
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }


    public int getFillLevel(World world, int x, int y, int z) {
        TileEntityChute te = (TileEntityChute)Util.getBlockTileEntity(world, x, y, z);
        float fill = (float)te.getNumUnitsInside() / (float)te.getMaxUnits();
        return (int)Math.ceil(10.0f * fill);
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public void onBlockRemoved(World world, int x, int y, int z, int data) {
        TileEntityChute te = (TileEntityChute)world.getBlockTileEntity(x, y, z);
        world.removeBlockTileEntity(x, y, z);
        if (world.isClientSide) {
            return;
        }
        te.dropAllItems();
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public boolean isIndirectlyPoweringTo(World world, int x, int y, int z, int side) {
        return this.isPoweringTo(world, x, y, z, side);
    }

    @Override
    public boolean isPoweringTo(WorldSource blockAccess, int x, int y, int z, int side) {
        TileEntityChute basketTileEntity = (TileEntityChute) blockAccess.getBlockTileEntity(x, y, z);
        if (basketTileEntity != null) {
            return basketTileEntity.getNumUnitsInside() == basketTileEntity.getMaxUnits();
        }
        return false;
    }

    /*
    @Override
    public ItemStack[] getBreakResult(World world, EnumDropCause dropCause, int x, int y, int z, int meta, TileEntity tileEntity) {
        return new ItemStack[]{new ItemStack(PotatoLogisticsMod.itemChute)};
    }
     */
}
