package com.matchanoi.mukbanghaven.registry;

import com.matchanoi.mukbanghaven.MukbangHaven;
import com.matchanoi.mukbanghaven.item.RamyunItem;
import com.matchanoi.mukbanghaven.util.RamyunConstants;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MukbangHaven.MOD_ID);

    // ไอเทมมาม่าดิบ - วางเป็นบล็อกได้เท่านั้น (จุดเริ่มต้นของการปรุง) กองได้ 16
    public static final RegistryObject<Item> UNCOOKED_RAMYUN = ITEMS.register("uncooked_ramyun",
            () -> new BlockItem(ModBlocks.RAMYUN_BLOCK.get(),
                    new Item.Properties().stacksTo(RamyunConstants.UNCOOKED_STACK_SIZE)));

    // ไอเทมมาม่าสุก - ได้จากการทุบบล็อกหลังสุกแล้ว ถือกินได้ ใช้ Durability แทนจำนวนคำที่กินได้
    public static final RegistryObject<Item> COOKED_RAMYUN = ITEMS.register("cooked_ramyun",
            () -> new RamyunItem(new Item.Properties().stacksTo(1)));

    private ModItems() {}
}
