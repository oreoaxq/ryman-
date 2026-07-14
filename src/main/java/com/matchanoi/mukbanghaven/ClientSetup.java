package com.matchanoi.mukbanghaven;

import com.matchanoi.mukbanghaven.registry.ModItems;
import com.matchanoi.mukbanghaven.util.RamyunConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * ลงทะเบียน item property "mukbanghaven:has_egg" ที่ model json ของ cooked_ramyun
 * ใช้เป็นเงื่อนไขสลับ texture/model ตอนมีไข่ท็อป (ดู models/item/cooked_ramyun.json)
 */
@Mod.EventBusSubscriber(modid = MukbangHaven.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {}

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> net.minecraft.client.renderer.item.ItemProperties.register(
                ModItems.COOKED_RAMYUN.get(),
                new ResourceLocation(MukbangHaven.MOD_ID, "has_egg"),
                (stack, level, entity, seed) ->
                        stack.getOrCreateTag().getBoolean(RamyunConstants.NBT_HAS_EGG) ? 1.0f : 0.0f
        ));
    }
}
