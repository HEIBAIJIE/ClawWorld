package com.heibai.clawworld.service;

import com.heibai.clawworld.model.*;
import com.heibai.clawworld.util.CsvReader;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ConfigDataManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigDataManager.class);
    private final CsvReader csvReader;
    private final ResourceLoader resourceLoader;

    private final Map<String, ItemConfig> itemConfigs = new ConcurrentHashMap<>();
    private final Map<String, EquipmentConfig> equipmentConfigs = new ConcurrentHashMap<>();
    private final Map<String, SkillConfig> skillConfigs = new ConcurrentHashMap<>();
    private final Map<String, EnemyConfig> enemyConfigs = new ConcurrentHashMap<>();
    private final Map<String, NpcConfig> npcConfigs = new ConcurrentHashMap<>();
    private final Map<String, MapConfig> mapConfigs = new ConcurrentHashMap<>();
    private final Map<String, RoleConfig> roleConfigs = new ConcurrentHashMap<>();

    private final Map<String, String> fileChecksums = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing ConfigDataManager...");
        loadAllConfigs();
    }

    @Scheduled(fixedDelayString = "${csv.reload-interval:60000}")
    public void checkAndReload() {
        log.debug("Checking for CSV file changes...");
        try {
            if (hasFileChanged("classpath:data/items.csv") ||
                hasFileChanged("classpath:data/equipment.csv") ||
                hasFileChanged("classpath:data/skills.csv") ||
                hasFileChanged("classpath:data/enemies.csv") ||
                hasFileChanged("classpath:data/npcs.csv") ||
                hasFileChanged("classpath:data/maps.csv") ||
                hasFileChanged("classpath:data/roles.csv") ||
                hasFileChanged("classpath:data/role_skills.csv")) {

                log.info("CSV files changed, reloading...");
                loadAllConfigs();
            }
        } catch (Exception e) {
            log.error("Error checking file changes", e);
        }
    }

    private boolean hasFileChanged(String path) throws Exception {
        Resource resource = resourceLoader.getResource(path);
        if (!resource.exists()) {
            return false;
        }

        String checksum = calculateChecksum(resource.getInputStream());
        String oldChecksum = fileChecksums.get(path);

        if (!checksum.equals(oldChecksum)) {
            fileChecksums.put(path, checksum);
            return true;
        }
        return false;
    }

    private String calculateChecksum(InputStream inputStream) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) > 0) {
            md.update(buffer, 0, read);
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void loadAllConfigs() {
        loadItems();
        loadEquipment();
        loadSkills();
        loadEnemies();
        loadNpcs();
        loadMaps();
        loadRoles();
        loadRoleSkills();
        log.info("All configs loaded successfully");
    }

    private void loadItems() {
        try {
            Resource resource = resourceLoader.getResource("classpath:data/items.csv");
            if (!resource.exists()) {
                log.warn("items.csv not found, skipping");
                return;
            }

            List<ItemConfig> items = csvReader.readCsv(resource.getInputStream(), record -> {
                ItemConfig item = new ItemConfig();
                item.setId(csvReader.getString(record, "id"));
                item.setName(csvReader.getString(record, "name"));
                item.setDescription(csvReader.getString(record, "description"));
                item.setType(ItemConfig.ItemType.valueOf(csvReader.getString(record, "type")));
                item.setMaxStack(csvReader.getInt(record, "maxStack"));
                item.setBasePrice(csvReader.getInt(record, "basePrice"));
                item.setEffect(csvReader.getString(record, "effect"));
                item.setEffectValue(csvReader.getIntOrNull(record, "effectValue"));
                return item;
            });

            itemConfigs.clear();
            items.forEach(item -> itemConfigs.put(item.getId(), item));
            log.info("Loaded {} items", items.size());
        } catch (IOException e) {
            log.error("Error loading items.csv", e);
        }
    }

    private void loadEquipment() {
        try {
            Resource resource = resourceLoader.getResource("classpath:data/equipment.csv");
            if (!resource.exists()) {
                log.warn("equipment.csv not found, skipping");
                return;
            }

            List<EquipmentConfig> equipment = csvReader.readCsv(resource.getInputStream(), record -> {
                EquipmentConfig eq = new EquipmentConfig();
                eq.setId(csvReader.getString(record, "id"));
                eq.setName(csvReader.getString(record, "name"));
                eq.setDescription(csvReader.getString(record, "description"));
                eq.setSlot(EquipmentConfig.EquipmentSlot.valueOf(csvReader.getString(record, "slot")));
                eq.setRarity(Rarity.valueOf(csvReader.getString(record, "rarity")));
                eq.setStrength(csvReader.getInt(record, "strength"));
                eq.setAgility(csvReader.getInt(record, "agility"));
                eq.setIntelligence(csvReader.getInt(record, "intelligence"));
                eq.setVitality(csvReader.getInt(record, "vitality"));
                eq.setPhysicalAttack(csvReader.getInt(record, "physicalAttack"));
                eq.setPhysicalDefense(csvReader.getInt(record, "physicalDefense"));
                eq.setMagicAttack(csvReader.getInt(record, "magicAttack"));
                eq.setMagicDefense(csvReader.getInt(record, "magicDefense"));
                eq.setSpeed(csvReader.getInt(record, "speed"));
                eq.setCritRate(csvReader.getDouble(record, "critRate"));
                eq.setCritDamage(csvReader.getDouble(record, "critDamage"));
                eq.setHitRate(csvReader.getDouble(record, "hitRate"));
                eq.setDodgeRate(csvReader.getDouble(record, "dodgeRate"));
                return eq;
            });

            equipmentConfigs.clear();
            equipment.forEach(eq -> equipmentConfigs.put(eq.getId(), eq));
            log.info("Loaded {} equipment", equipment.size());
        } catch (IOException e) {
            log.error("Error loading equipment.csv", e);
        }
    }

    private void loadSkills() {
        try {
            Resource resource = resourceLoader.getResource("classpath:data/skills.csv");
            if (!resource.exists()) {
                log.warn("skills.csv not found, skipping");
                return;
            }

            List<SkillConfig> skills = csvReader.readCsv(resource.getInputStream(), record -> {
                SkillConfig skill = new SkillConfig();
                skill.setId(csvReader.getString(record, "id"));
                skill.setName(csvReader.getString(record, "name"));
                skill.setDescription(csvReader.getString(record, "description"));
                skill.setTargetType(SkillConfig.SkillTarget.valueOf(csvReader.getString(record, "targetType")));
                skill.setDamageType(SkillConfig.DamageType.valueOf(csvReader.getString(record, "damageType")));
                skill.setManaCost(csvReader.getInt(record, "manaCost"));
                skill.setCooldown(csvReader.getInt(record, "cooldown"));
                skill.setDamageMultiplier(csvReader.getDouble(record, "damageMultiplier"));
                return skill;
            });

            skillConfigs.clear();
            skills.forEach(skill -> skillConfigs.put(skill.getId(), skill));
            log.info("Loaded {} skills", skills.size());
        } catch (IOException e) {
            log.error("Error loading skills.csv", e);
        }
    }

    private void loadEnemies() {
        try {
            Resource resource = resourceLoader.getResource("classpath:data/enemies.csv");
            if (!resource.exists()) {
                log.warn("enemies.csv not found, skipping");
                return;
            }

            List<EnemyConfig> enemies = csvReader.readCsv(resource.getInputStream(), record -> {
                EnemyConfig enemy = new EnemyConfig();
                enemy.setId(csvReader.getString(record, "id"));
                enemy.setName(csvReader.getString(record, "name"));
                enemy.setDescription(csvReader.getString(record, "description"));
                enemy.setLevel(csvReader.getInt(record, "level"));
                enemy.setTier(EnemyConfig.EnemyTier.valueOf(csvReader.getString(record, "tier")));
                enemy.setHealth(csvReader.getInt(record, "health"));
                enemy.setMana(csvReader.getInt(record, "mana"));
                enemy.setPhysicalAttack(csvReader.getInt(record, "physicalAttack"));
                enemy.setPhysicalDefense(csvReader.getInt(record, "physicalDefense"));
                enemy.setMagicAttack(csvReader.getInt(record, "magicAttack"));
                enemy.setMagicDefense(csvReader.getInt(record, "magicDefense"));
                enemy.setSpeed(csvReader.getInt(record, "speed"));
                enemy.setCritRate(csvReader.getDouble(record, "critRate"));
                enemy.setCritDamage(csvReader.getDouble(record, "critDamage"));
                enemy.setHitRate(csvReader.getDouble(record, "hitRate"));
                enemy.setDodgeRate(csvReader.getDouble(record, "dodgeRate"));

                String skills = csvReader.getString(record, "skills");
                enemy.setSkills(skills.isEmpty() ? List.of() : Arrays.asList(skills.split(";")));

                enemy.setExpMin(csvReader.getInt(record, "expMin"));
                enemy.setExpMax(csvReader.getInt(record, "expMax"));
                enemy.setGoldMin(csvReader.getInt(record, "goldMin"));
                enemy.setGoldMax(csvReader.getInt(record, "goldMax"));
                enemy.setRespawnSeconds(csvReader.getInt(record, "respawnSeconds"));
                enemy.setLootTable(new ArrayList<>());
                return enemy;
            });

            enemyConfigs.clear();
            enemies.forEach(enemy -> enemyConfigs.put(enemy.getId(), enemy));
            log.info("Loaded {} enemies", enemies.size());
        } catch (IOException e) {
            log.error("Error loading enemies.csv", e);
        }
    }

    private void loadNpcs() {
        try {
            Resource resource = resourceLoader.getResource("classpath:data/npcs.csv");
            if (!resource.exists()) {
                log.warn("npcs.csv not found, skipping");
                return;
            }

            List<NpcConfig> npcs = csvReader.readCsv(resource.getInputStream(), record -> {
                NpcConfig npc = new NpcConfig();
                npc.setId(csvReader.getString(record, "id"));
                npc.setName(csvReader.getString(record, "name"));
                npc.setDescription(csvReader.getString(record, "description"));
                npc.setHasShop(csvReader.getBoolean(record, "hasShop"));
                npc.setHasDialogue(csvReader.getBoolean(record, "hasDialogue"));

                String dialogues = csvReader.getString(record, "dialogues");
                npc.setDialogues(dialogues.isEmpty() ? List.of() : Arrays.asList(dialogues.split(";")));

                npc.setShopItems(new ArrayList<>());
                npc.setShopGold(csvReader.getInt(record, "shopGold"));
                npc.setShopRefreshSeconds(csvReader.getInt(record, "shopRefreshSeconds"));
                npc.setPriceMultiplier(csvReader.getDouble(record, "priceMultiplier"));
                return npc;
            });

            npcConfigs.clear();
            npcs.forEach(npc -> npcConfigs.put(npc.getId(), npc));
            log.info("Loaded {} NPCs", npcs.size());
        } catch (IOException e) {
            log.error("Error loading npcs.csv", e);
        }
    }

    private void loadMaps() {
        try {
            Resource resource = resourceLoader.getResource("classpath:data/maps.csv");
            if (!resource.exists()) {
                log.warn("maps.csv not found, skipping");
                return;
            }

            List<MapConfig> maps = csvReader.readCsv(resource.getInputStream(), record -> {
                MapConfig map = new MapConfig();
                map.setId(csvReader.getString(record, "id"));
                map.setName(csvReader.getString(record, "name"));
                map.setDescription(csvReader.getString(record, "description"));
                map.setWidth(csvReader.getInt(record, "width"));
                map.setHeight(csvReader.getInt(record, "height"));
                map.setSafe(csvReader.getBoolean(record, "isSafe"));
                map.setRecommendedLevel(csvReader.getIntOrNull(record, "recommendedLevel"));
                map.setTerrain(new ArrayList<>());
                map.setEntities(new ArrayList<>());
                return map;
            });

            mapConfigs.clear();
            maps.forEach(map -> mapConfigs.put(map.getId(), map));
            log.info("Loaded {} maps", maps.size());
        } catch (IOException e) {
            log.error("Error loading maps.csv", e);
        }
    }

    private void loadRoles() {
        try {
            Resource resource = resourceLoader.getResource("classpath:data/roles.csv");
            if (!resource.exists()) {
                log.warn("roles.csv not found, skipping");
                return;
            }

            List<RoleConfig> roles = csvReader.readCsv(resource.getInputStream(), record -> {
                RoleConfig role = new RoleConfig();
                role.setId(csvReader.getString(record, "id"));
                role.setName(csvReader.getString(record, "name"));
                role.setDescription(csvReader.getString(record, "description"));
                role.setBaseHealth(csvReader.getInt(record, "baseHealth"));
                role.setBaseMana(csvReader.getInt(record, "baseMana"));
                role.setBaseStrength(csvReader.getInt(record, "baseStrength"));
                role.setBaseAgility(csvReader.getInt(record, "baseAgility"));
                role.setBaseIntelligence(csvReader.getInt(record, "baseIntelligence"));
                role.setBaseVitality(csvReader.getInt(record, "baseVitality"));
                role.setHealthPerLevel(csvReader.getDouble(record, "healthPerLevel"));
                role.setManaPerLevel(csvReader.getDouble(record, "manaPerLevel"));
                role.setStrengthPerLevel(csvReader.getDouble(record, "strengthPerLevel"));
                role.setAgilityPerLevel(csvReader.getDouble(record, "agilityPerLevel"));
                role.setIntelligencePerLevel(csvReader.getDouble(record, "intelligencePerLevel"));
                role.setVitalityPerLevel(csvReader.getDouble(record, "vitalityPerLevel"));
                role.setSkillLearns(new ArrayList<>());
                return role;
            });

            roleConfigs.clear();
            roles.forEach(role -> roleConfigs.put(role.getId(), role));
            log.info("Loaded {} roles", roles.size());
        } catch (IOException e) {
            log.error("Error loading roles.csv", e);
        }
    }

    private void loadRoleSkills() {
        try {
            Resource resource = resourceLoader.getResource("classpath:data/role_skills.csv");
            if (!resource.exists()) {
                log.warn("role_skills.csv not found, skipping");
                return;
            }

            csvReader.readCsv(resource.getInputStream(), record -> {
                String roleId = csvReader.getString(record, "roleId");
                String skillId = csvReader.getString(record, "skillId");
                int learnLevel = csvReader.getInt(record, "learnLevel");

                RoleConfig role = roleConfigs.get(roleId);
                if (role != null) {
                    RoleConfig.SkillLearn skillLearn = new RoleConfig.SkillLearn();
                    skillLearn.setSkillId(skillId);
                    skillLearn.setLearnLevel(learnLevel);
                    role.getSkillLearns().add(skillLearn);
                }
                return null;
            });

            log.info("Loaded role skills mappings");
        } catch (IOException e) {
            log.error("Error loading role_skills.csv", e);
        }
    }

    public ItemConfig getItem(String id) {
        return itemConfigs.get(id);
    }

    public EquipmentConfig getEquipment(String id) {
        return equipmentConfigs.get(id);
    }

    public SkillConfig getSkill(String id) {
        return skillConfigs.get(id);
    }

    public EnemyConfig getEnemy(String id) {
        return enemyConfigs.get(id);
    }

    public NpcConfig getNpc(String id) {
        return npcConfigs.get(id);
    }

    public MapConfig getMap(String id) {
        return mapConfigs.get(id);
    }

    public RoleConfig getRole(String id) {
        return roleConfigs.get(id);
    }

    public Collection<ItemConfig> getAllItems() {
        return itemConfigs.values();
    }

    public Collection<EquipmentConfig> getAllEquipment() {
        return equipmentConfigs.values();
    }

    public Collection<SkillConfig> getAllSkills() {
        return skillConfigs.values();
    }

    public Collection<EnemyConfig> getAllEnemies() {
        return enemyConfigs.values();
    }

    public Collection<NpcConfig> getAllNpcs() {
        return npcConfigs.values();
    }

    public Collection<MapConfig> getAllMaps() {
        return mapConfigs.values();
    }

    public Collection<RoleConfig> getAllRoles() {
        return roleConfigs.values();
    }
}
