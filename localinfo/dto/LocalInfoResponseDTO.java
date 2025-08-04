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
    private String region;
    private String regionCd;
    private int gridX;
    private int gridY;
    private String locataddNm;
}
