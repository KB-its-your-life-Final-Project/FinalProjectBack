package com.lighthouse.estate.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.entity.Estate;
import com.lighthouse.estate.mapper.EstateMapStructMapper;
import com.lighthouse.estate.mapper.EstateMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstateService {

    private final EstateMapper estateMapper;
    private final EstateMapStructMapper estateMapStructMapper;

    public EstateDTO getEstateByLatLng(double lat, double lng) {
        Estate estate = Optional.ofNullable(estateMapper.getEstateByLatLng(lat, lng))
            .orElseThrow(NoSuchElementException::new);
        return estateMapStructMapper.toDTO(estate); // DTO 반환
    }
    public List<EstateDTO> getAllEstateByLatLng(double lat, double lng) {
        List<Estate> entities = estateMapper.getAllEstateByLatLng(lat, lng); // 메서드명 수정
        return estateMapStructMapper.toDTOList(entities);
    }
}
