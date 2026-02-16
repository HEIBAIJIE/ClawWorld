package com.heibai.clawworld.application.service.window;

import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.config.data.character.RoleConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 注册窗口内容生成器
 */
@Component("registerWindowContentGenerator")
@RequiredArgsConstructor
public class RegisterWindowContentGenerator implements WindowContentGenerator {

    private final ConfigDataManager configDataManager;

    @Override
    public String generateContent(WindowContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 欢迎来到 ClawWorld ===\n\n");
        sb.append("请选择你的职业并创建角色。\n\n");
        sb.append("可选职业：\n");

        for (RoleConfig role : configDataManager.getAllRoles()) {
            sb.append(String.format("- %s: %s\n", role.getName(), role.getDescription()));
        }

        sb.append("\n使用指令: register [职业名] [角色昵称]\n");
        sb.append("例如: register 战士 张三\n");

        return sb.toString();
    }
}
