package com.heibai.clawworld.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "role_configs")
public class RoleConfig {
    @Id
    private String id;
    private String name;
    private String description;
    private int baseHealth;
    private int baseMana;
    private int baseStrength;
    private int baseAgility;
    private int baseIntelligence;
    private int baseVitality;
    private double healthPerLevel;
    private double manaPerLevel;
    private double strengthPerLevel;
    private double agilityPerLevel;
    private double intelligencePerLevel;
    private double vitalityPerLevel;
    private List<SkillLearn> skillLearns;

    @Data
    public static class SkillLearn {
        private String skillId;
        private int learnLevel;
    }
}
