package com.matchanoi.mukbanghaven.registry;

import com.matchanoi.mukbanghaven.MukbangHaven;
import com.matchanoi.mukbanghaven.block.RamyunBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MukbangHaven.MOD_ID);

    public static final RegistryObject<Block> RAMYUN_BLOCK = BLOCKS.register("ramyun",
            () -> new RamyunBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.3f)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .noCollission()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    private ModBlocks() {}
}
