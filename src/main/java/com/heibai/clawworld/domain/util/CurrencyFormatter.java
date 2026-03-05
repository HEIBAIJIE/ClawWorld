package com.heibai.clawworld.domain.util;

/**
 * 货币格式化工具类
 * 将铜币数量转换为金/银/铜的分级显示
 *
 * 换算规则：
 * 1金币 = 1000银币
 * 1银币 = 1000铜币
 * 因此：1金币 = 1,000,000铜币
 */
public class CurrencyFormatter {

    private static final int COPPER_PER_SILVER = 1000;
    private static final int COPPER_PER_GOLD = 1000000; // 1000 * 1000

    /**
     * 将铜币数量格式化为可读字符串
     * 例如：1234567 铜币 -> "1金234银567铜"
     *
     * @param copperAmount 铜币数量
     * @return 格式化后的字符串
     */
    public static String format(int copperAmount) {
        if (copperAmount < 0) {
            return "0铜";
        }

        int gold = copperAmount / COPPER_PER_GOLD;
        int remaining = copperAmount % COPPER_PER_GOLD;
        int silver = remaining / COPPER_PER_SILVER;
        int copper = remaining % COPPER_PER_SILVER;

        StringBuilder result = new StringBuilder();

        if (gold > 0) {
            result.append(gold).append("金");
        }
        if (silver > 0) {
            result.append(silver).append("银");
        }
        if (copper > 0 || result.length() == 0) {
            result.append(copper).append("铜");
        }

        return result.toString();
    }

    /**
     * 将铜币数量格式化为详细字符串（总是显示三级）
     * 例如：1234567 铜币 -> "1金 234银 567铜"
     *
     * @param copperAmount 铜币数量
     * @return 格式化后的字符串
     */
    public static String formatDetailed(int copperAmount) {
        if (copperAmount < 0) {
            return "0金 0银 0铜";
        }

        int gold = copperAmount / COPPER_PER_GOLD;
        int remaining = copperAmount % COPPER_PER_GOLD;
        int silver = remaining / COPPER_PER_SILVER;
        int copper = remaining % COPPER_PER_SILVER;

        return String.format("%d金 %d银 %d铜", gold, silver, copper);
    }

    /**
     * 将铜币数量分解为金/银/铜
     *
     * @param copperAmount 铜币数量
     * @return 包含金/银/铜数量的数组 [金, 银, 铜]
     */
    public static int[] breakdown(int copperAmount) {
        if (copperAmount < 0) {
            return new int[]{0, 0, 0};
        }

        int gold = copperAmount / COPPER_PER_GOLD;
        int remaining = copperAmount % COPPER_PER_GOLD;
        int silver = remaining / COPPER_PER_SILVER;
        int copper = remaining % COPPER_PER_SILVER;

        return new int[]{gold, silver, copper};
    }

    /**
     * 将金/银/铜转换为铜币总数
     *
     * @param gold 金币数量
     * @param silver 银币数量
     * @param copper 铜币数量
     * @return 铜币总数
     */
    public static int toCopper(int gold, int silver, int copper) {
        return gold * COPPER_PER_GOLD + silver * COPPER_PER_SILVER + copper;
    }
}
