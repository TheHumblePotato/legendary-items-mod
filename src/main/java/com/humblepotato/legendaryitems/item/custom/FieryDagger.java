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
import net.minecraft.world.World;

public class FieryDagger extends SwordItem {
	private static final int FIRE_RING_COOLDOWN = 600; // 30 seconds
	private static final int ENRAGE_COOLDOWN = 800; // 40 seconds
	private static final int ENRAGE_DURATION = 200; // 10 seconds
	private static final double FIRE_RING_RADIUS = 4.0;

	public FieryDagger(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Item.Settings settings) {
		super(toolMaterial, attackDamage, attackSpeed, settings.fireproof());
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);

		if (!world.isClient && !user.isSneaking()) {
			if (user.getItemCooldown(this) == 0) {
				spawnFireRing(world, user);
				user.setItemCooldown(this, FIRE_RING_COOLDOWN);
				return TypedActionResult.success(itemStack);
			}
		} else if (!world.isClient && user.isSneaking()) {
			if (user.getItemCooldown(this) == 0) {
				enragePlayer(user);
				user.setItemCooldown(this, ENRAGE_COOLDOWN);
				return TypedActionResult.success(itemStack);
			}
		}

		return TypedActionResult.pass(itemStack);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof PlayerEntity player) {
			boolean hasEnrage = player.hasStatusEffect(StatusEffects.HASTE);
			if (hasEnrage) {
				// Deal extra fire damage that goes through fire resistance
				target.damage(target.getDamageSources().magic(), 3.0f);
			}
		}
		return super.postHit(stack, target, attacker);
	}

	private void spawnFireRing(World world, PlayerEntity player) {
		double x = player.getX();
		double y = player.getY() + 1.0;
		double z = player.getZ();

		// Particle circle
		for (int i = 0; i < 50; i++) {
			double angle = (i / 50.0) * Math.PI * 2;
			double px = x + Math.cos(angle) * FIRE_RING_RADIUS;
			double pz = z + Math.sin(angle) * FIRE_RING_RADIUS;
			world.addParticle(ParticleTypes.FLAME, px, y, pz, 0, 0.15, 0);
			world.addParticle(ParticleTypes.LARGE_SMOKE, px, y, pz, 0, 0.1, 0);
		}

		// Apply void damage to nearby entities (fire damage bypasses armor)
		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, 
			player.getBoundingBox().expand(FIRE_RING_RADIUS), 
			e -> e != player)) {
			entity.damage(entity.getDamageSources().magic(), 5.0f);
		}
	}

	private void enragePlayer(PlayerEntity player) {
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, ENRAGE_DURATION, 2, false, true));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, ENRAGE_DURATION, 2, false, true));
	}
}