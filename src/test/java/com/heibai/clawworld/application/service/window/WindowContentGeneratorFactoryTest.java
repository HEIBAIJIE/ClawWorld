package com.heibai.clawworld.application.service.window;

import com.heibai.clawworld.interfaces.command.CommandContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * WindowContentGeneratorFactory 单元测试
 */
@ExtendWith(MockitoExtension.class)
class WindowContentGeneratorFactoryTest {

    @Mock
    private WindowContentGenerator registerGenerator;

    @Mock
    private WindowContentGenerator mapGenerator;

    @Mock
    private WindowContentGenerator combatGenerator;

    @Mock
    private WindowContentGenerator tradeGenerator;

    @Mock
    private WindowContentGenerator shopGenerator;

    @InjectMocks
    private WindowContentGeneratorFactory factory;

    @Test
    void testGetRegisterGenerator() {
        Map<String, WindowContentGenerator> generators = new HashMap<>();
        generators.put("registerWindowContentGenerator", registerGenerator);
        generators.put("mapWindowContentGenerator", mapGenerator);
        generators.put("combatWindowContentGenerator", combatGenerator);
        generators.put("tradeWindowContentGenerator", tradeGenerator);
        generators.put("shopWindowContentGenerator", shopGenerator);

        WindowContentGeneratorFactory factory = new WindowContentGeneratorFactory(generators);

        WindowContentGenerator result = factory.getGenerator(CommandContext.WindowType.REGISTER);
        assertSame(registerGenerator, result);
    }

    @Test
    void testGetMapGenerator() {
        Map<String, WindowContentGenerator> generators = new HashMap<>();
        generators.put("registerWindowContentGenerator", registerGenerator);
        generators.put("mapWindowContentGenerator", mapGenerator);
        generators.put("combatWindowContentGenerator", combatGenerator);
        generators.put("tradeWindowContentGenerator", tradeGenerator);
        generators.put("shopWindowContentGenerator", shopGenerator);

        WindowContentGeneratorFactory factory = new WindowContentGeneratorFactory(generators);

        WindowContentGenerator result = factory.getGenerator(CommandContext.WindowType.MAP);
        assertSame(mapGenerator, result);
    }

    @Test
    void testGetCombatGenerator() {
        Map<String, WindowContentGenerator> generators = new HashMap<>();
        generators.put("registerWindowContentGenerator", registerGenerator);
        generators.put("mapWindowContentGenerator", mapGenerator);
        generators.put("combatWindowContentGenerator", combatGenerator);
        generators.put("tradeWindowContentGenerator", tradeGenerator);
        generators.put("shopWindowContentGenerator", shopGenerator);

        WindowContentGeneratorFactory factory = new WindowContentGeneratorFactory(generators);

        WindowContentGenerator result = factory.getGenerator(CommandContext.WindowType.COMBAT);
        assertSame(combatGenerator, result);
    }

    @Test
    void testGetTradeGenerator() {
        Map<String, WindowContentGenerator> generators = new HashMap<>();
        generators.put("registerWindowContentGenerator", registerGenerator);
        generators.put("mapWindowContentGenerator", mapGenerator);
        generators.put("combatWindowContentGenerator", combatGenerator);
        generators.put("tradeWindowContentGenerator", tradeGenerator);
        generators.put("shopWindowContentGenerator", shopGenerator);

        WindowContentGeneratorFactory factory = new WindowContentGeneratorFactory(generators);

        WindowContentGenerator result = factory.getGenerator(CommandContext.WindowType.TRADE);
        assertSame(tradeGenerator, result);
    }

    @Test
    void testGetShopGenerator() {
        Map<String, WindowContentGenerator> generators = new HashMap<>();
        generators.put("registerWindowContentGenerator", registerGenerator);
        generators.put("mapWindowContentGenerator", mapGenerator);
        generators.put("combatWindowContentGenerator", combatGenerator);
        generators.put("tradeWindowContentGenerator", tradeGenerator);
        generators.put("shopWindowContentGenerator", shopGenerator);

        WindowContentGeneratorFactory factory = new WindowContentGeneratorFactory(generators);

        WindowContentGenerator result = factory.getGenerator(CommandContext.WindowType.SHOP);
        assertSame(shopGenerator, result);
    }

    @Test
    void testGetGeneratorThrowsExceptionForMissingGenerator() {
        Map<String, WindowContentGenerator> generators = new HashMap<>();
        WindowContentGeneratorFactory factory = new WindowContentGeneratorFactory(generators);

        assertThrows(IllegalArgumentException.class, () -> {
            factory.getGenerator(CommandContext.WindowType.REGISTER);
        });
    }
}
