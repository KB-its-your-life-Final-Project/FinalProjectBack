package com.lighthouse.lawdCode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LawdCdRequestDTO {
    private String regionCd;
    private String sidoCd;
    private String sggCd;
    private String umdCd;
    private Integer offset;
    private Integer limit;

    /**
     * 페이지네이션용 DTO 생성
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return 설정된 LawdCdRequestDTO
     */
    public static LawdCdRequestDTO createForPagination(int offset, int limit) {
        LawdCdRequestDTO dto = new LawdCdRequestDTO();
        dto.setOffset(offset);
        dto.setLimit(limit);
        return dto;
    }
}
