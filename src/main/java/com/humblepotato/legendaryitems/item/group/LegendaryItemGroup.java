package com.humblepotato.legendaryitems.item.group;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.humblepotato.legendaryitems.LegendaryItemsMod;
import com.humblepotato.legendaryitems.item.LegendaryItems;
import com.humblepotato.legendaryitems.block.LegendaryBlocks;

public class LegendaryItemGroup {

	public static void register() {
		Registry.register(
			Registries.ITEM_GROUP,
			new Identifier(LegendaryItemsMod.MOD_ID, "legendary"),
			ItemGroup.create(ItemGroup.Row.TOP, 0)
				.displayName(Text.literal("\u00A76Legendary Items"))
				.icon(() -> new ItemStack(LegendaryItems.FIERY_DAGGER))
				.entries((context, entries) -> {
					entries.add(LegendaryItems.FIERY_DAGGER);
					entries.add(LegendaryItems.VOID_SWORD);
					entries.add(LegendaryItems.VAMPIRE_SWORD);
					entries.add(LegendaryBlocks.LEGENDARY_PEDESTAL);
				})
				.build()
		);
	}
}
