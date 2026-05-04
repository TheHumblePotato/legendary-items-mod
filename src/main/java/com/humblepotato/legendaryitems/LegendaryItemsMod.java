package com.humblepotato.legendaryitems;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.humblepotato.legendaryitems.item.LegendaryItems;
import com.humblepotato.legendaryitems.block.LegendaryBlocks;
import com.humblepotato.legendaryitems.block.entity.LegendaryBlockEntities;
import com.humblepotato.legendaryitems.item.group.LegendaryItemGroup;

public class LegendaryItemsMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("legendary-items");
	public static final String MOD_ID = "legendary-items";

	@Override
	public void onInitialize() {
		LEGGER.info("Initializing Legendary Items Mod...");
		LegendaryItems.register();
		LegendaryBlocks.register();
		LegendaryBlockEntities.register();
		LegendaryItemGroup.register();
		LEGGER.info("Legendary Items Mod initialized successfully!");
	}
}