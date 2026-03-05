package com.heibai.clawworld.domain.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 货币格式化工具类测试
 */
class CurrencyFormatterTest {

    @Test
    void testFormat() {
        // 测试基本格式化
        assertEquals("0铜", CurrencyFormatter.format(0));
        assertEquals("1铜", CurrencyFormatter.format(1));
        assertEquals("999铜", CurrencyFormatter.format(999));
        assertEquals("1银", CurrencyFormatter.format(1000));
        assertEquals("1银1铜", CurrencyFormatter.format(1001));
        assertEquals("999银999铜", CurrencyFormatter.format(999999));
        assertEquals("1金", CurrencyFormatter.format(1000000));
        assertEquals("1金1银", CurrencyFormatter.format(1001000));
        assertEquals("1金1铜", CurrencyFormatter.format(1000001));
        assertEquals("1金234银567铜", CurrencyFormatter.format(1234567));

        // 测试负数
        assertEquals("0铜", CurrencyFormatter.format(-100));
    }

    @Test
    void testFormatDetailed() {
        // 测试详细格式化（总是显示三级）
        assertEquals("0金 0银 0铜", CurrencyFormatter.formatDetailed(0));
        assertEquals("0金 0银 1铜", CurrencyFormatter.formatDetailed(1));
        assertEquals("0金 1银 0铜", CurrencyFormatter.formatDetailed(1000));
        assertEquals("1金 0银 0铜", CurrencyFormatter.formatDetailed(1000000));
        assertEquals("1金 234银 567铜", CurrencyFormatter.formatDetailed(1234567));
    }

    @Test
    void testBreakdown() {
        // 测试分解
        assertArrayEquals(new int[]{0, 0, 0}, CurrencyFormatter.breakdown(0));
        assertArrayEquals(new int[]{0, 0, 1}, CurrencyFormatter.breakdown(1));
        assertArrayEquals(new int[]{0, 1, 0}, CurrencyFormatter.breakdown(1000));
        assertArrayEquals(new int[]{1, 0, 0}, CurrencyFormatter.breakdown(1000000));
        assertArrayEquals(new int[]{1, 234, 567}, CurrencyFormatter.breakdown(1234567));
        assertArrayEquals(new int[]{0, 0, 0}, CurrencyFormatter.breakdown(-100));
    }

    @Test
    void testToCopper() {
        // 测试转换为铜币
        assertEquals(0, CurrencyFormatter.toCopper(0, 0, 0));
        assertEquals(1, CurrencyFormatter.toCopper(0, 0, 1));
        assertEquals(1000, CurrencyFormatter.toCopper(0, 1, 0));
        assertEquals(1000000, CurrencyFormatter.toCopper(1, 0, 0));
        assertEquals(1234567, CurrencyFormatter.toCopper(1, 234, 567));
    }

    @Test
    void testRoundTrip() {
        // 测试往返转换
        int[] testValues = {0, 1, 999, 1000, 1001, 999999, 1000000, 1234567, 9999999};
        for (int value : testValues) {
            int[] breakdown = CurrencyFormatter.breakdown(value);
            int reconstructed = CurrencyFormatter.toCopper(breakdown[0], breakdown[1], breakdown[2]);
            assertEquals(value, reconstructed, "Round trip failed for " + value);
        }
    }
}
