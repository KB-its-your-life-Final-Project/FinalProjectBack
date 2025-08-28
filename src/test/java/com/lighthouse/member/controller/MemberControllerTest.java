package com.lighthouse.member.controller;

import com.lighthouse.alarm.service.AlarmSchedulerService;
import com.lighthouse.member.dto.MemberResponseDTO;
import com.lighthouse.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MemberControllerTest {

    private MemberController controller;
    private MemberService service;
    private AlarmSchedulerService alarmSchedulerService;
    @BeforeEach
    void setUp() {
        service = mock(MemberService.class);
        alarmSchedulerService = mock(AlarmSchedulerService.class);
        controller = new MemberController(service, alarmSchedulerService);
    }

    @Test
    @DisplayName("회원 ID로 조회 - 성공")
    void testGetById() {
        int memberId = 1;
        // Mock Member 엔티티 생성
        MemberResponseDTO dto = MemberResponseDTO.builder()
                .id(1)
                .name("관리자")
                .email("admin@example.com")
                .kakaoId("kakao123")
                .googleId(null)
                .phone("010-1234-5678")
                .age(30)
                .profileImg(null)
                .createdType(1)
                .regDate(null)
                .isDelete(0)
                .build();

        when(service.findMemberById(memberId)).thenReturn(dto);

        var response = controller.findMemberById(memberId);

        verify(service).findMemberById(memberId);
        assertNotNull(response.getBody());
        assertEquals(memberId, response.getBody().getData().getId());
    }

    @Test
    @DisplayName("회원 목록 조회 - 성공")
    void testGetList() {
        MemberResponseDTO member1 = MemberResponseDTO.builder()
                .id(1)
                .name("관리자")
                .email("admin@example.com")
                .kakaoId("kakao123")
                .phone("010-1234-5678")
                .createdType(1)
                .isDelete(0)
                .build();

        MemberResponseDTO member2 = MemberResponseDTO.builder()
                .id(2)
                .name("사용자1")
                .email("user1@example.com")
                .kakaoId(null)
                .phone("010-9876-5432")
                .createdType(1)
                .isDelete(0)
                .build();
        List<MemberResponseDTO> members = List.of(member1, member2);

        when(service.findAllMembers()).thenReturn(members);

        var response = controller.findAllMembers();

        verify(service).findAllMembers();
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getData().size());
        assertEquals(1, response.getBody().getData().get(0).getId());
    }
}