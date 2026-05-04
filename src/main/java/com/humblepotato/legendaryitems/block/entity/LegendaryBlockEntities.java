package com.humblepotato.legendaryitems.block.entity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import com.humblepotato.legendaryitems.LegendaryItemsMod;
import com.humblepotato.legendaryitems.block.LegendaryBlocks;

public class LegendaryBlockEntities {
	public static final BlockEntityType<LegendaryPedestalBlockEntity> LEGENDARY_PEDESTAL_ENTITY = Registry.register(
		Registries.BLOCK_ENTITY_TYPE,
		new Identifier(LegendaryItemsMod.MOD_ID, "legendary_pedestal"),
		BlockEntityType.Builder.create(LegendaryPedestalBlockEntity::new, LegendaryBlocks.LEGENDARY_PEDESTAL).build(null)
	);

	public static void register() {
		// Register is handled above
	}
}