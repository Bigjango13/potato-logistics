package deboni.potatologistics.blocks.entities;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import deboni.potatologistics.Util;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockChest;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.entity.TileEntityChest;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.packet.Packet;
import net.minecraft.core.net.packet.Packet140TileEntityData;
import net.minecraft.core.player.inventory.IInventory;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.world.World;

import java.util.*;

public class TileEntityChute extends TileEntity {

    public int numUnitsInside = 0;
    public final Map<ChuteEntry, Integer> contents = new HashMap<>();

    public void dropAllItems() {
        Random rand = new Random();
        for (Map.Entry<ChuteEntry, Integer> entry : this.contents.entrySet()) {
            int stackSize;
            ChuteEntry be = entry.getKey();
            for (int numItems = entry.getValue(); numItems > 0; numItems -= stackSize) {
                int maxStackSize;
                stackSize = maxStackSize = be.getItem().getItemStackLimit();
                int remainingItems = numItems - maxStackSize;
                if (remainingItems < 0) {
                    stackSize = numItems;
                }
                this.dropItemStack(rand, new ItemStack(be.id, stackSize, be.metadata, be.tag));
            }
        }
        this.contents.clear();
        this.worldObj.notifyBlockChange(this.x, this.y, this.z, Block.basket.id);
        this.updateNumUnits();
    }

    private void updateNumUnits() {
        this.numUnitsInside = 0;
        for (Map.Entry<ChuteEntry, Integer> entry : this.contents.entrySet()) {
            ChuteEntry be = entry.getKey();
            int numItems = entry.getValue();
            int unitsPerItem = this.getItemSizeUnits(be.getItem());
            this.numUnitsInside += unitsPerItem * numItems;
        }
    }

    private int getItemSizeUnits(Item item) {
        return 64 / item.getItemStackLimit();
    }

    private void dropItemStack(Random rand, ItemStack itemstack) {
        float f = rand.nextFloat() * 0.8f + 0.1f;
        float f1 = rand.nextFloat() * 0.8f + 0.1f;
        float f2 = rand.nextFloat() * 0.8f + 0.1f;
        EntityItem entityitem = new EntityItem(this.worldObj, (float)this.x + f, (float)this.y + f1, (float)this.z + f2, itemstack);
        float f3 = 0.05f;
        entityitem.xd = (float)rand.nextGaussian() * 0.05f;
        entityitem.yd = (float)rand.nextGaussian() * 0.05f + 0.25f;
        entityitem.zd = (float)rand.nextGaussian() * 0.05f;
        this.worldObj.entityJoinedWorld(entityitem);
    }

    public int getNumUnitsInside() {
        return this.numUnitsInside;
    }

    public int getMaxUnits() {
        return 1728;
    }

    public void givePlayerAllItems(World world, EntityPlayer player) {
        List<ChuteEntry> toRemove = new ArrayList<>();
        Iterator var4 = this.contents.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry entry = (Map.Entry)var4.next();
            ChuteEntry basketEntry = (ChuteEntry)entry.getKey();
            ItemStack basketEntryStack = new ItemStack(basketEntry.id, (Integer)entry.getValue(), basketEntry.metadata, basketEntry.tag);
            player.inventory.insertItem(basketEntryStack, true);
            this.contents.put(basketEntry, basketEntryStack.stackSize);
            if (basketEntryStack.stackSize <= 0) {
                toRemove.add(basketEntry);
            }
        }

        var4 = toRemove.iterator();

        while(var4.hasNext()) {
            ChuteEntry entry = (ChuteEntry)var4.next();
            this.contents.remove(entry);
        }

        this.updateNumUnits();
        this.worldObj.notifyBlockChange(this.x, this.y, this.z, Block.basket.id);
    }
    public ItemStack removeOneItem() {
        ChuteEntry firstKey = null;
        int itemCount = 0;
        for (Map.Entry<ChuteEntry, Integer> entry : this.contents.entrySet()) {
            firstKey = entry.getKey();
            itemCount = entry.getValue();
            break;
        }

        if (firstKey == null || itemCount == 0) return null;

        ItemStack itemStack = new ItemStack(firstKey.getItem(), 1, firstKey.metadata);

        itemCount--;

        if (itemCount == 0) {
            this.contents.remove(firstKey);
        } else {
            this.contents.put(firstKey, itemCount);
        }

        this.worldObj.notifyBlockChange(this.x, this.y, this.z, Block.basket.id);
        this.updateNumUnits();

        return itemStack;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        ListTag itemsTag = tag.getList("Items");
        this.contents.clear();
        for (int i = 0; i < itemsTag.tagCount(); ++i) {
            CompoundTag itemTag = (CompoundTag)itemsTag.tagAt(i);
            ChuteEntry entry = ChuteEntry.read(itemTag);
            short count = itemTag.getShort("Count");
            this.contents.put(entry, (int) count);
        }
        this.updateNumUnits();
    }

    @Override
    public void tick() {
        if (this.worldObj == null || this.worldObj.isClientSide) {
            return;
        }
        AABB aabb = AABB.getBoundingBoxFromPool(this.x, this.y, this.z, this.x + 1, this.y + 2, this.z + 1);
        List<Entity> entities = this.worldObj.getEntitiesWithinAABB(EntityItem.class, aabb);
        boolean shouldUpdate = false;
        if (!entities.isEmpty()) {
            for (Entity e : entities) {
                EntityItem entity = (EntityItem)e;
                if (entity.item == null || entity.item.stackSize <= 0 || entity.delayBeforeCanPickup != 0) continue;
                shouldUpdate = this.importItemStack(entity.item);
                if (entity.item.stackSize > 0) continue;
                entity.item.stackSize = 0;
                e.outOfWorld();
            }
        }
        if (shouldUpdate) {
            this.worldObj.notifyBlockChange(this.x, this.y, this.z, Block.basket.id);
            this.updateNumUnits();
        }

        TileEntity outTe = Util.getBlockTileEntity(worldObj, x, y-1, z) ;
        if (outTe instanceof IInventory) {
            ItemStack itemToRemove = this.removeOneItem();

            if (itemToRemove != null) {
                boolean hasInserted = false;

                IInventory inventory;
                if (outTe instanceof TileEntityChest) {
                    inventory = BlockChest.getInventory(worldObj, x, y - 1, z);
                } else {
                    inventory = (IInventory) outTe;
                }

                if (inventory != null) {
                    hasInserted = Util.insertOnInventory(inventory, itemToRemove, Direction.DOWN, new TileEntityPipe[0]);
                }
                if (!hasInserted) {
                    importItemStack(itemToRemove);
                }
            }
        } else if (outTe instanceof TileEntityChute) {
            ItemStack itemToRemove = this.removeOneItem();
            if (itemToRemove != null) {
                if (!((TileEntityChute)outTe).importItemStack(itemToRemove)) {
                    this.importItemStack(itemToRemove);
                }
            }
        }
    }

    public boolean importItemStack(ItemStack stack) {
        ChuteEntry entry = new ChuteEntry(stack.itemID, stack.getMetadata(), stack.getData());
        int sizeUnits = this.getItemSizeUnits(stack.getItem());
        int freeUnits = this.getMaxUnits() - this.numUnitsInside;
        int itemsToTake = Math.min(freeUnits / sizeUnits, stack.stackSize);
        if (itemsToTake <= 0) {
            return false;
        }
        stack.stackSize -= itemsToTake;
        int currentItemsInBE = this.contents.getOrDefault(entry, 0);
        this.contents.put(entry, currentItemsInBE += itemsToTake);
        return true;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        ListTag itemsTag = new ListTag();
        for (Map.Entry<ChuteEntry, Integer> entry : this.contents.entrySet()) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putShort("Count", (short)entry.getValue().intValue());
            ChuteEntry.write(itemTag, entry.getKey());
            itemsTag.addTag(itemTag);
        }
        tag.put("Items", itemsTag);
    }

    @Override
    public Packet getDescriptionPacket() {
        return new Packet140TileEntityData(this);
    }

    public static final class ChuteEntry {
        public final int id;
        public final int metadata;
        public final CompoundTag tag;

        public ChuteEntry(int id, int metadata, CompoundTag tag) {
            this.id = id;
            this.metadata = metadata;
            this.tag = tag;
        }

        public static ChuteEntry read(CompoundTag tag) {
            short id = tag.getShort("id");
            short damage = tag.getShort("Damage");
            CompoundTag data = tag.getCompound("Data");
            return new ChuteEntry(id, damage, data);
        }

        public static void write(CompoundTag tag, ChuteEntry entry) {
            tag.putShort("id", (short)entry.id);
            tag.putShort("Damage", (short)entry.metadata);
            tag.putCompound("Data", entry.tag);
        }

        public Item getItem() {
            return Item.itemsList[this.id];
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ChuteEntry)) {
                return false;
            }
            ChuteEntry other = (ChuteEntry)obj;
            if (this.id != other.id || this.metadata != other.metadata) {
                return false;
            }
            return this.tag.getValues().size() <= 2 && other.tag.getValues().size() <= 2;
        }

        public int hashCode() {
            if (this.tag.getValues().size() <= 2) {
                return Objects.hash(this.id, this.metadata);
            }
            return Objects.hash(this.id, this.metadata, this.tag);
        }
    }
}
