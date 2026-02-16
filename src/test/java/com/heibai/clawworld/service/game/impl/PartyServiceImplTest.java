package com.heibai.clawworld.service.game.impl;

import com.heibai.clawworld.domain.character.Party;
import com.heibai.clawworld.persistence.entity.PartyEntity;
import com.heibai.clawworld.persistence.entity.PlayerEntity;
import com.heibai.clawworld.persistence.mapper.PartyMapper;
import com.heibai.clawworld.persistence.repository.PartyRepository;
import com.heibai.clawworld.persistence.repository.PlayerRepository;
import com.heibai.clawworld.service.game.PartyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyServiceImplTest {

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PartyMapper partyMapper;

    @InjectMocks
    private PartyServiceImpl partyService;

    private PlayerEntity inviter;
    private PlayerEntity target;
    private PartyEntity party;

    @BeforeEach
    void setUp() {
        inviter = new PlayerEntity();
        inviter.setId("player1");
        inviter.setPartyId(null);

        target = new PlayerEntity();
        target.setId("player2");
        target.setPartyId(null);

        party = new PartyEntity();
        party.setId("party1");
        party.setLeaderId("player1");
        party.setMemberIds(new ArrayList<>());
        party.getMemberIds().add("player1");
        party.setPendingInvitations(new ArrayList<>());
        party.setPendingRequests(new ArrayList<>());
        party.setCreatedTime(System.currentTimeMillis());
    }

    @Test
    void testInvitePlayer_Success() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(inviter));
        when(playerRepository.findById("player2")).thenReturn(Optional.of(target));
        when(partyRepository.save(any(PartyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PartyService.PartyResult result = partyService.invitePlayer("player1", "player2");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("邀请已发送", result.getMessage());
        verify(partyRepository, atLeastOnce()).save(any(PartyEntity.class));
    }

    @Test
    void testInvitePlayer_TargetAlreadyInParty() {
        // Arrange
        target.setPartyId("party2");
        PartyEntity targetParty = new PartyEntity();
        targetParty.setId("party2");
        targetParty.setMemberIds(new ArrayList<>());
        targetParty.getMemberIds().add("player2");
        targetParty.getMemberIds().add("player3");

        when(playerRepository.findById("player1")).thenReturn(Optional.of(inviter));
        when(playerRepository.findById("player2")).thenReturn(Optional.of(target));
        when(partyRepository.findById("party2")).thenReturn(Optional.of(targetParty));

        // Act
        PartyService.PartyResult result = partyService.invitePlayer("player1", "player2");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("被邀请者已在队伍中", result.getMessage());
    }

    @Test
    void testAcceptInvite_Success() {
        // Arrange
        inviter.setPartyId("party1");
        PartyEntity.PartyInvitationData invitation = new PartyEntity.PartyInvitationData();
        invitation.setInviterId("player1");
        invitation.setInviteeId("player2");
        invitation.setInviteTime(System.currentTimeMillis());
        party.getPendingInvitations().add(invitation);

        when(playerRepository.findById("player2")).thenReturn(Optional.of(target));
        when(partyRepository.findByMemberIdsContaining("player1")).thenReturn(Optional.of(party));
        when(partyRepository.save(any(PartyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(playerRepository.save(any(PlayerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PartyService.PartyResult result = partyService.acceptInvite("player2", "player1");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("成功加入队伍", result.getMessage());
        verify(partyRepository).save(any(PartyEntity.class));
        verify(playerRepository).save(any(PlayerEntity.class));
    }

    @Test
    void testAcceptInvite_InvitationExpired() {
        // Arrange
        inviter.setPartyId("party1");
        PartyEntity.PartyInvitationData invitation = new PartyEntity.PartyInvitationData();
        invitation.setInviterId("player1");
        invitation.setInviteeId("player2");
        invitation.setInviteTime(System.currentTimeMillis() - 6 * 60 * 1000); // 6 minutes ago
        party.getPendingInvitations().add(invitation);

        when(playerRepository.findById("player2")).thenReturn(Optional.of(target));
        when(partyRepository.findByMemberIdsContaining("player1")).thenReturn(Optional.of(party));
        when(partyRepository.save(any(PartyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PartyService.PartyResult result = partyService.acceptInvite("player2", "player1");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("邀请已过期", result.getMessage());
    }

    @Test
    void testKickPlayer_Success() {
        // Arrange
        party.getMemberIds().add("player2");
        when(partyRepository.findByLeaderId("player1")).thenReturn(Optional.of(party));
        when(partyRepository.save(any(PartyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(playerRepository.findById("player2")).thenReturn(Optional.of(target));
        when(playerRepository.save(any(PlayerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PartyService.PartyResult result = partyService.kickPlayer("player1", "player2");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("已踢出玩家", result.getMessage());
        verify(partyRepository, times(2)).save(any(PartyEntity.class)); // Once for kick, once for solo party
    }

    @Test
    void testKickPlayer_CannotKickSelf() {
        // Arrange
        when(partyRepository.findByLeaderId("player1")).thenReturn(Optional.of(party));

        // Act
        PartyService.PartyResult result = partyService.kickPlayer("player1", "player1");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("不能踢出自己", result.getMessage());
    }

    @Test
    void testLeaveParty_Success() {
        // Arrange
        target.setPartyId("party1");
        party.getMemberIds().add("player2");

        when(playerRepository.findById("player2")).thenReturn(Optional.of(target));
        when(partyRepository.findById("party1")).thenReturn(Optional.of(party));
        when(partyRepository.save(any(PartyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(playerRepository.save(any(PlayerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PartyService.PartyResult result = partyService.leaveParty("player2");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("已离开队伍", result.getMessage());
    }

    @Test
    void testLeaveParty_LeaderCannotLeave() {
        // Arrange
        inviter.setPartyId("party1");

        when(playerRepository.findById("player1")).thenReturn(Optional.of(inviter));
        when(partyRepository.findById("party1")).thenReturn(Optional.of(party));

        // Act
        PartyService.PartyResult result = partyService.leaveParty("player1");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("队长不能离队，请使用解散队伍命令", result.getMessage());
    }

    @Test
    void testDisbandParty_Success() {
        // Arrange
        party.getMemberIds().add("player2");
        when(partyRepository.findByLeaderId("player1")).thenReturn(Optional.of(party));
        when(playerRepository.findById("player1")).thenReturn(Optional.of(inviter));
        when(playerRepository.findById("player2")).thenReturn(Optional.of(target));
        when(partyRepository.save(any(PartyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(playerRepository.save(any(PlayerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PartyService.PartyResult result = partyService.disbandParty("player1");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("队伍已解散", result.getMessage());
        verify(partyRepository).delete(party);
    }

    @Test
    void testGetPlayerParty_ReturnsParty() {
        // Arrange
        Party domainParty = new Party();
        domainParty.setId("party1");

        when(partyRepository.findByMemberIdsContaining("player1")).thenReturn(Optional.of(party));
        when(partyMapper.toDomain(party)).thenReturn(domainParty);

        // Act
        Party result = partyService.getPlayerParty("player1");

        // Assert
        assertNotNull(result);
        assertEquals("party1", result.getId());
    }
}
