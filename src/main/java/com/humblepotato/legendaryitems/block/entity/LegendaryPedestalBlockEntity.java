package com.humblepotato.legendaryitems.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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

	// ── Legendary item ──────────────────────────────────────────────────────────

	public boolean hasItem() {
		return !legendaryItem.isEmpty();
	}

	public void setItem(ItemStack stack) {
		this.legendaryItem = stack.copy();
		markDirty();
	}

	public ItemStack getItem() {
		return legendaryItem.copy();
	}

	public void removeItem() {
		this.legendaryItem = ItemStack.EMPTY;
		markDirty();
	}

	// ── Required items ──────────────────────────────────────────────────────────

	public void setRequiredItems(DefaultedList<ItemStack> items) {
		this.requiredItems = items;
		markDirty();
	}

	/**
	 * Returns true only when the player's inventory contains every required stack
	 * (item type + count match).
	 */
	public boolean playerHasRequiredItems(PlayerEntity player) {
		PlayerInventory inv = player.getInventory();
		for (ItemStack required : requiredItems) {
			if (required.isEmpty()) continue;
			int found = 0;
			for (ItemStack slot : inv.main) {
				if (ItemStack.areItemsEqual(slot, required)) {
					found += slot.getCount();
				}
			}
			if (found < required.getCount()) return false;
		}
		return true;
	}

	/**
	 * Removes all required items from the player's inventory.
	 * Call this only after {@link #playerHasRequiredItems} returns true.
	 */
	public void consumeRequiredItems(PlayerEntity player) {
		PlayerInventory inv = player.getInventory();
		for (ItemStack required : requiredItems) {
			if (required.isEmpty()) continue;
			int toRemove = required.getCount();
			for (ItemStack slot : inv.main) {
				if (toRemove <= 0) break;
				if (ItemStack.areItemsEqual(slot, required)) {
					int take = Math.min(slot.getCount(), toRemove);
					slot.decrement(take);
					toRemove -= take;
				}
			}
		}
	}

	// ── NBT serialisation ───────────────────────────────────────────────────────

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
			NbtList list = nbt.getList("RequiredItems", NbtCompound.COMPOUND_TYPE);
			requiredItems = DefaultedList.of();
			for (int i = 0; i < list.size(); i++) {
				ItemStack stack = ItemStack.fromNbt(list.getCompound(i));
				if (!stack.isEmpty()) requiredItems.add(stack);
			}
		}
	}
}
