package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.domain.chat.ChatMessage;
import com.heibai.clawworld.infrastructure.persistence.entity.AccountEntity;
import com.heibai.clawworld.infrastructure.persistence.entity.ChatMessageEntity;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.mapper.ChatMapper;
import com.heibai.clawworld.infrastructure.persistence.repository.AccountRepository;
import com.heibai.clawworld.infrastructure.persistence.repository.ChatMessageRepository;
import com.heibai.clawworld.infrastructure.persistence.repository.PartyRepository;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import com.heibai.clawworld.application.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private ChatMapper chatMapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    private PlayerEntity testPlayer;
    private AccountEntity testAccount;

    @BeforeEach
    void setUp() {
        testPlayer = new PlayerEntity();
        testPlayer.setId("player1");
        testPlayer.setCurrentMapId("map1");
        testPlayer.setPartyId("party1");

        testAccount = new AccountEntity();
        testAccount.setId("account1");
        testAccount.setPlayerId("player1");
        testAccount.setNickname("TestPlayer");
    }

    @Test
    void testSendWorldMessage_Success() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));
        when(accountRepository.findByPlayerId("player1")).thenReturn(Optional.of(testAccount));
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ChatService.ChatResult result = chatService.sendWorldMessage("player1", "Hello World");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("世界频道消息发送成功", result.getMessage());
        verify(chatMessageRepository).save(any(ChatMessageEntity.class));
    }

    @Test
    void testSendWorldMessage_MessageTooLong() {
        // Arrange
        String longMessage = "a".repeat(31);

        // Act
        ChatService.ChatResult result = chatService.sendWorldMessage("player1", longMessage);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("消息长度不能超过30字", result.getMessage());
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    void testSendMapMessage_Success() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));
        when(accountRepository.findByPlayerId("player1")).thenReturn(Optional.of(testAccount));
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ChatService.ChatResult result = chatService.sendMapMessage("player1", "Hello Map");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("地图频道消息发送成功", result.getMessage());
        verify(chatMessageRepository).save(any(ChatMessageEntity.class));
    }

    @Test
    void testSendPartyMessage_Success() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));
        when(accountRepository.findByPlayerId("player1")).thenReturn(Optional.of(testAccount));
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ChatService.ChatResult result = chatService.sendPartyMessage("player1", "Hello Party");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("队伍频道消息发送成功", result.getMessage());
        verify(chatMessageRepository).save(any(ChatMessageEntity.class));
    }

    @Test
    void testSendPartyMessage_NotInParty() {
        // Arrange
        testPlayer.setPartyId(null);
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));

        // Act
        ChatService.ChatResult result = chatService.sendPartyMessage("player1", "Hello Party");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("玩家不在任何队伍中", result.getMessage());
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    void testSendPrivateMessage_Success() {
        // Arrange
        AccountEntity receiverAccount = new AccountEntity();
        receiverAccount.setId("account2");
        receiverAccount.setPlayerId("player2");
        receiverAccount.setNickname("TargetPlayer");

        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));
        when(accountRepository.findByPlayerId("player1")).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByNickname("TargetPlayer")).thenReturn(Optional.of(receiverAccount));
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ChatService.ChatResult result = chatService.sendPrivateMessage("player1", "TargetPlayer", "Hello");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("私聊消息发送成功", result.getMessage());
        verify(chatMessageRepository).save(any(ChatMessageEntity.class));
    }

    @Test
    void testGetChatHistory_ReturnsMessages() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));
        when(chatMessageRepository.findByTimestampAfterOrderByTimestampAsc(anyLong())).thenReturn(new ArrayList<>());
        when(chatMessageRepository.findByChannelTypeAndPartyIdOrderByTimestampDesc(any(), anyString())).thenReturn(new ArrayList<>());
        when(chatMessageRepository.findByChannelTypeAndMapIdOrderByTimestampDesc(any(), anyString())).thenReturn(new ArrayList<>());
        when(chatMessageRepository.findByChannelTypeOrderByTimestampDesc(any())).thenReturn(new ArrayList<>());

        // Act
        List<ChatMessage> result = chatService.getChatHistory("player1");

        // Assert
        assertNotNull(result);
        // 由于所有消息源都返回空列表,结果应该为空
        assertTrue(result.isEmpty(), "应该返回空列表");
    }
}
