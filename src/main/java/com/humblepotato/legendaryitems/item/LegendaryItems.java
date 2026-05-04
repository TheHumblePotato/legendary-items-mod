package com.humblepotato.legendaryitems.item;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import com.humblepotato.legendaryitems.LegendaryItemsMod;
import com.humblepotato.legendaryitems.item.custom.FieryDagger;
import com.humblepotato.legendaryitems.item.custom.VoidSword;
import com.humblepotato.legendaryitems.item.custom.VampireSword;

public class LegendaryItems {
	public static final FieryDagger FIERY_DAGGER = new FieryDagger(
		ToolMaterials.NETHERITE, 
		3, // attack damage (less than netherite's 8)
		-1.4f, // attack speed (more attack speed = less negative)
		new Item.Settings()
	);

	public static final VoidSword VOID_SWORD = new VoidSword(
		ToolMaterials.NETHERITE, 
		5, // attack damage
		-1.6f, // attack speed
		new Item.Settings()
	);

	public static final VampireSword VAMPIRE_SWORD = new VampireSword(
		ToolMaterials.NETHERITE, 
		4, // attack damage
		-1.8f, // attack speed
		new Item.Settings()
	);

	public static void register() {
		Registry.register(Registries.ITEM, new Identifier(LegendaryItemsMod.MOD_ID, "fiery_dagger"), FIERY_DAGGER);
		Registry.register(Registries.ITEM, new Identifier(LegendaryItemsMod.MOD_ID, "void_sword"), VOID_SWORD);
		Registry.register(Registries.ITEM, new Identifier(LegendaryItemsMod.MOD_ID, "vampire_sword"), VAMPIRE_SWORD);
	}
}