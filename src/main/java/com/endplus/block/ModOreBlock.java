package com.endplus.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.item.ItemStack;

public class ModOreBlock extends Block {

    private final IntProvider experienceDropped;

    public ModOreBlock(IntProvider experienceDropped, Settings settings) {
        super(settings);
        this.experienceDropped = experienceDropped;
    }

    @Override
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.onStacksDropped(state, world, pos, tool, dropExperience);
        if (dropExperience) {
            this.dropExperience(world, pos, this.experienceDropped.get(world.getRandom()));
        }
    }
}
