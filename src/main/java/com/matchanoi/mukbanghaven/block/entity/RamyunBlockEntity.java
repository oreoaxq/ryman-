package com.matchanoi.mukbanghaven.block.entity;

import com.matchanoi.mukbanghaven.block.RamyunBlock;
import com.matchanoi.mukbanghaven.registry.ModBlockEntities;
import com.matchanoi.mukbanghaven.util.RamyunConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * เก็บ "สถานะภายใน" ของบล็อกรามยอนแต่ละใบ แล้ว sync ออกไปเป็น BlockState
 * property (stage 0-5, egg true/false) เพื่อให้ resource pack เลือกโมเดลที่ถูกต้อง
 *
 * internalStage: 0=วางแล้วยังไม่เปิดฝา, 1=เปิดฝาแล้ว, 2=เติมนมกำลังรอสุก, 3=สุกแล้ว
 * เมื่อ internalStage == 3 ให้ดู bites/maxBites เพื่อรู้ว่าอยู่ขั้น "สุก / ซุปเหลือก้นถ้วย / ว่างเปล่า"
 */
public class RamyunBlockEntity extends BlockEntity {

    private int internalStage = RamyunConstants.STAGE_PLACED_UNCOOKED; // 0,1,2,3 เท่านั้น
    private int cookTimer = 0;
    private int bites = 0;
    private boolean hasEgg = false;
    private int maxBites = RamyunConstants.MAX_BITES_NO_EGG;

    public RamyunBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RAMYUN_BLOCK_ENTITY.get(), pos, state);
    }

    // ---------- ตัวเข้าถึงสถานะ ----------
    public int getBites() { return bites; }
    public int getMaxBites() { return maxBites; }
    public boolean hasEgg() { return hasEgg; }

    public boolean canOpenLid() { return internalStage == RamyunConstants.STAGE_PLACED_UNCOOKED; }
    public boolean canAddMilk() { return internalStage == RamyunConstants.STAGE_LID_OPEN; }
    public boolean isCooking() { return internalStage == RamyunConstants.STAGE_COOKING; }
    public boolean isCooked() { return internalStage == RamyunConstants.STAGE_COOKED; }

    /** true เมื่อกินครบ maxBites แล้ว = เหลือน้ำซุปก้นถ้วย */
    public boolean isSoupLeft() { return isCooked() && bites == maxBites; }

    /** true เมื่อกินน้ำซุปก้นถ้วยหมดแล้ว = ว่างเปล่า ต้องลบบล็อกทิ้ง */
    public boolean isEmpty() { return isCooked() && bites > maxBites; }

    /** ยังอยู่ใน 3 ขั้นแรก (ยังไม่สุก) แปลว่าทุบแล้วต้องเด้งกลับเป็น Uncooked item */
    public boolean isPreCook() { return internalStage != RamyunConstants.STAGE_COOKED; }

    // ---------- Action: เปิดฝา ----------
    public boolean openLid() {
        if (!canOpenLid()) return false;
        internalStage = RamyunConstants.STAGE_LID_OPEN;
        syncBlockState();
        return true;
    }

    // ---------- Action: เติมนม ----------
    public boolean addMilk() {
        if (!canAddMilk()) return false;
        internalStage = RamyunConstants.STAGE_COOKING;
        cookTimer = RamyunConstants.COOKING_TIME_TICKS;
        syncBlockState();
        return true;
    }

    // ---------- Action: เติมไข่ (ทำได้เฉพาะตอนสุกแล้วและยังไม่ได้กินคำไหนเลย) ----------
    public boolean addEgg() {
        if (!isCooked() || bites != 0 || hasEgg) return false;
        hasEgg = true;
        maxBites = RamyunConstants.MAX_BITES_WITH_EGG;
        syncBlockState();
        return true;
    }

    /**
     * กินหนึ่งคำ คืนค่า true ถ้ากินสำเร็จ
     * ถ้ากำลังกินคำที่มีไข่ท็อปอยู่ จะกินโบนัสไข่ก่อน แล้ว "ถอดไข่" ออกจากการแสดงผล
     */
    public boolean eatBite() {
        if (!isCooked() || isEmpty() || bites > maxBites) return false;
        if (hasEgg) {
            hasEgg = false; // กินคำไข่แล้ว โมเดลกลับไปเป็นแบบไม่มีไข่
        }
        bites++;
        syncBlockState();
        return true;
    }

    /** คำนวณ stage สำหรับ blockstate (0-5) จากสถานะภายใน แล้วอัปเดต BlockState จริง */
    private void syncBlockState() {
        setChanged();
        if (level == null || level.isClientSide) return;

        int visualStage;
        boolean visualEgg = false;
        if (!isCooked()) {
            visualStage = internalStage; // 0,1,2 ตรงกับภายในเป๊ะ
        } else if (isEmpty()) {
            visualStage = RamyunConstants.STAGE_EMPTY;
        } else if (isSoupLeft()) {
            visualStage = RamyunConstants.STAGE_SOUP_LEFT;
        } else {
            visualStage = RamyunConstants.STAGE_COOKED;
            visualEgg = hasEgg;
        }

        BlockState newState = getBlockState()
                .setValue(RamyunBlock.STAGE, visualStage)
                .setValue(RamyunBlock.EGG, visualEgg);
        level.setBlock(getBlockPos(), newState, 3);
        level.sendBlockUpdated(getBlockPos(), newState, newState, 3);
    }

    // ---------- Ticker ----------
    public static void serverTick(Level level, BlockPos pos, BlockState state, RamyunBlockEntity be) {
        if (be.isCooking()) {
            be.cookTimer--;
            if (be.cookTimer <= 0) {
                be.internalStage = RamyunConstants.STAGE_COOKED;
                be.syncBlockState();
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                            8, 0.15, 0.1, 0.15, 0.01);
                }
            }
        }
    }

    // ---------- NBT ----------
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("InternalStage", internalStage);
        tag.putInt("CookTimer", cookTimer);
        tag.putInt(RamyunConstants.NBT_BITES, bites);
        tag.putBoolean(RamyunConstants.NBT_HAS_EGG, hasEgg);
        tag.putInt("MaxBites", maxBites);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        internalStage = tag.getInt("InternalStage");
        cookTimer = tag.getInt("CookTimer");
        bites = tag.getInt(RamyunConstants.NBT_BITES);
        hasEgg = tag.getBoolean(RamyunConstants.NBT_HAS_EGG);
        maxBites = tag.contains("MaxBites") ? tag.getInt("MaxBites") : RamyunConstants.MAX_BITES_NO_EGG;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** ใช้ตอน breakblock (หลังสุกแล้ว) เพื่อสร้าง ItemStack ที่เก็บ progress การกินไว้ */
    public CompoundTag writeItemNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(RamyunConstants.NBT_BITES, bites);
        tag.putBoolean(RamyunConstants.NBT_HAS_EGG, hasEgg);
        tag.putInt("MaxBites", maxBites);
        return tag;
    }
}
