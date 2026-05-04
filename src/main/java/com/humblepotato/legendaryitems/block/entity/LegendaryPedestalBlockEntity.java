package com.humblepotato.legendaryitems.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class LegendaryPedestalBlockEntity extends BlockEntity {
	private ItemStack legendaryItem = ItemStack.EMPTY;
	private DefaultedList<ItemStack> requiredItems = DefaultedList.of();

	public LegendaryPedestalBlockEntity(BlockPos pos, BlockState state) {
		super(LegendaryBlockEntities.LEGENDARY_PEDESTAL_ENTITY, pos, state);
	}

	public boolean hasItem() {
		return !legendaryItem.isEmpty();
	}

	public void setItem(ItemStack stack) {
		this.legendaryItem = stack.copy();
	}

	public ItemStack getItem() {
		return legendaryItem.copy();
	}

	public void removeItem() {
		this.legendaryItem = ItemStack.EMPTY;
	}

	public void setRequiredItems(DefaultedList<ItemStack> items) {
		this.requiredItems = items;
	}

	public boolean playerHasRequiredItems(PlayerEntity player) {
		for (ItemStack required : requiredItems) {
			if (required.isEmpty()) continue;
			if (!player.getInventory().contains(required)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		if (!legendaryItem.isEmpty()) {
			nbt.put("LegendaryItem", legendaryItem.writeNbt(new NbtCompound()));
		}
		NbtList requiredList = new NbtList();
		for (ItemStack item : requiredItems) {
			if (!item.isEmpty()) {
				requiredList.add(item.writeNbt(new NbtCompound()));
			}
		}
		nbt.put("RequiredItems", requiredList);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		if (nbt.contains("LegendaryItem")) {
			legendaryItem = ItemStack.fromNbt(nbt.getCompound("LegendaryItem"));
		}
		if (nbt.contains("RequiredItems")) {
			NbtList requiredList = nbt.getList("RequiredItems", 10);
			requiredItems.clear();
			for (int i = 0; i < requiredList.size(); i++) {
				requiredItems.add(ItemStack.fromNbt(requiredList.getCompound(i)));
			}
		}
	}
}