package com.matchanoi.mukbanghaven.registry;

import com.matchanoi.mukbanghaven.MukbangHaven;
import com.matchanoi.mukbanghaven.block.entity.RamyunBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MukbangHaven.MOD_ID);

    public static final RegistryObject<BlockEntityType<RamyunBlockEntity>> RAMYUN_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ramyun", () -> BlockEntityType.Builder.of(
                    RamyunBlockEntity::new, ModBlocks.RAMYUN_BLOCK.get()).build(null));

    private ModBlockEntities() {}
}
