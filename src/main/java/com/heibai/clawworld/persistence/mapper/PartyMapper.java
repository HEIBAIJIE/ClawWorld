package com.heibai.clawworld.persistence.mapper;

import com.heibai.clawworld.domain.character.Party;
import com.heibai.clawworld.persistence.entity.PartyEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 队伍领域对象与持久化实体之间的映射器
 */
@Component
public class PartyMapper {

    /**
     * 将领域对象转换为持久化实体
     */
    public PartyEntity toEntity(Party party) {
        if (party == null) {
            return null;
        }

        PartyEntity entity = new PartyEntity();
        entity.setId(party.getId());
        entity.setLeaderId(party.getLeaderId());
        entity.setMemberIds(party.getMemberIds());
        entity.setFaction(party.getFaction());
        entity.setCreatedTime(party.getCreatedTime());

        // 转换待处理的邀请
        if (party.getPendingInvitations() != null) {
            entity.setPendingInvitations(party.getPendingInvitations().stream()
                    .map(invitation -> {
                        PartyEntity.PartyInvitationData data = new PartyEntity.PartyInvitationData();
                        data.setInviterId(invitation.getInviterId());
                        data.setInviteeId(invitation.getInviteeId());
                        data.setInviteTime(invitation.getInviteTime());
                        return data;
                    })
                    .collect(Collectors.toList()));
        }

        // 转换待处理的请求
        if (party.getPendingRequests() != null) {
            entity.setPendingRequests(party.getPendingRequests().stream()
                    .map(request -> {
                        PartyEntity.PartyRequestData data = new PartyEntity.PartyRequestData();
                        data.setRequesterId(request.getRequesterId());
                        data.setPartyId(request.getPartyId());
                        data.setRequestTime(request.getRequestTime());
                        return data;
                    })
                    .collect(Collectors.toList()));
        }

        return entity;
    }

    /**
     * 将持久化实体转换为领域对象
     */
    public Party toDomain(PartyEntity entity) {
        if (entity == null) {
            return null;
        }

        Party party = new Party();
        party.setId(entity.getId());
        party.setLeaderId(entity.getLeaderId());
        party.setMemberIds(entity.getMemberIds());
        party.setFaction(entity.getFaction());
        party.setCreatedTime(entity.getCreatedTime());

        // 转换待处理的邀请
        if (entity.getPendingInvitations() != null) {
            party.setPendingInvitations(entity.getPendingInvitations().stream()
                    .map(data -> {
                        Party.PartyInvitation invitation = new Party.PartyInvitation();
                        invitation.setInviterId(data.getInviterId());
                        invitation.setInviteeId(data.getInviteeId());
                        invitation.setInviteTime(data.getInviteTime());
                        return invitation;
                    })
                    .collect(Collectors.toList()));
        }

        // 转换待处理的请求
        if (entity.getPendingRequests() != null) {
            party.setPendingRequests(entity.getPendingRequests().stream()
                    .map(data -> {
                        Party.PartyRequest request = new Party.PartyRequest();
                        request.setRequesterId(data.getRequesterId());
                        request.setPartyId(data.getPartyId());
                        request.setRequestTime(data.getRequestTime());
                        return request;
                    })
                    .collect(Collectors.toList()));
        }

        return party;
    }
}
