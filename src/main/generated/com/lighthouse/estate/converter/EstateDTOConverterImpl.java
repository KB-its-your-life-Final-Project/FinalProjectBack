package com.lighthouse.estate.converter;

import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.entity.Estate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-05T16:42:46+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class EstateDTOConverterImpl implements EstateDTOConverter {

    @Override
    public EstateDTO toDTO(Estate entity) {
        if ( entity == null ) {
            return null;
        }

        EstateDTO estateDTO = new EstateDTO();

        estateDTO.setId( entity.getId() );
        estateDTO.setSggCd( entity.getSggCd() );
        estateDTO.setSggNm( entity.getSggNm() );
        estateDTO.setUmdNm( entity.getUmdNm() );
        estateDTO.setJibun( entity.getJibun() );
        estateDTO.setBuildingName( entity.getBuildingName() );
        estateDTO.setMhouseType( entity.getMhouseType() );
        estateDTO.setShouseType( entity.getShouseType() );
        estateDTO.setBuildYear( entity.getBuildYear() );
        estateDTO.setBuildingType( entity.getBuildingType() );
        estateDTO.setSourceTable( entity.getSourceTable() );
        estateDTO.setOriginalId( entity.getOriginalId() );
        estateDTO.setJibunAddr( entity.getJibunAddr() );
        estateDTO.setLatitude( entity.getLatitude() );
        estateDTO.setLongitude( entity.getLongitude() );

        return estateDTO;
    }

    @Override
    public List<EstateDTO> toDTOList(Collection<Estate> entities) {
        if ( entities == null ) {
            return null;
        }

        List<EstateDTO> list = new ArrayList<EstateDTO>( entities.size() );
        for ( Estate estate : entities ) {
            list.add( toDTO( estate ) );
        }

        return list;
    }
}
