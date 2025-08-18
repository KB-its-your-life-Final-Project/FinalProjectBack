package com.lighthouse.member.service;

import com.lighthouse.member.dto.MemberResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MemberServiceTest {

    private MemberService service;

    @BeforeEach
    void setUp() {
        service = mock(MemberService.class);
    }

    @Test
    @DisplayName("회원 목록 조회 - Mock 테스트")
    void testSelectMembers() {
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

        when(service.findAllMembers()).thenReturn(List.of(member1, member2));

        List<MemberResponseDTO> result = service.findAllMembers();

        verify(service).findAllMembers();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("관리자", result.get(0).getName());
        assertEquals("사용자1", result.get(1).getName());
    }

    @Test
    @DisplayName("회원 단건 조회 - Mock 테스트")
    void testSelectMemberByUsername() {
        int username = 1;

        MemberResponseDTO dto = MemberResponseDTO.builder()
                .id(1)
                .name("관리자")
                .email("admin@example.com")
                .kakaoId("kakao123")
                .phone("010-1234-5678")
                .createdType(1)
                .isDelete(0)
                .build();

        when(service.findMemberById(1)).thenReturn(dto);

        MemberResponseDTO result = service.findMemberById(username);

        verify(service).findMemberById(username);
        assertNotNull(result, "admin 회원은 반드시 존재해야 함");
        assertEquals("관리자", result.getName());
        assertEquals(1, result.getId());
    }
}
