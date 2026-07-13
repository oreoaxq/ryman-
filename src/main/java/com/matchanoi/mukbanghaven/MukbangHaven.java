package com.matchanoi.mukbanghaven;

import com.matchanoi.mukbanghaven.registry.ModBlockEntities;
import com.matchanoi.mukbanghaven.registry.ModBlocks;
import com.matchanoi.mukbanghaven.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MukbangHaven.MOD_ID)
public class MukbangHaven {

    public static final String MOD_ID = "mukbanghaven";

    public MukbangHaven() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(this::addCreative);
    }

    // เพิ่มไอเทมเข้าแท็บ Food & Drinks ของ creative inventory
    private void addCreative(CreativeModeTabEvent.BuildContents event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.UNCOOKED_RAMYUN);
            event.accept(ModItems.COOKED_RAMYUN);
        }
    }
}
