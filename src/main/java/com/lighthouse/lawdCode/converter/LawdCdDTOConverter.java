package com.lighthouse.lawdCode.converter;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import com.lighthouse.lawdCode.dto.LawdCdResponseDTO;
import com.lighthouse.lawdCode.entity.LawdCd;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface LawdCdDTOConverter {
    LawdCdResponseDTO toDTO(LawdCd entity);
}