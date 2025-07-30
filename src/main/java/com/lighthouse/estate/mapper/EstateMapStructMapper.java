package com.lighthouse.estate.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.entity.Estate;

@Mapper(componentModel = "spring")
public interface EstateMapStructMapper {
    EstateDTO toDTO(Estate entity);
    List<EstateDTO> toDTOList(List<Estate> entities);
}