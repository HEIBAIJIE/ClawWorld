package com.heibai.clawworld.integration;

import com.heibai.clawworld.application.impl.AuthService;
import com.heibai.clawworld.application.service.MapEntityService;
import com.heibai.clawworld.application.service.PlayerSessionService;
import com.heibai.clawworld.infrastructure.persistence.repository.AccountRepository;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 游戏流程集成测试
 * 测试从注册到游戏的完整流程
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("游戏流程集成测试")
class GameFlowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private PlayerSessionService playerSessionService;

    @Autowired
    private MapEntityService mapEntityService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private static String sessionId;
    private static String playerId;

    @BeforeEach
    void setUp() {
        // 每个测试前清理数据,确保测试独立
        try {
            accountRepository.deleteAll();
            playerRepository.deleteAll();
        } catch (Exception e) {
            // 忽略清理错误
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 新用户注册流程")
    void testNewUserRegistration() {
        // 1. 登录(自动注册)
        AuthService.LoginResult loginResult = authService.loginOrRegister("testuser", "testpass");

        assertTrue(loginResult.isSuccess(), "登录应该成功");
        assertNotNull(loginResult.getSessionId(), "应该返回sessionId");
        assertNotNull(loginResult.getContent(), "应该返回背景内容");
        assertTrue(loginResult.isNewUser(), "应该是新用户");

        sessionId = loginResult.getSessionId();

        // 2. 注册角色
        PlayerSessionService.SessionResult registerResult =
            playerSessionService.registerPlayer(sessionId, "战士", "测试战士");

        assertTrue(registerResult.isSuccess(), "注册应该成功");
        assertNotNull(registerResult.getPlayerId(), "应该返回playerId");
        assertNotNull(registerResult.getWindowContent(), "应该返回窗口内容");

        playerId = registerResult.getPlayerId();
    }

    @Test
    @Order(2)
    @DisplayName("2. 老用户登录流程")
    void testExistingUserLogin() {
        // 先注册一个用户并创建角色
        AuthService.LoginResult registerResult = authService.loginOrRegister("existinguser", "password");
        assertTrue(registerResult.isSuccess());
        assertTrue(registerResult.isNewUser(), "第一次应该是新用户");

        // 创建角色
        PlayerSessionService.SessionResult createResult =
            playerSessionService.registerPlayer(registerResult.getSessionId(), "战士", "老玩家");
        assertTrue(createResult.isSuccess(), "创建角色应该成功");

        // 登出
        authService.logout(registerResult.getSessionId());

        // 再次登录
        AuthService.LoginResult loginResult = authService.loginOrRegister("existinguser", "password");

        assertTrue(loginResult.isSuccess(), "登录应该成功");
        assertNotNull(loginResult.getSessionId(), "应该返回sessionId");
        assertFalse(loginResult.isNewUser(), "不应该是新用户,因为已经创建了角色");
    }

    @Test
    @Order(3)
    @DisplayName("3. 错误密码登录")
    void testWrongPasswordLogin() {
        // 先注册一个用户
        authService.loginOrRegister("user1", "correctpass");

        // 使用错误密码登录
        AuthService.LoginResult loginResult = authService.loginOrRegister("user1", "wrongpass");

        assertFalse(loginResult.isSuccess(), "登录应该失败");
        assertEquals("密码错误", loginResult.getMessage());
        assertNull(loginResult.getSessionId(), "不应该返回sessionId");
    }

    @Test
    @Order(4)
    @DisplayName("4. 完整游戏流程 - 注册到移动")
    void testCompleteGameFlow() {
        // 1. 注册并登录
        AuthService.LoginResult loginResult = authService.loginOrRegister("player1", "pass1");
        assertTrue(loginResult.isSuccess());
        String sid = loginResult.getSessionId();

        // 2. 注册角色
        PlayerSessionService.SessionResult registerResult =
            playerSessionService.registerPlayer(sid, "战士", "勇士一号");
        assertTrue(registerResult.isSuccess());
        String pid = registerResult.getPlayerId();

        // 3. 加属性点
        PlayerSessionService.OperationResult attrResult =
            playerSessionService.addAttribute(pid, "str", 3);
        assertTrue(attrResult.isSuccess(), "加点应该成功");

        // 4. 移动
        MapEntityService.MoveResult moveResult = mapEntityService.movePlayer(pid, 6, 6);
        assertTrue(moveResult.isSuccess(), "移动应该成功");
        assertEquals(6, moveResult.getCurrentX());
        assertEquals(6, moveResult.getCurrentY());

        // 5. 等待
        PlayerSessionService.OperationResult waitResult = playerSessionService.wait(pid, 5);
        assertTrue(waitResult.isSuccess(), "等待应该成功");

        // 6. 下线
        PlayerSessionService.OperationResult logoutResult = playerSessionService.logout(sid);
        assertTrue(logoutResult.isSuccess(), "下线应该成功");
    }

    @Test
    @Order(5)
    @DisplayName("5. 昵称唯一性测试")
    void testNicknameUniqueness() {
        // 1. 第一个用户注册
        AuthService.LoginResult login1 = authService.loginOrRegister("user1", "pass1");
        PlayerSessionService.SessionResult register1 =
            playerSessionService.registerPlayer(login1.getSessionId(), "战士", "独特昵称");
        assertTrue(register1.isSuccess());

        // 2. 第二个用户尝试使用相同昵称
        AuthService.LoginResult login2 = authService.loginOrRegister("user2", "pass2");
        PlayerSessionService.SessionResult register2 =
            playerSessionService.registerPlayer(login2.getSessionId(), "法师", "独特昵称");
        assertFalse(register2.isSuccess(), "应该失败,昵称已被使用");
        assertEquals("昵称已被使用", register2.getMessage());
    }

    @Test
    @Order(6)
    @DisplayName("6. 属性点分配测试")
    void testAttributeAllocation() {
        // 1. 注册用户
        AuthService.LoginResult loginResult = authService.loginOrRegister("attrtest", "pass");
        PlayerSessionService.SessionResult registerResult =
            playerSessionService.registerPlayer(loginResult.getSessionId(), "战士", "属性测试");
        String pid = registerResult.getPlayerId();

        // 2. 分配属性点 (初始5点)
        PlayerSessionService.OperationResult result1 = playerSessionService.addAttribute(pid, "str", 2);
        assertTrue(result1.isSuccess());

        PlayerSessionService.OperationResult result2 = playerSessionService.addAttribute(pid, "agi", 2);
        assertTrue(result2.isSuccess());

        PlayerSessionService.OperationResult result3 = playerSessionService.addAttribute(pid, "vit", 1);
        assertTrue(result3.isSuccess());

        // 3. 尝试分配超过剩余点数
        PlayerSessionService.OperationResult result4 = playerSessionService.addAttribute(pid, "int", 1);
        assertFalse(result4.isSuccess(), "应该失败,属性点不足");
        assertEquals("可用属性点不足", result4.getMessage());
    }

    @Test
    @Order(7)
    @DisplayName("7. 无效职业注册测试")
    void testInvalidRoleRegistration() {
        AuthService.LoginResult loginResult = authService.loginOrRegister("invalidrole", "pass");
        PlayerSessionService.SessionResult registerResult =
            playerSessionService.registerPlayer(loginResult.getSessionId(), "无效职业", "测试");

        assertFalse(registerResult.isSuccess(), "应该失败,职业不存在");
        assertEquals("职业不存在", registerResult.getMessage());
    }

    @Test
    @Order(8)
    @DisplayName("8. 移动边界测试")
    void testMovementBoundaries() {
        // 1. 注册用户
        AuthService.LoginResult loginResult = authService.loginOrRegister("movetest", "pass");
        PlayerSessionService.SessionResult registerResult =
            playerSessionService.registerPlayer(loginResult.getSessionId(), "游侠", "移动测试");
        String pid = registerResult.getPlayerId();

        // 2. 尝试移动到负坐标
        MapEntityService.MoveResult result1 = mapEntityService.movePlayer(pid, -1, 5);
        assertFalse(result1.isSuccess(), "应该失败,坐标为负数");

        // 3. 尝试移动到超出地图范围的坐标
        MapEntityService.MoveResult result2 = mapEntityService.movePlayer(pid, 1000, 1000);
        assertFalse(result2.isSuccess(), "应该失败,超出地图范围");
    }

    @Test
    @Order(9)
    @DisplayName("9. 等待时间边界测试")
    void testWaitTimeBoundaries() {
        // 1. 注册用户
        AuthService.LoginResult loginResult = authService.loginOrRegister("waittest", "pass");
        PlayerSessionService.SessionResult registerResult =
            playerSessionService.registerPlayer(loginResult.getSessionId(), "牧师", "等待测试");
        String pid = registerResult.getPlayerId();

        // 2. 测试有效等待时间
        PlayerSessionService.OperationResult result1 = playerSessionService.wait(pid, 1);
        assertTrue(result1.isSuccess(), "1秒应该有效");

        PlayerSessionService.OperationResult result2 = playerSessionService.wait(pid, 60);
        assertTrue(result2.isSuccess(), "60秒应该有效");

        // 3. 测试无效等待时间
        PlayerSessionService.OperationResult result3 = playerSessionService.wait(pid, 0);
        assertFalse(result3.isSuccess(), "0秒应该无效");

        PlayerSessionService.OperationResult result4 = playerSessionService.wait(pid, 61);
        assertFalse(result4.isSuccess(), "61秒应该无效");
    }

    @Test
    @Order(10)
    @DisplayName("10. 会话管理测试")
    void testSessionManagement() {
        // 1. 登录
        AuthService.LoginResult loginResult = authService.loginOrRegister("sessiontest", "pass");
        String sid = loginResult.getSessionId();

        // 2. 验证会话有效
        var account = authService.getAccountBySessionId(sid);
        assertTrue(account.isPresent(), "会话应该有效");

        // 3. 下线
        authService.logout(sid);

        // 4. 验证会话失效
        var accountAfterLogout = authService.getAccountBySessionId(sid);
        assertFalse(accountAfterLogout.isPresent(), "会话应该失效");
    }
}
