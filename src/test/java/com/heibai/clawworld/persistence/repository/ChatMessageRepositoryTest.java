package com.heibai.clawworld.persistence.repository;

import com.heibai.clawworld.persistence.entity.ChatMessageEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 聊天记录Repository单元测试
 */
@DataMongoTest
class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private ChatMessageEntity testMessage;

    @BeforeEach
    void setUp() {
        testMessage = new ChatMessageEntity();
        testMessage.setChannelType(ChatMessageEntity.ChannelType.WORLD);
        testMessage.setSenderId("player1");
        testMessage.setSenderNickname("玩家1");
        testMessage.setMessage("测试消息");
        testMessage.setTimestamp(System.currentTimeMillis());
    }

    @AfterEach
    void tearDown() {
        chatMessageRepository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        ChatMessageEntity saved = chatMessageRepository.save(testMessage);
        assertNotNull(saved.getId());

        assertTrue(chatMessageRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testFindByChannelType() {
        chatMessageRepository.save(testMessage);

        List<ChatMessageEntity> messages =
            chatMessageRepository.findByChannelTypeOrderByTimestampDesc(
                ChatMessageEntity.ChannelType.WORLD);
        assertFalse(messages.isEmpty());
        assertEquals("测试消息", messages.get(0).getMessage());
    }

    @Test
    void testFindByChannelTypeAndMapId() {
        testMessage.setChannelType(ChatMessageEntity.ChannelType.MAP);
        testMessage.setMapId("test_map");
        chatMessageRepository.save(testMessage);

        List<ChatMessageEntity> messages =
            chatMessageRepository.findByChannelTypeAndMapIdOrderByTimestampDesc(
                ChatMessageEntity.ChannelType.MAP, "test_map");
        assertEquals(1, messages.size());
    }

    @Test
    void testFindByTimestampAfter() {
        long now = System.currentTimeMillis();
        testMessage.setTimestamp(now);
        chatMessageRepository.save(testMessage);

        // 查找1分钟前之后的消息
        List<ChatMessageEntity> messages =
            chatMessageRepository.findByTimestampAfterOrderByTimestampAsc(now - 60000);
        assertFalse(messages.isEmpty());
    }

    @Test
    void testMultipleChannelTypes() {
        // 世界频道消息
        chatMessageRepository.save(testMessage);

        // 地图频道消息
        ChatMessageEntity mapMessage = new ChatMessageEntity();
        mapMessage.setChannelType(ChatMessageEntity.ChannelType.MAP);
        mapMessage.setMapId("test_map");
        mapMessage.setSenderId("player1");
        mapMessage.setSenderNickname("玩家1");
        mapMessage.setMessage("地图消息");
        mapMessage.setTimestamp(System.currentTimeMillis());
        chatMessageRepository.save(mapMessage);

        // 验证不同频道的消息
        List<ChatMessageEntity> worldMessages =
            chatMessageRepository.findByChannelTypeOrderByTimestampDesc(
                ChatMessageEntity.ChannelType.WORLD);
        assertEquals(1, worldMessages.size());

        List<ChatMessageEntity> mapMessages =
            chatMessageRepository.findByChannelTypeAndMapIdOrderByTimestampDesc(
                ChatMessageEntity.ChannelType.MAP, "test_map");
        assertEquals(1, mapMessages.size());
    }
}
