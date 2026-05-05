package com.humblepotato.legendaryitems.item.custom;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VoidSword extends SwordItem {
	// 20s * 20 ticks/s = 400 ticks
	private static final int TELEPORT_COOLDOWN = 400;
	// 30s * 20 ticks/s = 600 ticks
	private static final int THROW_COOLDOWN = 600;
	private static final double PULL_RANGE = 8.0;
	// 4s stun = 80 ticks
	private static final int STUN_DURATION = 80;
	private static final double TELEPORT_DISTANCE = 10.0;
	private static final double TARGET_SEARCH_RANGE = 20.0;

	public VoidSword(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Item.Settings settings) {
		super(toolMaterial, attackDamage, attackSpeed, settings);
	}

	/**
	 * Allow Sharpness up to level 7 (vanilla caps at 5).
	 */
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}

	@Override
	public boolean canBeEnchantedWith(ItemStack stack, Enchantment enchantment) {
		// Allow Sharpness beyond vanilla cap
		if (enchantment == Enchantments.SHARPNESS) {
			return true;
		}
		return super.canBeEnchantedWith(stack, enchantment);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);

		if (!world.isClient) {
			if (!user.isSneaking()) {
				if (user.getItemCooldownManager().getCooldownProgress(this, 0) == 0) {
					voidTeleport(world, user);
					user.getItemCooldownManager().set(this, TELEPORT_COOLDOWN);
					user.sendMessage(
						net.minecraft.text.Text.literal("\u00A75You step through the void..."),
						true
					);
					return TypedActionResult.success(itemStack);
				}
			} else {
				if (user.getItemCooldownManager().getCooldownProgress(this, 0) == 0) {
					voidThrow(world, user);
					user.getItemCooldownManager().set(this, THROW_COOLDOWN);
					user.sendMessage(
						net.minecraft.text.Text.literal("\u00A75The void reaches out and freezes your target!"),
						true
					);
					return TypedActionResult.success(itemStack);
				}
			}
		}

		return TypedActionResult.pass(itemStack);
	}

	private void voidTeleport(World world, PlayerEntity player) {
		Vec3d look = player.getRotationVector();
		double newX = player.getX() + look.x * TELEPORT_DISTANCE;
		double newY = player.getY() + look.y * TELEPORT_DISTANCE;
		double newZ = player.getZ() + look.z * TELEPORT_DISTANCE;

		// Clamp Y so player doesn't go underground or too high
		newY = Math.max(world.getBottomY() + 1, newY);

		// Particles at origin
		spawnVoidBurst(world, player.getX(), player.getY() + 1, player.getZ(), 20);

		player.teleport(newX, newY, newZ);

		// Particles at destination
		spawnVoidBurst(world, newX, newY + 1, newZ, 40);

		pullNearbyEntities(world, player);
	}

	private void spawnVoidBurst(World world, double x, double y, double z, int count) {
		for (int i = 0; i < count; i++) {
			double angle = (i / (double) count) * Math.PI * 2;
			double px = x + Math.cos(angle) * 0.8;
			double pz = z + Math.sin(angle) * 0.8;
			world.addParticle(ParticleTypes.SOUL, px, y, pz, 0, 0.1, 0);
			world.addParticle(ParticleTypes.REVERSE_PORTAL, px, y, pz, 0, 0.15, 0);
		}
	}

	private void pullNearbyEntities(World world, PlayerEntity player) {
		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class,
			player.getBoundingBox().expand(PULL_RANGE),
			e -> e != player)) {
			double dx = player.getX() - entity.getX();
			double dy = (player.getY() + 1) - entity.getY();
			double dz = player.getZ() - entity.getZ();
			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (dist > 0.1) {
				double speed = 0.6;
				entity.setVelocity(
					entity.getVelocity().add(
						(dx / dist) * speed,
						(dy / dist) * 0.4,
						(dz / dist) * speed
					)
				);
				entity.velocityModified = true;
			}
		}
	}

	private void voidThrow(World world, PlayerEntity player) {
		LivingEntity target = null;
		double closestDist = TARGET_SEARCH_RANGE;

		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class,
			player.getBoundingBox().expand(TARGET_SEARCH_RANGE),
			e -> e != player)) {
			double dist = player.distanceTo(entity);
			if (dist < closestDist && player.canSee(entity)) {
				closestDist = dist;
				target = entity;
			}
		}

		if (target != null) {
			stunEntity(target);
			// Soul particle burst on target
			double tx = target.getX(), ty = target.getY() + 1, tz = target.getZ();
			for (int i = 0; i < 30; i++) {
				double ox = (Math.random() - 0.5) * 2;
				double oy = Math.random() * 2;
				double oz = (Math.random() - 0.5) * 2;
				world.addParticle(ParticleTypes.SOUL, tx + ox * 0.8, ty + oy * 0.5, tz + oz * 0.8, 0, 0.05, 0);
				world.addParticle(ParticleTypes.PORTAL, tx + ox * 0.5, ty + oy * 0.3, tz + oz * 0.5, 0, 0.1, 0);
			}
		}
	}

	private void stunEntity(LivingEntity entity) {
		// Zero velocity first
		entity.setVelocity(Vec3d.ZERO);
		entity.velocityModified = true;

		if (entity instanceof PlayerEntity player) {
			player.setTicksFrozen(STUN_DURATION * 2); // freeze ticks counts differently
		} else {
			// Mobs: max slowness + mining fatigue to simulate stun
			entity.addStatusEffect(new StatusEffectInstance(
				StatusEffects.SLOWNESS, STUN_DURATION, 10, false, true, true));
			entity.addStatusEffect(new StatusEffectInstance(
				StatusEffects.MINING_FATIGUE, STUN_DURATION, 4, false, false, false));
		}
	}
}
