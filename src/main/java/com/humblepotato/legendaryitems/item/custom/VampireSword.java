package com.humblepotato.legendaryitems.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class VampireSword extends SwordItem {
	// 30s * 20 ticks/s = 600 ticks
	private static final int HEART_STEAL_COOLDOWN = 600;
	// 45s * 20 ticks/s = 900 ticks
	private static final int BITE_COOLDOWN = 900;
	private static final double HEART_STEAL_RANGE = 5.0;
	// Bite lasts 10 seconds = 200 ticks
	private static final int BITE_DURATION = 200;
	// 10% heal chance
	private static final double HEAL_CHANCE = 0.10;

	private static final String BITE_ACTIVE_KEY = "VampireBiteActive";
	private static final String BITE_EXPIRE_KEY = "VampireBiteExpire";

	public VampireSword(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Item.Settings settings) {
		super(toolMaterial, attackDamage, attackSpeed, settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);

		if (!world.isClient) {
			if (!user.isSneaking()) {
				if (user.getItemCooldownManager().getCooldownProgress(this, 0) == 0) {
					heartSteal(world, user);
					user.getItemCooldownManager().set(this, HEART_STEAL_COOLDOWN);
					user.sendMessage(
						net.minecraft.text.Text.literal("\u00A74You drain the life force of nearby players!"),
						true
					);
					return TypedActionResult.success(itemStack);
				}
			} else {
				if (user.getItemCooldownManager().getCooldownProgress(this, 0) == 0) {
					activateBite(user, itemStack);
					user.getItemCooldownManager().set(this, BITE_COOLDOWN);
					user.sendMessage(
						net.minecraft.text.Text.literal("\u00A7cYour fangs hunger for the next target..."),
						true
					);
					return TypedActionResult.success(itemStack);
				}
			}
		}

		return TypedActionResult.pass(itemStack);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof PlayerEntity player) {
			// Passive: 10% chance to heal 1 heart (2 HP) on hit
			if (Math.random() < HEAL_CHANCE) {
				float newHealth = Math.min(player.getHealth() + 2.0f, player.getMaxHealth());
				player.setHealth(newHealth);
				// Small heart particle on the player
				spawnHeartParticle(player.getWorld(), player.getX(), player.getY() + 1.5, player.getZ(), 5);
			}

			// Check bite: active and not expired
			NbtCompound nbt = stack.getOrCreateNbt();
			boolean biteActive = nbt.getBoolean(BITE_ACTIVE_KEY);
			long biteExpire = nbt.getLong(BITE_EXPIRE_KEY);
			long currentTime = player.getWorld().getTime();

			if (biteActive && currentTime <= biteExpire) {
				// Triple damage (on top of the normal attack)
				target.damage(target.getDamageSources().playerAttack(player),
					(float) (getAttackDamage() * 2)); // +2x = 3x total

				// Drain 2 max hearts (4 HP) from target
				drainMaxHealth(target, 4.0);

				// Gain 2 max hearts (4 HP) for player
				gainMaxHealth(player, 4.0);
				player.setHealth(Math.min(player.getHealth() + 4.0f, player.getMaxHealth()));

				// Consume bite
				nbt.putBoolean(BITE_ACTIVE_KEY, false);
				nbt.putLong(BITE_EXPIRE_KEY, 0L);

				// Dramatic particles
				spawnBiteParticles(player.getWorld(), target.getX(), target.getY() + 1, target.getZ());
				player.sendMessage(
					net.minecraft.text.Text.literal("\u00A74You drain their very life essence!"),
					true
				);
			}
		}
		return super.postHit(stack, target, attacker);
	}

	/**
	 * Called each tick to expire stale bite buffs.
	 * Fabric item tick doesn't exist, so we check on each hit instead.
	 * The expire time is stored in world time (ticks since world creation).
	 */
	private void activateBite(PlayerEntity player, ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putBoolean(BITE_ACTIVE_KEY, true);
		// Expire after BITE_DURATION ticks from now
		nbt.putLong(BITE_EXPIRE_KEY, player.getWorld().getTime() + BITE_DURATION);
	}

	private void heartSteal(World world, PlayerEntity player) {
		int playersAffected = 0;

		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class,
			player.getBoundingBox().expand(HEART_STEAL_RANGE),
			e -> e != player && e instanceof PlayerEntity)) {
			PlayerEntity target = (PlayerEntity) entity;
			// Drain 1 heart (2 HP) from each player
			drainMaxHealth(target, 2.0);
			playersAffected++;
			// Visual on victims
			spawnHeartParticle(world, target.getX(), target.getY() + 1.5, target.getZ(), 8);
		}

		if (playersAffected > 0) {
			// Grant 1 heart per player drained
			gainMaxHealth(player, playersAffected * 2.0);
			player.setHealth(Math.min(player.getHealth() + (playersAffected * 2.0f), player.getMaxHealth()));
		}

		// Steal ring particles around the caster
		spawnHeartStealRing(world, player.getX(), player.getY(), player.getZ());
	}

	private void drainMaxHealth(LivingEntity entity, double amount) {
		EntityAttributeInstance attr = entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (attr != null) {
			double newMax = Math.max(2.0, attr.getBaseValue() - amount);
			attr.setBaseValue(newMax);
			// Clamp current health to new max
			if (entity instanceof LivingEntity le) {
				if (le.getHealth() > le.getMaxHealth()) {
					le.setHealth(le.getMaxHealth());
				}
			}
		}
	}

	private void gainMaxHealth(PlayerEntity player, double amount) {
		EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (attr != null) {
			attr.setBaseValue(attr.getBaseValue() + amount);
		}
	}

	private void spawnHeartStealRing(World world, double x, double y, double z) {
		for (int i = 0; i < 60; i++) {
			double angle = (i / 60.0) * Math.PI * 2;
			double px = x + Math.cos(angle) * HEART_STEAL_RANGE;
			double pz = z + Math.sin(angle) * HEART_STEAL_RANGE;
			world.addParticle(ParticleTypes.HEART, px, y + 1.0, pz, 0, 0.1, 0);
			world.addParticle(ParticleTypes.ENCHANTED_HIT, px, y + 0.5, pz, 0, 0.08, 0);
		}
		// Center burst
		for (int i = 0; i < 20; i++) {
			double ox = (Math.random() - 0.5) * 2;
			double oz = (Math.random() - 0.5) * 2;
			world.addParticle(ParticleTypes.HEART, x + ox, y + 1.5, z + oz, 0, 0.15, 0);
		}
	}

	private void spawnHeartParticle(World world, double x, double y, double z, int count) {
		for (int i = 0; i < count; i++) {
			double ox = (Math.random() - 0.5) * 0.8;
			double oy = Math.random() * 0.5;
			double oz = (Math.random() - 0.5) * 0.8;
			world.addParticle(ParticleTypes.HEART, x + ox, y + oy, z + oz, 0, 0.1, 0);
		}
	}

	private void spawnBiteParticles(World world, double x, double y, double z) {
		for (int i = 0; i < 25; i++) {
			double ox = (Math.random() - 0.5) * 1.5;
			double oy = Math.random() * 1.5;
			double oz = (Math.random() - 0.5) * 1.5;
			world.addParticle(ParticleTypes.ENCHANTED_HIT, x + ox, y + oy, z + oz, 0, 0.05, 0);
			world.addParticle(ParticleTypes.DAMAGE_INDICATOR, x + ox * 0.5, y + oy, z + oz * 0.5, 0, 0.1, 0);
		}
	}
}
