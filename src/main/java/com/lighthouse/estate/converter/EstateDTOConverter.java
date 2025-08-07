package com.lighthouse.estate.converter;

import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.dto.EstateSalesDTO;
import com.lighthouse.estate.dto.EstateAndEstateSalesDTO;
import com.lighthouse.estate.entity.Estate;
import com.lighthouse.estate.entity.EstateSales;

@Mapper(componentModel = "spring")
public interface EstateDTOConverter {
    EstateDTO toDTO(Estate entity);
    List<EstateDTO> toDTOList(Collection<Estate> entities);

    List<EstateSalesDTO> toDTOSalesList(Collection<EstateSales> entities);
    
    @Mapping(source = "estate", target = ".")
    @Mapping(source = "sales", target = ".")
    EstateAndEstateSalesDTO toEstateAndEstateSalesDTO(EstateDTO estate, EstateSalesDTO sales);
}