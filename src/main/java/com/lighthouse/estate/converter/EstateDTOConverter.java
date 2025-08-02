package com.lighthouse.estate.converter;

import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.entity.Estate;

@Mapper(componentModel = "spring")
public interface EstateDTOConverter {
    EstateDTO toDTO(Estate entity);
    List<EstateDTO> toDTOList(Collection<Estate> entities);
}