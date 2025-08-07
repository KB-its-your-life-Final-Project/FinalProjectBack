package com.lighthouse.wishlist.mapper;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.security.config.SecurityConfig;
import com.lighthouse.wishlist.dto.EstateWishlistResponseDTO;
import com.lighthouse.wishlist.entity.LikeEstate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@Slf4j
@ActiveProfiles("local")
@Transactional
@Rollback
class EstateWishlistMapperTest {

    @Autowired
    EstateWishlistMapper mapper;

    @Test
    void saveLikeEstate() {
        LikeEstate likeEstate = new LikeEstate();
        likeEstate.setJibunAddr("아주동 1575");
        likeEstate.setMemberId(34L);
        likeEstate.setIsLike(1);

        mapper.saveLikeEstate(likeEstate);

        LikeEstate result = mapper.findByMemberIdAndJibunAddr(34L, "아주동 1575", false);
        assertNotNull(result);
        assertEquals(1, result.getIsLike());

        log.info("Insert test passed: {}", result);
    }

    @Test
    void updateLikeEstate() {
        // 먼저 insert
        LikeEstate likeEstate = new LikeEstate();
        likeEstate.setJibunAddr("아주동 1575");
        likeEstate.setMemberId(34L);
        likeEstate.setIsLike(1);
        mapper.saveLikeEstate(likeEstate);

        // 그 다음 update
        likeEstate.setIsLike(2);
        mapper.updateLikeEstate(likeEstate);

        LikeEstate updated = mapper.findByMemberIdAndJibunAddr(34L, "아주동 1575",false);
        assertNotNull(updated);
        assertEquals(2, updated.getIsLike());

        log.info("Update test passed: {}", updated);
    }

    @Test
    void findByMemberId_AndJibunAddr_whenExists_thenReturnData() {
        LikeEstate likeEstate = new LikeEstate();
        likeEstate.setJibunAddr("아주동 1575");
        likeEstate.setMemberId(34L);
        likeEstate.setIsLike(1);
        mapper.saveLikeEstate(likeEstate);

        LikeEstate result = mapper.findByMemberIdAndJibunAddr(34L, "아주동 1575",false);
        assertNotNull(result);
        assertEquals(1, result.getIsLike());
        log.info("Select test (exists) passed: {}", result);
    }

    @Test
    void findByMemberId_AndJibunAddr_whenNotExists_thenReturnNull() {
        LikeEstate result = mapper.findByMemberIdAndJibunAddr(999L, "아주동 1575", false);
        assertNull(result);
        log.info("Select test (not exists) passed: result is null as expected");
    }

    @Test
    void findEstateIdsByMemberId_returnsIds() {
        // Givenc
        LikeEstate likeEstate = new LikeEstate();
        likeEstate.setEstateId(698L);
        likeEstate.setMemberId(34L);
        likeEstate.setIsLike(1);
        likeEstate.setJibunAddr("일운면 소동리 687");
        likeEstate.setBuildingName("동성그린");
        likeEstate.setBuildingType(1); // 예: 1:아파트, 2:오피스텔, 3:빌라 등

        mapper.saveLikeEstate(likeEstate);

        // When
        Long memberId = 34L;
        List<EstateWishlistResponseDTO> result = mapper.findAllEstateByMemberId(memberId);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());

        EstateWishlistResponseDTO dto = result.get(0);
        assertEquals(698L, dto.getEstateId());
        assertEquals("동성그린", dto.getBuildingName());
        assertEquals("일운면 소동리 687", dto.getJibunAddr());
        assertEquals(1, dto.getBuildingType());

        // 매매가 OR 보증금/월세는 NULL일 수 있음
        log.info("조회된 매물: estateId={}, name={}, amount={}, deposit={}, rent={}",
                dto.getEstateId(), dto.getBuildingName(), dto.getAmount(), dto.getDeposit(), dto.getMonthlyRent());
    }
}