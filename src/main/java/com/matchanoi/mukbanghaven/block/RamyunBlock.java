package com.matchanoi.mukbanghaven.block;

import com.matchanoi.mukbanghaven.block.entity.RamyunBlockEntity;
import com.matchanoi.mukbanghaven.registry.ModBlockEntities;
import com.matchanoi.mukbanghaven.registry.ModItems;
import com.matchanoi.mukbanghaven.util.RamyunConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class RamyunBlock extends net.minecraft.world.level.block.BaseEntityBlock {

    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, RamyunConstants.MAX_STAGE_PROPERTY);
    public static final BooleanProperty EGG = BooleanProperty.create("egg");

    private static final VoxelShape SHAPE = Shapes.box(0.1, 0, 0.1, 0.9, 0.5, 0.9);

    public RamyunBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(STAGE, RamyunConstants.STAGE_PLACED_UNCOOKED)
                .setValue(EGG, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(STAGE, EGG);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RamyunBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.RAMYUN_BLOCK_ENTITY.get(), RamyunBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof RamyunBlockEntity be)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);

        // ขั้น 0 -> เปิดฝา ไม่สนไอเทมในมือ
        if (be.canOpenLid()) {
            be.openLid();
            level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.6f, 1.4f);
            return InteractionResult.CONSUME;
        }

        // ขั้น 1 -> ต้องใช้ farmersdelight:milk_bottle เติมนม
        if (be.canAddMilk()) {
            Item milkBottle = ForgeRegistries.ITEMS.getValue(new ResourceLocation("farmersdelight", "milk_bottle"));
            if (milkBottle != null && held.is(milkBottle)) {
                if (be.addMilk()) {
                    held.shrink(1);
                    ItemStack glass = new ItemStack(Items.GLASS_BOTTLE);
                    if (!player.getInventory().add(glass)) {
                        player.drop(glass, false);
                    }
                    level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.7f, 1.0f);
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.PASS;
        }

        // ขั้น 2 -> กำลังปรุง ทำอะไรไม่ได้ นอกจากรอ
        if (be.isCooking()) {
            return InteractionResult.PASS;
        }

        // สุกแล้ว: ถ้ากินน้ำซุปหมด (isEmpty) -> คลิกอะไรก็ลบบล็อกทิ้ง
        if (be.isEmpty()) {
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.5f, 1.0f);
            return InteractionResult.CONSUME;
        }

        // สุกแล้ว ยังไม่ได้กินคำไหนเลย และถือไข่ -> เติมไข่
        if (be.isCooked() && be.getBites() == 0 && !be.hasEgg() && isEggItem(held)) {
            if (be.addEgg()) {
                held.shrink(1);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.6f, 1.2f);
                return InteractionResult.CONSUME;
            }
        }

        // สุกแล้ว (รวมถึงตอนเหลือน้ำซุปก้นถ้วย) -> กิน
        if (be.isCooked()) {
            if (be.eatBite()) {
                applyEatEffects(player);
                level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8f, 1.0f + level.random.nextFloat() * 0.4f - 0.2f);
                if (be.isEmpty()) {
                    // จะถูกลบทิ้งในคลิกครั้งถัดไป (ให้ผู้เล่นเห็น Stage ว่างเปล่าก่อน)
                }
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    private boolean isEggItem(ItemStack stack) {
        if (stack.is(Items.EGG)) return true;
        Item friedEgg = ForgeRegistries.ITEMS.getValue(new ResourceLocation("farmersdelight", "fried_egg"));
        return friedEgg != null && stack.is(friedEgg);
    }

    public static void applyEatEffects(Player player) {
        var foodData = player.getFoodData();
        foodData.setFoodLevel(Math.min(20, foodData.getFoodLevel() + RamyunConstants.HUNGER_PER_BITE));
        foodData.setSaturation(Math.min(foodData.getFoodLevel(), foodData.getSaturationLevel() + RamyunConstants.SATURATION_PER_BITE * 10));

        var comfort = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("farmersdelight", "comfort"));
        if (comfort != null) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(comfort, RamyunConstants.COMFORT_DURATION_TICKS, 0, false, true));
        }
    }

    // ---------- Drop logic ----------
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RamyunBlockEntity be) {
            if (!player.isCreative()) {
                ItemStack drop;
                if (be.isPreCook()) {
                    // ทุบใน 3 ขั้นแรก -> เด้งกลับไปเป็นมาม่าดิบ (เสียของที่เติมไปแล้ว)
                    drop = new ItemStack(ModItems.UNCOOKED_RAMYUN.get());
                } else if (be.isEmpty()) {
                    drop = ItemStack.EMPTY;
                } else {
                    drop = new ItemStack(ModItems.COOKED_RAMYUN.get());
                    drop.setTag(be.writeItemNbt());
                }
                if (!drop.isEmpty()) {
                    net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
