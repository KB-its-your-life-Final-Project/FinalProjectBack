package com.lighthouse.lawdCode.converter;

import com.lighthouse.lawdCode.dto.LawdCdResponseDTO;
import com.lighthouse.lawdCode.entity.LawdCd;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-05T16:42:45+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class LawdCdDTOConverterImpl implements LawdCdDTOConverter {

    @Override
    public LawdCdResponseDTO toDTO(LawdCd entity) {
        if ( entity == null ) {
            return null;
        }

        LawdCdResponseDTO lawdCdResponseDTO = new LawdCdResponseDTO();

        lawdCdResponseDTO.setRegionCd( entity.getRegionCd() );
        lawdCdResponseDTO.setSidoCd( entity.getSidoCd() );
        lawdCdResponseDTO.setSggCd( entity.getSggCd() );
        lawdCdResponseDTO.setUmdCd( entity.getUmdCd() );
        lawdCdResponseDTO.setRiCd( entity.getRiCd() );
        lawdCdResponseDTO.setLocatjuminCd( entity.getLocatjuminCd() );
        lawdCdResponseDTO.setLocatjijukCd( entity.getLocatjijukCd() );
        lawdCdResponseDTO.setLocataddNm( entity.getLocataddNm() );
        lawdCdResponseDTO.setLocatOrder( entity.getLocatOrder() );
        lawdCdResponseDTO.setLocatRm( entity.getLocatRm() );
        lawdCdResponseDTO.setLocallowNm( entity.getLocallowNm() );
        lawdCdResponseDTO.setAdptDe( entity.getAdptDe() );
        lawdCdResponseDTO.setGridX( entity.getGridX() );
        lawdCdResponseDTO.setGridY( entity.getGridY() );
        lawdCdResponseDTO.setRegDate( entity.getRegDate() );

        return lawdCdResponseDTO;
    }
}
