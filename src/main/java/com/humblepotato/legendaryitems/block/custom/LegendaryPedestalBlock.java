package com.humblepotato.legendaryitems.block.custom;

import net.minecraft.block.Block;
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

public class LegendaryPedestalBlock extends Block {
	private static final VoxelShape SHAPE = VoxelShapes.union(
		VoxelShapes.cuboid(0.1, 0.0, 0.1, 0.9, 0.2, 0.9),  // base
		VoxelShapes.cuboid(0.2, 0.2, 0.2, 0.8, 0.9, 0.8)   // column
	);

	public LegendaryPedestalBlock(Settings settings) {
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) return ActionResult.SUCCESS;

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof LegendaryPedestalBlockEntity pedestal) {
			if (pedestal.hasItem()) {
				if (pedestal.playerHasRequiredItems(player)) {
					ItemStack legendaryItem = pedestal.getItem().copy();
					if (player.getInventory().insertStack(legendaryItem)) {
						pedestal.removeItem();
						player.sendMessage(
							net.minecraft.text.Text.literal("\u00A76You claimed the legendary item!"),
							false
						);
						return ActionResult.SUCCESS;
					}
				} else {
					player.sendMessage(
						net.minecraft.text.Text.literal("\u00A7cYou don't have the required items!"),
						true
					);
				}
			}
		}
		return ActionResult.PASS;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LegendaryPedestalBlockEntity(pos, state);
	}

	@Override
	public boolean hasBlockEntity() {
		return true;
	}
}