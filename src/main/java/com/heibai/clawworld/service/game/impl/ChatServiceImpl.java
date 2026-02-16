package com.heibai.clawworld.service.game.impl;

import com.heibai.clawworld.domain.chat.ChatMessage;
import com.heibai.clawworld.persistence.entity.AccountEntity;
import com.heibai.clawworld.persistence.entity.ChatMessageEntity;
import com.heibai.clawworld.persistence.entity.PlayerEntity;
import com.heibai.clawworld.persistence.repository.AccountRepository;
import com.heibai.clawworld.persistence.repository.ChatMessageRepository;
import com.heibai.clawworld.persistence.repository.PartyRepository;
import com.heibai.clawworld.persistence.repository.PlayerRepository;
import com.heibai.clawworld.service.game.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final PlayerRepository playerRepository;
    private final AccountRepository accountRepository;
    private final PartyRepository partyRepository;

    @Override
    public ChatResult sendWorldMessage(String playerId, String message) {
        // 验证消息长度
        if (message == null || message.length() > 30) {
            return ChatResult.error("消息长度不能超过30字");
        }

        // 获取玩家信息
        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return ChatResult.error("玩家不存在");
        }

        // 获取玩家昵称
        Optional<AccountEntity> accountOpt = accountRepository.findByPlayerId(playerId);
        if (!accountOpt.isPresent()) {
            return ChatResult.error("账号不存在");
        }

        // 创建聊天消息
        ChatMessageEntity chatMessage = new ChatMessageEntity();
        chatMessage.setChannelType(ChatMessageEntity.ChannelType.WORLD);
        chatMessage.setSenderId(playerId);
        chatMessage.setSenderNickname(accountOpt.get().getNickname());
        chatMessage.setMessage(message);
        chatMessage.setTimestamp(System.currentTimeMillis());

        chatMessageRepository.save(chatMessage);

        return ChatResult.success("世界频道消息发送成功");
    }

    @Override
    public ChatResult sendMapMessage(String playerId, String message) {
        // 验证消息长度
        if (message == null || message.length() > 30) {
            return ChatResult.error("消息长度不能超过30字");
        }

        // 获取玩家信息
        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return ChatResult.error("玩家不存在");
        }

        PlayerEntity player = playerOpt.get();
        if (player.getCurrentMapId() == null) {
            return ChatResult.error("玩家不在任何地图上");
        }

        // 获取玩家昵称
        Optional<AccountEntity> accountOpt = accountRepository.findByPlayerId(playerId);
        if (!accountOpt.isPresent()) {
            return ChatResult.error("账号不存在");
        }

        // 创建聊天消息
        ChatMessageEntity chatMessage = new ChatMessageEntity();
        chatMessage.setChannelType(ChatMessageEntity.ChannelType.MAP);
        chatMessage.setSenderId(playerId);
        chatMessage.setSenderNickname(accountOpt.get().getNickname());
        chatMessage.setMapId(player.getCurrentMapId());
        chatMessage.setMessage(message);
        chatMessage.setTimestamp(System.currentTimeMillis());

        chatMessageRepository.save(chatMessage);

        return ChatResult.success("地图频道消息发送成功");
    }

    @Override
    public ChatResult sendPartyMessage(String playerId, String message) {
        // 验证消息长度
        if (message == null || message.length() > 30) {
            return ChatResult.error("消息长度不能超过30字");
        }

        // 获取玩家信息
        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return ChatResult.error("玩家不存在");
        }

        PlayerEntity player = playerOpt.get();
        if (player.getPartyId() == null) {
            return ChatResult.error("玩家不在任何队伍中");
        }

        // 获取玩家昵称
        Optional<AccountEntity> accountOpt = accountRepository.findByPlayerId(playerId);
        if (!accountOpt.isPresent()) {
            return ChatResult.error("账号不存在");
        }

        // 创建聊天消息
        ChatMessageEntity chatMessage = new ChatMessageEntity();
        chatMessage.setChannelType(ChatMessageEntity.ChannelType.PARTY);
        chatMessage.setSenderId(playerId);
        chatMessage.setSenderNickname(accountOpt.get().getNickname());
        chatMessage.setPartyId(player.getPartyId());
        chatMessage.setMessage(message);
        chatMessage.setTimestamp(System.currentTimeMillis());

        chatMessageRepository.save(chatMessage);

        return ChatResult.success("队伍频道消息发送成功");
    }

    @Override
    public ChatResult sendPrivateMessage(String senderId, String targetPlayerName, String message) {
        // 验证消息长度
        if (message == null || message.length() > 30) {
            return ChatResult.error("消息长度不能超过30字");
        }

        // 获取发送者信息
        Optional<PlayerEntity> senderOpt = playerRepository.findById(senderId);
        if (!senderOpt.isPresent()) {
            return ChatResult.error("发送者不存在");
        }

        // 获取发送者昵称
        Optional<AccountEntity> senderAccountOpt = accountRepository.findByPlayerId(senderId);
        if (!senderAccountOpt.isPresent()) {
            return ChatResult.error("发送者账号不存在");
        }

        // 根据昵称查找接收者
        Optional<AccountEntity> receiverAccountOpt = accountRepository.findByNickname(targetPlayerName);
        if (!receiverAccountOpt.isPresent()) {
            return ChatResult.error("目标玩家不存在");
        }

        String receiverId = receiverAccountOpt.get().getPlayerId();
        if (receiverId == null) {
            return ChatResult.error("目标玩家尚未创建角色");
        }

        // 创建聊天消息
        ChatMessageEntity chatMessage = new ChatMessageEntity();
        chatMessage.setChannelType(ChatMessageEntity.ChannelType.PRIVATE);
        chatMessage.setSenderId(senderId);
        chatMessage.setSenderNickname(senderAccountOpt.get().getNickname());
        chatMessage.setReceiverId(receiverId);
        chatMessage.setMessage(message);
        chatMessage.setTimestamp(System.currentTimeMillis());

        chatMessageRepository.save(chatMessage);

        return ChatResult.success("私聊消息发送成功");
    }

    @Override
    public List<ChatMessage> getChatHistory(String playerId) {
        // 获取玩家信息
        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return Collections.emptyList();
        }

        PlayerEntity player = playerOpt.get();
        long fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000;

        List<ChatMessageEntity> allMessages = new ArrayList<>();

        // 1. 获取私聊消息（最高优先级）
        List<ChatMessageEntity> privateMessages = chatMessageRepository.findByTimestampAfterOrderByTimestampAsc(fiveMinutesAgo)
                .stream()
                .filter(msg -> msg.getChannelType() == ChatMessageEntity.ChannelType.PRIVATE)
                .filter(msg -> msg.getSenderId().equals(playerId) || msg.getReceiverId().equals(playerId))
                .collect(Collectors.toList());
        allMessages.addAll(privateMessages);

        // 2. 获取队伍频道消息
        if (player.getPartyId() != null) {
            List<ChatMessageEntity> partyMessages = chatMessageRepository
                    .findByChannelTypeAndPartyIdOrderByTimestampDesc(
                            ChatMessageEntity.ChannelType.PARTY, player.getPartyId())
                    .stream()
                    .filter(msg -> msg.getTimestamp() >= fiveMinutesAgo)
                    .collect(Collectors.toList());
            allMessages.addAll(partyMessages);
        }

        // 3. 获取地图频道消息
        if (player.getCurrentMapId() != null) {
            List<ChatMessageEntity> mapMessages = chatMessageRepository
                    .findByChannelTypeAndMapIdOrderByTimestampDesc(
                            ChatMessageEntity.ChannelType.MAP, player.getCurrentMapId())
                    .stream()
                    .filter(msg -> msg.getTimestamp() >= fiveMinutesAgo)
                    .collect(Collectors.toList());
            allMessages.addAll(mapMessages);
        }

        // 4. 获取世界频道消息
        List<ChatMessageEntity> worldMessages = chatMessageRepository
                .findByChannelTypeOrderByTimestampDesc(ChatMessageEntity.ChannelType.WORLD)
                .stream()
                .filter(msg -> msg.getTimestamp() >= fiveMinutesAgo)
                .collect(Collectors.toList());
        allMessages.addAll(worldMessages);

        // 按时间排序并限制数量
        return allMessages.stream()
                .sorted(Comparator.comparing(ChatMessageEntity::getTimestamp))
                .limit(50)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 将实体转换为领域对象
     */
    private ChatMessage toDomain(ChatMessageEntity entity) {
        ChatMessage message = new ChatMessage();
        message.setId(entity.getId());
        message.setChannelType(ChatMessage.ChannelType.valueOf(entity.getChannelType().name()));
        message.setSenderId(entity.getSenderId());
        message.setSenderNickname(entity.getSenderNickname());
        message.setReceiverId(entity.getReceiverId());
        message.setMapId(entity.getMapId());
        message.setPartyId(entity.getPartyId());
        message.setMessage(entity.getMessage());
        message.setTimestamp(entity.getTimestamp());
        return message;
    }
}
