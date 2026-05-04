package com.humblepotato.legendaryitems.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import com.humblepotato.legendaryitems.LegendaryItemsMod;
import com.humblepotato.legendaryitems.block.custom.LegendaryPedestalBlock;

public class LegendaryBlocks {
	public static final Block LEGENDARY_PEDESTAL = new LegendaryPedestalBlock(
		Block.Settings.create()
			.strength(5.0f, 6.0f)
			.requiresTool()
	);

	public static void register() {
		Registry.register(Registries.BLOCK, new Identifier(LegendaryItemsMod.MOD_ID, "legendary_pedestal"), LEGENDARY_PEDESTAL);
		Registry.register(Registries.ITEM, new Identifier(LegendaryItemsMod.MOD_ID, "legendary_pedestal"), 
			new BlockItem(LEGENDARY_PEDESTAL, new Item.Settings()));
	}
}