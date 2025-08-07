package com.lighthouse.wishlist.mapper;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.security.config.SecurityConfig;
import com.lighthouse.wishlist.dto.SeparatedRegionDTO;
import com.lighthouse.wishlist.entity.LikeRegion;
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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@Slf4j
@ActiveProfiles("local")
@Transactional
@Rollback
class RegionWishlistMapperTest {

    @Autowired
    private RegionWishlistMapper mapper;

    @Test
    void testInsertAndFindByMemberIdAndRegionCd() {
        // given
        LikeRegion likeRegion = new LikeRegion();
        likeRegion.setMemberId(34L);
        likeRegion.setSidoCd("11");
        likeRegion.setSsgCd("110");
        likeRegion.setUmdCd("111");
        likeRegion.setIsLike(2);
        likeRegion.setUmdNm("은행동");

        // when
        int inserted = mapper.saveLikeRegion(likeRegion);
        LikeRegion found = mapper.findByMemberIdAndRegionCd(34L, "11", "110", "111",false);

        // then
        assertThat(inserted).isEqualTo(1);
        assertThat(found).isNotNull();
        assertThat(found.getMemberId()).isEqualTo(34L);
        assertThat(found.getSidoCd()).isEqualTo("11");
    }

    @Test
    void testUpdateLikeRegion() {
        // given
        LikeRegion likeRegion = new LikeRegion();
        likeRegion.setMemberId(34L);
        likeRegion.setSidoCd("11");
        likeRegion.setSsgCd("110");
        likeRegion.setUmdCd("111");
        likeRegion.setIsLike(2);
        likeRegion.setUmdNm("은행동");
        mapper.saveLikeRegion(likeRegion);
        // when
        likeRegion.setIsLike(1);
        int updated = mapper.updateLikeRegion(likeRegion);
        LikeRegion found = mapper.findByMemberIdAndRegionCd(34L, "11", "110", "111", false);

        // then
        assertThat(updated).isEqualTo(1);
        assertThat(found.getIsLike()).isEqualTo(1);
    }

    @Test
    void testFindRegionsByMemberId() {
        // given
        LikeRegion likeRegion = new LikeRegion();
        likeRegion.setMemberId(34L);
        likeRegion.setSidoCd("11");
        likeRegion.setSsgCd("110");
        likeRegion.setUmdCd("111");
        likeRegion.setIsLike(1);
        likeRegion.setUmdNm("은행동");
        mapper.saveLikeRegion(likeRegion);

        // when
        List<SeparatedRegionDTO> regions = mapper.findRegionsByMemberId(34L);

        // then
        assertThat(regions).isNotEmpty();
        assertThat(regions.get(0).getSidoCd()).isEqualTo("11");
    }
}
