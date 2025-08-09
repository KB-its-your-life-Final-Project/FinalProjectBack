package com.lighthouse.localinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocalInfoResponseDTO {

    private String regionCd;
    private String locataddNm;
    private Integer gridX;
    private Integer gridY;
    private String sidoCd;
    private String sggCd;
}