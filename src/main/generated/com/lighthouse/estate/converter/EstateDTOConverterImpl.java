package com.lighthouse.estate.converter;

import com.lighthouse.estate.dto.EstateAndEstateSalesDTO;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.dto.EstateSalesDTO;
import com.lighthouse.estate.entity.Estate;
import com.lighthouse.estate.entity.EstateSales;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-11T15:30:10+0900",
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

    @Override
    public List<EstateSalesDTO> toDTOSalesList(Collection<EstateSales> entities) {
        if ( entities == null ) {
            return null;
        }

        List<EstateSalesDTO> list = new ArrayList<EstateSalesDTO>( entities.size() );
        for ( EstateSales estateSales : entities ) {
            list.add( estateSalesToEstateSalesDTO( estateSales ) );
        }

        return list;
    }

    @Override
    public EstateAndEstateSalesDTO toEstateAndEstateSalesDTO(EstateDTO estate, EstateSalesDTO sales) {
        if ( estate == null && sales == null ) {
            return null;
        }

        EstateAndEstateSalesDTO estateAndEstateSalesDTO = new EstateAndEstateSalesDTO();

        if ( estate != null ) {
            estateAndEstateSalesDTO.setSggCd( estate.getSggCd() );
            estateAndEstateSalesDTO.setSggNm( estate.getSggNm() );
            estateAndEstateSalesDTO.setUmdNm( estate.getUmdNm() );
            estateAndEstateSalesDTO.setJibun( estate.getJibun() );
            estateAndEstateSalesDTO.setBuildingName( estate.getBuildingName() );
            estateAndEstateSalesDTO.setMhouseType( estate.getMhouseType() );
            estateAndEstateSalesDTO.setShouseType( estate.getShouseType() );
            estateAndEstateSalesDTO.setBuildYear( estate.getBuildYear() );
            estateAndEstateSalesDTO.setBuildingType( estate.getBuildingType() );
            estateAndEstateSalesDTO.setSourceTable( estate.getSourceTable() );
            estateAndEstateSalesDTO.setOriginalId( estate.getOriginalId() );
            estateAndEstateSalesDTO.setJibunAddr( estate.getJibunAddr() );
            estateAndEstateSalesDTO.setLatitude( estate.getLatitude() );
            estateAndEstateSalesDTO.setLongitude( estate.getLongitude() );
        }
        if ( sales != null ) {
            estateAndEstateSalesDTO.setEstateId( sales.getEstateId() );
            estateAndEstateSalesDTO.setDealYear( sales.getDealYear() );
            estateAndEstateSalesDTO.setDealMonth( sales.getDealMonth() );
            estateAndEstateSalesDTO.setDealDay( sales.getDealDay() );
            estateAndEstateSalesDTO.setDealAmount( sales.getDealAmount() );
            estateAndEstateSalesDTO.setDeposit( sales.getDeposit() );
            estateAndEstateSalesDTO.setMonthlyRent( sales.getMonthlyRent() );
            estateAndEstateSalesDTO.setTradeType( sales.getTradeType() );
        }

        return estateAndEstateSalesDTO;
    }

    protected EstateSalesDTO estateSalesToEstateSalesDTO(EstateSales estateSales) {
        if ( estateSales == null ) {
            return null;
        }

        EstateSalesDTO estateSalesDTO = new EstateSalesDTO();

        estateSalesDTO.setId( estateSales.getId() );
        estateSalesDTO.setEstateId( estateSales.getEstateId() );
        estateSalesDTO.setDealYear( estateSales.getDealYear() );
        estateSalesDTO.setDealMonth( estateSales.getDealMonth() );
        estateSalesDTO.setDealDay( estateSales.getDealDay() );
        estateSalesDTO.setDealAmount( estateSales.getDealAmount() );
        estateSalesDTO.setDeposit( estateSales.getDeposit() );
        estateSalesDTO.setMonthlyRent( estateSales.getMonthlyRent() );
        estateSalesDTO.setTradeType( estateSales.getTradeType() );

        return estateSalesDTO;
    }
}
