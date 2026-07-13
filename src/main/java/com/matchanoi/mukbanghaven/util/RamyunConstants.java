package com.matchanoi.mukbanghaven.util;

/**
 * ค่าคงที่ทั้งหมดของกลไก Ramyun
 * อ้างอิงจากสเปคที่ MATCHANOi กำหนด:
 * - กินได้ 3 รอบ (ไม่รวมน้ำซุปก้นถ้วย) = 4 รอบรวมน้ำก้นถ้วย
 * - กินได้อีก 1 รอบพิเศษถ้าเติมไข่ก่อนกินครั้งแรก (บวกได้แค่ครั้งเดียว)
 * - แต่ละคำ: Hunger +6 (3 หลอด), Saturation +6 (3 หลอด)
 * - ติด farmersdelight:comfort I เป็นเวลา 2 นาทีครึ่ง (150 วินาที) ต่อคำ
 * - เติมน้ำนมแล้วรอ 10 วินาทีถึงจะสุก
 */
public final class RamyunConstants {

    private RamyunConstants() {}

    // ----- Stage IDs (blockstate property "stage", ช่วง 0-5) -----
    // สถานะ "มีไข่" แยกเก็บใน blockstate property "egg" (มีผลเฉพาะตอน stage == STAGE_COOKED)
    public static final int STAGE_PLACED_UNCOOKED = 0; // วางบล็อกแรกสุด ยังไม่เปิดฝา
    public static final int STAGE_LID_OPEN = 1;         // เปิดฝาแล้ว รอเติมนม
    public static final int STAGE_COOKING = 2;           // เติมนมแล้ว กำลังรอ 10 วิ
    public static final int STAGE_COOKED = 3;            // สุกแล้ว (ดู bites เพื่อรู้ว่ากินไปกี่คำ)
    public static final int STAGE_SOUP_LEFT = 4;         // กินครบแล้ว เหลือน้ำซุปก้นถ้วย
    public static final int STAGE_EMPTY = 5;             // กินน้ำซุปหมดแล้ว คลิก/ทุบครั้งต่อไป = หายไปเลย

    public static final int MAX_STAGE_PROPERTY = STAGE_EMPTY;

    // ----- เวลา (หน่วย tick, 20 tick = 1 วินาที) -----
    public static final int COOKING_TIME_TICKS = 200; // 10 วินาที
    public static final int COMFORT_DURATION_TICKS = 150 * 20; // 2 นาทีครึ่ง

    // ----- กลไกการกิน -----
    public static final int NORMAL_BITES = 3;      // กินได้ 3 คำปกติ (ไม่รวมไข่/ซุป)
    public static final int EGG_BONUS_BITES = 1;   // โบนัส +1 คำถ้าเติมไข่ก่อนกินคำแรก
    public static final int MAX_BITES_NO_EGG = NORMAL_BITES;                 // 3
    public static final int MAX_BITES_WITH_EGG = NORMAL_BITES + EGG_BONUS_BITES; // 4

    public static final int HUNGER_PER_BITE = 6;      // 3 หลอด
    public static final float SATURATION_PER_BITE = 0.6f; // เทียบเท่า 3 หลอดทอง (saturation modifier มาตรฐาน 0.2/หลอด)

    // ----- ไอเทม -----
    public static final int UNCOOKED_STACK_SIZE = 16;

    // ----- NBT / DataComponent keys -----
    public static final String NBT_BITES = "Bites";
    public static final String NBT_HAS_EGG = "HasEgg";
    public static final String NBT_STAGE = "RamyunStage";
    public static final String NBT_COOK_TIME = "CookTime";
}
