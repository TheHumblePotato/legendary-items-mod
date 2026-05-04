package com.humblepotato.legendaryitems.item.custom;

import java.util.Random;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class VampireSword extends SwordItem {
	private static final int HEART_STEAL_COOLDOWN = 600; // 30 seconds
	private static final int BITE_COOLDOWN = 900; // 45 seconds
	private static final double HEART_STEAL_RANGE = 5.0;
	private static final int BITE_DURATION = 200; // 10 seconds
	private static final Random RANDOM = new Random();
	private static final String BITE_KEY = "VampireBite";

	public VampireSword(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Item.Settings settings) {
		super(toolMaterial, attackDamage, attackSpeed, settings.fireproof());
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);

		if (!world.isClient && !user.isSneaking()) {
			if (user.getItemCooldown(this) == 0) {
				heartSteal(world, user);
				user.setItemCooldown(this, HEART_STEAL_COOLDOWN);
				return TypedActionResult.success(itemStack);
			}
		} else if (!world.isClient && user.isSneaking()) {
			if (user.getItemCooldown(this) == 0) {
				activateBite(user, itemStack);
				user.setItemCooldown(this, BITE_COOLDOWN);
				return TypedActionResult.success(itemStack);
			}
		}

		return TypedActionResult.pass(itemStack);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof PlayerEntity player) {
			// 10% chance to heal on hit
			if (RANDOM.nextDouble() < 0.10) {
				if (player.getHealth() < player.getMaxHealth()) {
					player.heal(2.0f);
				}
			}

			// Check if bite is active
			NbtCompound nbt = stack.getNbt();
			if (nbt != null && nbt.getBoolean(BITE_KEY)) {
				// Triple damage
				target.damage(target.getDamageSources().playerAttack(player), (float) (this.getAttackDamage() * 3));
				
				// Drain 2 max health from target
				double targetMaxHealth = target.getMaxHealth();
				if (target instanceof PlayerEntity targetPlayer) {
					targetPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
						.setBaseValue(Math.max(2.0, targetMaxHealth - 4.0));
				}
				
				// Gain 2 max health
				double playerMaxHealth = player.getMaxHealth();
				player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
					.setBaseValue(playerMaxHealth + 4.0);
				player.setHealth(Math.min(player.getHealth() + 4.0f, player.getMaxHealth()));
				
				// Reset bite
				nbt.putBoolean(BITE_KEY, false);
			}
		}

		return super.postHit(stack, target, attacker);
	}

	private void heartSteal(World world, PlayerEntity player) {
		int playersAffected = 0;
		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, 
			player.getBoundingBox().expand(HEART_STEAL_RANGE), 
			e -> e != player && e instanceof PlayerEntity)) {
			PlayerEntity targetPlayer = (PlayerEntity) entity;
			double targetMaxHealth = targetPlayer.getMaxHealth();
			targetPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
				.setBaseValue(Math.max(2.0, targetMaxHealth - 2.0));
			playersAffected++;
		}

		// Gain max health for each player affected
		double playerMaxHealth = player.getMaxHealth();
		player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
			.setBaseValue(playerMaxHealth + (playersAffected * 2.0));

		// Particles
		spawnHeartStealParticles(world, player.getX(), player.getY() + 1, player.getZ());
	}

	private void activateBite(PlayerEntity player, ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putBoolean(BITE_KEY, true);
	}

	private void spawnHeartStealParticles(World world, double x, double y, double z) {
		for (int i = 0; i < 50; i++) {
			double angle = (i / 50.0) * Math.PI * 2;
			double px = x + Math.cos(angle) * 3.0;
			double pz = z + Math.sin(angle) * 3.0;
			world.addParticle(ParticleTypes.HEART, px, y, pz, 0, 0.15, 0);
			world.addParticle(ParticleTypes.ENCHANTED_HIT, px, y, pz, 0, 0.1, 0);
		}
	}
}