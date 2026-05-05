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

	/**
	 * Fiery Dagger: 6 damage (netherite sword is 8), faster 2.0f attack speed.
	 * Note: SwordItem adds material's attack damage + base. ToolMaterials.NETHERITE = 3 bonus.
	 * To get 6 total we pass attackDamage=3 (3 base + 3 material = 6).
	 */
	public static final FieryDagger FIERY_DAGGER = new FieryDagger(
		ToolMaterials.NETHERITE,
		3,      // + 3 material bonus = 6 total attack damage
		2.0f,   // faster than netherite sword (1.6f)
		new Item.Settings().maxCount(1)
	);

	/**
	 * Void Sword: 8 damage (same as netherite sword), standard 1.6f speed.
	 * ToolMaterials.NETHERITE gives +3, so pass 5 to get 8 total.
	 */
	public static final VoidSword VOID_SWORD = new VoidSword(
		ToolMaterials.NETHERITE,
		5,      // + 3 = 8 total
		1.6f,
		new Item.Settings().maxCount(1)
	);

	/**
	 * Vampire Sword: 8 damage, standard 1.6f speed.
	 */
	public static final VampireSword VAMPIRE_SWORD = new VampireSword(
		ToolMaterials.NETHERITE,
		5,      // + 3 = 8 total
		1.6f,
		new Item.Settings().maxCount(1)
	);

	public static void register() {
		Registry.register(Registries.ITEM,
			new Identifier(LegendaryItemsMod.MOD_ID, "fiery_dagger"), FIERY_DAGGER);
		Registry.register(Registries.ITEM,
			new Identifier(LegendaryItemsMod.MOD_ID, "void_sword"), VOID_SWORD);
		Registry.register(Registries.ITEM,
			new Identifier(LegendaryItemsMod.MOD_ID, "vampire_sword"), VAMPIRE_SWORD);
	}
}
