package com.humblepotato.legendaryitems.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VoidSword extends SwordItem {
	private static final int TELEPORT_COOLDOWN = 400; // 20 seconds
	private static final int THROW_COOLDOWN = 600; // 30 seconds
	private static final double PULL_RANGE = 8.0;
	private static final int STUN_DURATION = 80; // 4 seconds
	private static final double TELEPORT_DISTANCE = 10.0;

	public VoidSword(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Item.Settings settings) {
		super(toolMaterial, attackDamage, attackSpeed, settings.fireproof());
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);

		if (!world.isClient && !user.isSneaking()) {
			if (user.getItemCooldown(this) == 0) {
				voidTeleport(world, user);
				user.setItemCooldown(this, TELEPORT_COOLDOWN);
				return TypedActionResult.success(itemStack);
			}
		} else if (!world.isClient && user.isSneaking()) {
			if (user.getItemCooldown(this) == 0) {
				voidThrow(world, user);
				user.setItemCooldown(this, THROW_COOLDOWN);
				return TypedActionResult.success(itemStack);
			}
		}

		return TypedActionResult.pass(itemStack);
	}

	private void voidTeleport(World world, PlayerEntity player) {
		Vec3d lookDirection = player.getRotationVector();
		double newX = player.getX() + lookDirection.x * TELEPORT_DISTANCE;
		double newY = player.getY() + lookDirection.y * TELEPORT_DISTANCE;
		double newZ = player.getZ() + lookDirection.z * TELEPORT_DISTANCE;

		// Particle effect at destination
		for (int i = 0; i < 40; i++) {
			double angle = (i / 40.0) * Math.PI * 2;
			double offsetX = Math.cos(angle) * 0.8;
			double offsetZ = Math.sin(angle) * 0.8;
			world.addParticle(ParticleTypes.SOUL, newX + offsetX, newY + 1, newZ + offsetZ, 0, 0.1, 0);
		}

		player.teleport(newX, newY, newZ);
		pullNearbyEntities(world, player);
	}

	private void pullNearbyEntities(World world, PlayerEntity player) {
		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, 
			player.getBoundingBox().expand(PULL_RANGE), 
			e -> e != player)) {
			double dx = player.getX() - entity.getX();
			double dy = player.getY() + 1 - entity.getY();
			double dz = player.getZ() - entity.getZ();
			double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (distance > 0.1) {
				double speed = 0.6;
				entity.setVelocity(
					entity.getVelocity().add((dx / distance) * speed, (dy / distance) * 0.4, (dz / distance) * speed)
				);
			}
		}
	}

	private void voidThrow(World world, PlayerEntity player) {
		LivingEntity target = null;
		double closestDistance = 20.0;

		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, 
			player.getBoundingBox().expand(20.0), 
			e -> e != player)) {
			double distance = player.distanceTo(entity);
			if (distance < closestDistance && player.canSee(entity)) {
				closestDistance = distance;
				target = entity;
			}
		}

		if (target != null) {
			stunEntity(target);
			// Particle effect on target
			for (int i = 0; i < 20; i++) {
				world.addParticle(ParticleTypes.SOUL, 
					target.getX() + (Math.random() - 0.5) * 2, 
					target.getY() + 1, 
					target.getZ() + (Math.random() - 0.5) * 2, 
					0, 0.1, 0);
			}
		}
	}

	private void stunEntity(LivingEntity entity) {
		entity.setVelocity(0, 0, 0);
		if (entity instanceof PlayerEntity player) {
			player.setTicksFrozen(STUN_DURATION);
		} else {
			// For mobs, apply slowness
			var slowness = new StatusEffectInstance(
				StatusEffects.SLOWNESS, 
				STUN_DURATION, 
				3, 
				false, 
				true
			);
			entity.addStatusEffect(slowness);
		}
	}
}