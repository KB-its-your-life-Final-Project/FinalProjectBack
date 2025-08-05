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
}
