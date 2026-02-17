package com.heibai.clawworld.application.impl.window;

import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.mapper.PlayerMapper;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 战斗窗口内容生成器
 */
@Component("combatWindowContentGenerator")
@RequiredArgsConstructor
public class CombatWindowContentGenerator implements WindowContentGenerator {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final ConfigDataManager configDataManager;

    @Override
    public String generateContent(WindowContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 战斗 ===\n");
        sb.append("战斗ID: ").append(context.getWindowId()).append("\n\n");

        Optional<PlayerEntity> playerOpt = playerRepository.findById(context.getPlayerId());
        if (playerOpt.isPresent()) {
            PlayerEntity player = playerOpt.get();
            sb.append("--- 你的状态 ---\n");
            sb.append(String.format("生命: %d/%d\n", player.getCurrentHealth(), player.getMaxHealth()));
            sb.append(String.format("法力: %d/%d\n", player.getCurrentMana(), player.getMaxMana()));
            sb.append("\n");

            sb.append("--- 可用技能 ---\n");
            Player domainPlayer = playerMapper.toDomain(player);
            if (domainPlayer.getSkills() != null && !domainPlayer.getSkills().isEmpty()) {
                for (String skillId : domainPlayer.getSkills()) {
                    String skillName = getSkillName(skillId);
                    sb.append("- ").append(skillName).append("\n");
                }
            } else {
                sb.append("- 普通攻击\n");
            }
            sb.append("\n");
        }

        sb.append("--- 战斗信息 ---\n");
        sb.append("（战斗详细信息需要从战斗系统获取）\n\n");

        sb.append("--- 可用指令 ---\n");
        sb.append("cast [技能名] - 释放非指向技能\n");
        sb.append("cast [技能名] [目标名] - 释放指向技能\n");
        sb.append("use [物品名] - 使用物品\n");
        sb.append("wait - 空过回合\n");
        sb.append("end - 退出战斗\n");

        return sb.toString();
    }

    private String getSkillName(String skillId) {
        if (skillId == null) return "未知技能";
        if ("basic_attack".equals(skillId)) return "普通攻击";
        var skillConfig = configDataManager.getSkill(skillId);
        return skillConfig != null ? skillConfig.getName() : skillId;
    }
}
