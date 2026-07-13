package com.matchanoi.mukbanghaven.item;

import com.matchanoi.mukbanghaven.block.RamyunBlock;
import com.matchanoi.mukbanghaven.util.RamyunConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * มาม่าสุกแบบถือกิน - ใช้ Durability แทนจำนวนคำที่เหลือ
 * (แนวคิดเดียวกับ Tool: Damage เพิ่มทีละ 1 ต่อการกิน 1 คำ, พอถึง MaxDamage ไอเทมจะแตกหายไปเอง)
 */
public class RamyunItem extends Item {

    public RamyunItem(Properties properties) {
        // durability สูงสุดที่เป็นไปได้ (กรณีเติมไข่) = MAX_BITES_WITH_EGG + 1 (บวกคำน้ำซุปก้นถ้วย)
        super(properties.durability(RamyunConstants.MAX_BITES_WITH_EGG + 1));
    }

    /** MaxDamage จริงของแต่ละ stack ขึ้นกับว่ามีไข่หรือไม่ (เก็บไว้ตอนบล็อกถูกทุบ) */
    @Override
    public int getMaxDamage(ItemStack stack) {
        int maxBites = stack.getOrCreateTag().contains("MaxBites")
                ? stack.getOrCreateTag().getInt("MaxBites")
                : RamyunConstants.MAX_BITES_NO_EGG;
        return maxBites + 1; // +1 สำหรับคำน้ำซุปก้นถ้วย
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        int maxDamage = getMaxDamage(stack);
        int damage = stack.getDamageValue();
        if (damage >= maxDamage) {
            return InteractionResultHolder.pass(stack);
        }

        boolean hasEgg = stack.getOrCreateTag().getBoolean(RamyunConstants.NBT_HAS_EGG);
        if (damage == 0 && hasEgg) {
            // คำแรกคือคำที่มีไข่ท็อป -> กินไข่แล้วถอดสถานะไข่ออก
            stack.getOrCreateTag().putBoolean(RamyunConstants.NBT_HAS_EGG, false);
        }

        stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
        RamyunBlock.applyEatEffects(player);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS,
                0.8f, 1.0f + level.random.nextFloat() * 0.4f - 0.2f);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int maxDamage = getMaxDamage(stack);
        int remaining = maxDamage - stack.getDamageValue();
        boolean hasEgg = stack.getOrCreateTag().getBoolean(RamyunConstants.NBT_HAS_EGG);
        tooltip.add(Component.translatable("tooltip.mukbanghaven.bites_left", remaining).withStyle(ChatFormatting.GOLD));
        if (hasEgg) {
            tooltip.add(Component.translatable("tooltip.mukbanghaven.egg_topped").withStyle(ChatFormatting.YELLOW));
        }
    }
}
