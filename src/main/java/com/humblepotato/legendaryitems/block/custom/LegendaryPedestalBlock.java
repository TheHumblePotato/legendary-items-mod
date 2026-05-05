package com.humblepotato.legendaryitems.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.humblepotato.legendaryitems.block.entity.LegendaryPedestalBlockEntity;

/**
 * Legendary Pedestal block.
 * Implements BlockEntityProvider directly (instead of extending BlockWithEntity)
 * to avoid the abstract method requirement while keeping full block entity support.
 */
public class LegendaryPedestalBlock extends Block implements BlockEntityProvider {

	private static final VoxelShape SHAPE = VoxelShapes.union(
		VoxelShapes.cuboid(0.0625, 0.0,  0.0625, 0.9375, 0.125, 0.9375), // base slab
		VoxelShapes.cuboid(0.25,   0.125, 0.25,   0.75,   0.875, 0.75),  // pillar
		VoxelShapes.cuboid(0.125,  0.875, 0.125,  0.875,  1.0,   0.875)  // top cap
	);

	public LegendaryPedestalBlock(Settings settings) {
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LegendaryPedestalBlockEntity(pos, state);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
							  BlockHitResult hit) {
		if (world.isClient) return ActionResult.SUCCESS;

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!(blockEntity instanceof LegendaryPedestalBlockEntity pedestal)) {
			return ActionResult.PASS;
		}

		if (!pedestal.hasItem()) {
			player.sendMessage(
				net.minecraft.text.Text.literal("\u00A77The pedestal is empty."),
				true
			);
			return ActionResult.PASS;
		}

		if (pedestal.playerHasRequiredItems(player)) {
			ItemStack legendary = pedestal.getItem().copy();
			// Consume all required items from player inventory
			pedestal.consumeRequiredItems(player);
			// Give the legendary item
			if (!player.getInventory().insertStack(legendary)) {
				// If inventory is full, drop at feet
				player.dropItem(legendary, false);
			}
			pedestal.removeItem();
			// Mark for save
			pedestal.markDirty();
			world.updateListeners(pos, state, state, Block.NOTIFY_ALL);

			player.sendMessage(
				net.minecraft.text.Text.literal("\u00A76You have claimed the legendary item!"),
				false
			);
			return ActionResult.SUCCESS;
		} else {
			player.sendMessage(
				net.minecraft.text.Text.literal("\u00A7cYou lack the required items to claim this!"),
				true
			);
			return ActionResult.FAIL;
		}
	}
}
