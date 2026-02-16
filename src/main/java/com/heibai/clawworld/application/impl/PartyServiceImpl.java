package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.domain.character.Party;
import com.heibai.clawworld.infrastructure.persistence.entity.PartyEntity;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.mapper.PartyMapper;
import com.heibai.clawworld.infrastructure.persistence.repository.PartyRepository;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import com.heibai.clawworld.application.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * 队伍管理服务实现
 */
@Service
@RequiredArgsConstructor
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;
    private final PlayerRepository playerRepository;
    private final PartyMapper partyMapper;

    @Override
    @Transactional
    public PartyResult invitePlayer(String inviterId, String targetPlayerId) {
        // 验证邀请者
        Optional<PlayerEntity> inviterOpt = playerRepository.findById(inviterId);
        if (!inviterOpt.isPresent()) {
            return PartyResult.error("邀请者不存在");
        }

        // 验证被邀请者
        Optional<PlayerEntity> targetOpt = playerRepository.findById(targetPlayerId);
        if (!targetOpt.isPresent()) {
            return PartyResult.error("被邀请者不存在");
        }

        PlayerEntity inviter = inviterOpt.get();
        PlayerEntity target = targetOpt.get();

        // 检查被邀请者是否已在队伍中
        if (target.getPartyId() != null) {
            Optional<PartyEntity> targetPartyOpt = partyRepository.findById(target.getPartyId());
            if (targetPartyOpt.isPresent() && !targetPartyOpt.get().isSolo()) {
                return PartyResult.error("被邀请者已在队伍中");
            }
        }

        // 获取或创建邀请者的队伍
        PartyEntity party;
        if (inviter.getPartyId() == null) {
            // 创建新队伍
            party = createParty(inviter);
        } else {
            Optional<PartyEntity> partyOpt = partyRepository.findById(inviter.getPartyId());
            if (!partyOpt.isPresent()) {
                // 队伍不存在，创建新队伍
                party = createParty(inviter);
            } else {
                party = partyOpt.get();
            }
        }

        // 检查队伍是否已满
        if (party.isFull()) {
            return PartyResult.error("队伍已满");
        }

        // 检查是否已有邀请
        boolean alreadyInvited = party.getPendingInvitations().stream()
                .anyMatch(inv -> inv.getInviteeId().equals(targetPlayerId) && !isExpired(inv.getInviteTime()));
        if (alreadyInvited) {
            return PartyResult.error("已经邀请过该玩家");
        }

        // 添加邀请
        PartyEntity.PartyInvitationData invitation = new PartyEntity.PartyInvitationData();
        invitation.setInviterId(inviterId);
        invitation.setInviteeId(targetPlayerId);
        invitation.setInviteTime(System.currentTimeMillis());
        party.getPendingInvitations().add(invitation);

        partyRepository.save(party);

        return PartyResult.success("邀请已发送", party.getId());
    }

    @Override
    @Transactional
    public PartyResult acceptInvite(String playerId, String inviterId) {
        // 验证玩家
        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return PartyResult.error("玩家不存在");
        }

        PlayerEntity player = playerOpt.get();

        // 检查玩家是否已在队伍中
        if (player.getPartyId() != null) {
            Optional<PartyEntity> currentPartyOpt = partyRepository.findById(player.getPartyId());
            if (currentPartyOpt.isPresent() && !currentPartyOpt.get().isSolo()) {
                return PartyResult.error("你已在队伍中");
            }
        }

        // 查找邀请者的队伍
        Optional<PartyEntity> partyOpt = partyRepository.findByMemberIdsContaining(inviterId);
        if (!partyOpt.isPresent()) {
            return PartyResult.error("邀请者不在任何队伍中");
        }

        PartyEntity party = partyOpt.get();

        // 查找邀请记录
        Optional<PartyEntity.PartyInvitationData> invitationOpt = party.getPendingInvitations().stream()
                .filter(inv -> inv.getInviterId().equals(inviterId) && inv.getInviteeId().equals(playerId))
                .findFirst();

        if (!invitationOpt.isPresent()) {
            return PartyResult.error("未找到邀请记录");
        }

        PartyEntity.PartyInvitationData invitation = invitationOpt.get();
        if (isExpired(invitation.getInviteTime())) {
            party.getPendingInvitations().remove(invitation);
            partyRepository.save(party);
            return PartyResult.error("邀请已过期");
        }

        // 检查队伍是否已满
        if (party.isFull()) {
            return PartyResult.error("队伍已满");
        }

        // 移除旧的单人队伍
        if (player.getPartyId() != null) {
            partyRepository.deleteById(player.getPartyId());
        }

        // 加入队伍
        party.getMemberIds().add(playerId);
        party.getPendingInvitations().remove(invitation);
        partyRepository.save(party);

        // 更新玩家信息
        player.setPartyId(party.getId());
        player.setPartyLeader(false);
        playerRepository.save(player);

        return PartyResult.success("成功加入队伍", party.getId());
    }

    @Override
    @Transactional
    public PartyResult rejectInvite(String playerId, String inviterId) {
        // 查找邀请者的队伍
        Optional<PartyEntity> partyOpt = partyRepository.findByMemberIdsContaining(inviterId);
        if (!partyOpt.isPresent()) {
            return PartyResult.error("邀请者不在任何队伍中");
        }

        PartyEntity party = partyOpt.get();

        // 查找并移除邀请记录
        boolean removed = party.getPendingInvitations().removeIf(
                inv -> inv.getInviterId().equals(inviterId) && inv.getInviteeId().equals(playerId)
        );

        if (!removed) {
            return PartyResult.error("未找到邀请记录");
        }

        partyRepository.save(party);
        return PartyResult.success("已拒绝邀请");
    }

    @Override
    @Transactional
    public PartyResult requestJoin(String playerId, String targetPlayerId) {
        // 验证请求者
        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return PartyResult.error("玩家不存在");
        }

        PlayerEntity player = playerOpt.get();

        // 检查请求者是否已在队伍中
        if (player.getPartyId() != null) {
            Optional<PartyEntity> currentPartyOpt = partyRepository.findById(player.getPartyId());
            if (currentPartyOpt.isPresent() && !currentPartyOpt.get().isSolo()) {
                return PartyResult.error("你已在队伍中");
            }
        }

        // 查找目标玩家的队伍
        Optional<PartyEntity> targetPartyOpt = partyRepository.findByMemberIdsContaining(targetPlayerId);
        if (!targetPartyOpt.isPresent()) {
            return PartyResult.error("目标玩家不在任何队伍中");
        }

        PartyEntity targetParty = targetPartyOpt.get();

        // 检查是否为单人队伍
        if (targetParty.isSolo()) {
            return PartyResult.error("目标玩家不在队伍中");
        }

        // 检查队伍是否已满
        if (targetParty.isFull()) {
            return PartyResult.error("目标队伍已满");
        }

        // 检查是否已有请求
        boolean alreadyRequested = targetParty.getPendingRequests().stream()
                .anyMatch(req -> req.getRequesterId().equals(playerId) && !isExpired(req.getRequestTime()));
        if (alreadyRequested) {
            return PartyResult.error("已经请求过加入该队伍");
        }

        // 添加请求
        PartyEntity.PartyRequestData request = new PartyEntity.PartyRequestData();
        request.setRequesterId(playerId);
        request.setPartyId(targetParty.getId());
        request.setRequestTime(System.currentTimeMillis());
        targetParty.getPendingRequests().add(request);

        partyRepository.save(targetParty);

        return PartyResult.success("加入请求已发送");
    }

    @Override
    @Transactional
    public PartyResult acceptJoinRequest(String leaderId, String requesterId) {
        // 验证队长
        Optional<PartyEntity> partyOpt = partyRepository.findByLeaderId(leaderId);
        if (!partyOpt.isPresent()) {
            return PartyResult.error("你不是任何队伍的队长");
        }

        PartyEntity party = partyOpt.get();

        // 查找请求记录
        Optional<PartyEntity.PartyRequestData> requestOpt = party.getPendingRequests().stream()
                .filter(req -> req.getRequesterId().equals(requesterId))
                .findFirst();

        if (!requestOpt.isPresent()) {
            return PartyResult.error("未找到加入请求");
        }

        PartyEntity.PartyRequestData request = requestOpt.get();
        if (isExpired(request.getRequestTime())) {
            party.getPendingRequests().remove(request);
            partyRepository.save(party);
            return PartyResult.error("请求已过期");
        }

        // 检查队伍是否已满
        if (party.isFull()) {
            return PartyResult.error("队伍已满");
        }

        // 验证请求者
        Optional<PlayerEntity> requesterOpt = playerRepository.findById(requesterId);
        if (!requesterOpt.isPresent()) {
            return PartyResult.error("请求者不存在");
        }

        PlayerEntity requester = requesterOpt.get();

        // 移除旧的单人队伍
        if (requester.getPartyId() != null) {
            partyRepository.deleteById(requester.getPartyId());
        }

        // 加入队伍
        party.getMemberIds().add(requesterId);
        party.getPendingRequests().remove(request);
        partyRepository.save(party);

        // 更新玩家信息
        requester.setPartyId(party.getId());
        requester.setPartyLeader(false);
        playerRepository.save(requester);

        return PartyResult.success("玩家已加入队伍", party.getId());
    }

    @Override
    @Transactional
    public PartyResult rejectJoinRequest(String leaderId, String requesterId) {
        // 验证队长
        Optional<PartyEntity> partyOpt = partyRepository.findByLeaderId(leaderId);
        if (!partyOpt.isPresent()) {
            return PartyResult.error("你不是任何队伍的队长");
        }

        PartyEntity party = partyOpt.get();

        // 查找并移除请求记录
        boolean removed = party.getPendingRequests().removeIf(
                req -> req.getRequesterId().equals(requesterId)
        );

        if (!removed) {
            return PartyResult.error("未找到加入请求");
        }

        partyRepository.save(party);
        return PartyResult.success("已拒绝加入请求");
    }

    @Override
    @Transactional
    public PartyResult kickPlayer(String leaderId, String targetPlayerId) {
        // 验证队长
        Optional<PartyEntity> partyOpt = partyRepository.findByLeaderId(leaderId);
        if (!partyOpt.isPresent()) {
            return PartyResult.error("你不是任何队伍的队长");
        }

        PartyEntity party = partyOpt.get();

        // 检查目标玩家是否在队伍中
        if (!party.hasMember(targetPlayerId)) {
            return PartyResult.error("目标玩家不在队伍中");
        }

        // 不能踢出自己
        if (targetPlayerId.equals(leaderId)) {
            return PartyResult.error("不能踢出自己");
        }

        // 移除玩家
        party.getMemberIds().remove(targetPlayerId);
        partyRepository.save(party);

        // 为被踢出的玩家创建单人队伍
        Optional<PlayerEntity> targetOpt = playerRepository.findById(targetPlayerId);
        if (targetOpt.isPresent()) {
            PlayerEntity target = targetOpt.get();
            PartyEntity soloParty = createParty(target);
            target.setPartyId(soloParty.getId());
            target.setPartyLeader(true);
            playerRepository.save(target);
        }

        return PartyResult.success("已踢出玩家");
    }

    @Override
    @Transactional
    public PartyResult leaveParty(String playerId) {
        // 验证玩家
        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return PartyResult.error("玩家不存在");
        }

        PlayerEntity player = playerOpt.get();

        if (player.getPartyId() == null) {
            return PartyResult.error("你不在任何队伍中");
        }

        // 查找队伍
        Optional<PartyEntity> partyOpt = partyRepository.findById(player.getPartyId());
        if (!partyOpt.isPresent()) {
            return PartyResult.error("队伍不存在");
        }

        PartyEntity party = partyOpt.get();

        // 队长不能离队，只能解散
        if (party.isLeader(playerId)) {
            return PartyResult.error("队长不能离队，请使用解散队伍命令");
        }

        // 移除玩家
        party.getMemberIds().remove(playerId);
        partyRepository.save(party);

        // 为玩家创建单人队伍
        PartyEntity soloParty = createParty(player);
        player.setPartyId(soloParty.getId());
        player.setPartyLeader(true);
        playerRepository.save(player);

        return PartyResult.success("已离开队伍");
    }

    @Override
    @Transactional
    public PartyResult disbandParty(String leaderId) {
        // 验证队长
        Optional<PartyEntity> partyOpt = partyRepository.findByLeaderId(leaderId);
        if (!partyOpt.isPresent()) {
            return PartyResult.error("你不是任何队伍的队长");
        }

        PartyEntity party = partyOpt.get();

        // 为所有成员创建单人队伍
        for (String memberId : party.getMemberIds()) {
            Optional<PlayerEntity> memberOpt = playerRepository.findById(memberId);
            if (memberOpt.isPresent()) {
                PlayerEntity member = memberOpt.get();
                PartyEntity soloParty = createParty(member);
                member.setPartyId(soloParty.getId());
                member.setPartyLeader(true);
                playerRepository.save(member);
            }
        }

        // 删除队伍
        partyRepository.delete(party);

        return PartyResult.success("队伍已解散");
    }

    @Override
    public Party getParty(String partyId) {
        Optional<PartyEntity> partyOpt = partyRepository.findById(partyId);
        return partyOpt.map(partyMapper::toDomain).orElse(null);
    }

    @Override
    public Party getPlayerParty(String playerId) {
        Optional<PartyEntity> partyOpt = partyRepository.findByMemberIdsContaining(playerId);
        return partyOpt.map(partyMapper::toDomain).orElse(null);
    }

    /**
     * 创建单人队伍
     */
    private PartyEntity createParty(PlayerEntity player) {
        PartyEntity party = new PartyEntity();
        party.setId(UUID.randomUUID().toString());
        party.setLeaderId(player.getId());
        party.setMemberIds(new ArrayList<>());
        party.getMemberIds().add(player.getId());
        party.setFaction("PLAYER_" + player.getId());
        party.setCreatedTime(System.currentTimeMillis());
        party.setPendingInvitations(new ArrayList<>());
        party.setPendingRequests(new ArrayList<>());
        return partyRepository.save(party);
    }

    /**
     * 检查时间是否过期（5分钟）
     */
    private boolean isExpired(Long timestamp) {
        return System.currentTimeMillis() - timestamp > 5 * 60 * 1000;
    }
}
