package com.humblepotato.legendaryitems.item.custom;

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
import net.minecraft.world.World;

public class FieryDagger extends SwordItem {
	// 30s * 20 ticks/s = 600 ticks
	private static final int FIRE_RING_COOLDOWN = 600;
	// 40s * 20 ticks/s = 800 ticks
	private static final int ENRAGE_COOLDOWN = 800;
	// 10s enrage duration
	private static final int ENRAGE_DURATION = 200;
	private static final double FIRE_RING_RADIUS = 4.0;
	// Fire Aspect II: 4 seconds of fire per hit
	private static final int FIRE_ASPECT_TICKS = 80;

	public FieryDagger(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Item.Settings settings) {
		super(toolMaterial, attackDamage, attackSpeed, settings.fireproof());
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);

		if (!world.isClient) {
			if (!user.isSneaking()) {
				if (user.getItemCooldownManager().getCooldownProgress(this, 0) == 0) {
					spawnFireRing(world, user);
					user.getItemCooldownManager().set(this, FIRE_RING_COOLDOWN);
					user.sendMessage(
						net.minecraft.text.Text.literal("\u00A7cA ring of infernal flames erupts around you!"),
						true
					);
					return TypedActionResult.success(itemStack);
				}
			} else {
				if (user.getItemCooldownManager().getCooldownProgress(this, 0) == 0) {
					enragePlayer(user);
					user.getItemCooldownManager().set(this, ENRAGE_COOLDOWN);
					user.sendMessage(
						net.minecraft.text.Text.literal("\u00A74You become enraged with infernal power!"),
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
		// Passive: Fire Aspect II - set target on fire for 4 seconds
		target.setOnFireFor(FIRE_ASPECT_TICKS / 20);

		if (attacker instanceof PlayerEntity player) {
			// If enraged, extra fire/magic damage that goes through fire resistance
			if (player.hasStatusEffect(StatusEffects.HASTE)) {
				// Void/magic damage goes through fire res but not armor
				target.damage(target.getDamageSources().magic(), 4.0f);
			}
		}
		return super.postHit(stack, target, attacker);
	}

	private void spawnFireRing(World world, PlayerEntity player) {
		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();

		// Animate a ring of flame + smoke particles
		for (int i = 0; i < 60; i++) {
			double angle = (i / 60.0) * Math.PI * 2;
			double px = x + Math.cos(angle) * FIRE_RING_RADIUS;
			double pz = z + Math.sin(angle) * FIRE_RING_RADIUS;
			world.addParticle(ParticleTypes.FLAME, px, y + 0.1, pz, 0, 0.2, 0);
			world.addParticle(ParticleTypes.LARGE_SMOKE, px, y + 0.3, pz, 0, 0.05, 0);
			if (i % 3 == 0) {
				world.addParticle(ParticleTypes.LAVA, px, y + 0.5, pz, 0, 0.1, 0);
			}
		}

		// Inner spiral
		for (int i = 0; i < 30; i++) {
			double angle = (i / 30.0) * Math.PI * 2;
			double r = FIRE_RING_RADIUS * 0.5;
			double px = x + Math.cos(angle) * r;
			double pz = z + Math.sin(angle) * r;
			world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, y + 0.1, pz, 0, 0.15, 0);
		}

		// Void damage (magic, bypasses armor and enchants) to all nearby entities
		for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class,
			player.getBoundingBox().expand(FIRE_RING_RADIUS),
			e -> e != player)) {
			// Magic damage = void damage (bypasses armor + fire res)
			entity.damage(entity.getDamageSources().magic(), 6.0f);
		}
	}

	private void enragePlayer(PlayerEntity player) {
		// Haste III = faster attack speed, Strength II = more damage
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, ENRAGE_DURATION, 2, false, true, true));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, ENRAGE_DURATION, 1, false, true, true));

		// Visual: burst of flame particles around the player
		World world = player.getWorld();
		double x = player.getX(), y = player.getY() + 1, z = player.getZ();
		for (int i = 0; i < 40; i++) {
			double ox = (Math.random() - 0.5) * 2;
			double oy = Math.random() * 2;
			double oz = (Math.random() - 0.5) * 2;
			world.addParticle(ParticleTypes.FLAME, x, y, z, ox * 0.2, oy * 0.15, oz * 0.2);
			world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, ox * 0.15, oy * 0.1, oz * 0.15);
		}
	}
}
