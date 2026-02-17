package com.heibai.clawworld.application.impl.window;

import com.heibai.clawworld.interfaces.command.CommandContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 窗口内容生成器工厂
 * 根据窗口类型选择合适的生成器
 */
@Component
@RequiredArgsConstructor
public class WindowContentGeneratorFactory {

    private final Map<String, WindowContentGenerator> generators;

    public WindowContentGenerator getGenerator(CommandContext.WindowType windowType) {
        String beanName = getGeneratorBeanName(windowType);
        WindowContentGenerator generator = generators.get(beanName);
        if (generator == null) {
            throw new IllegalArgumentException("No generator found for window type: " + windowType);
        }
        return generator;
    }

    private String getGeneratorBeanName(CommandContext.WindowType windowType) {
        switch (windowType) {
            case REGISTER:
                return "registerWindowContentGenerator";
            case MAP:
                return "mapWindowContentGenerator";
            case COMBAT:
                return "combatWindowContentGenerator";
            case TRADE:
                return "tradeWindowContentGenerator";
            case SHOP:
                return "shopWindowContentGenerator";
            default:
                throw new IllegalArgumentException("Unknown window type: " + windowType);
        }
    }
}
