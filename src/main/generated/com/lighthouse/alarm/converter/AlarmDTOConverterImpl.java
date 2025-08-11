package com.lighthouse.alarm.converter;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.entity.Alarms;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-11T15:30:10+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class AlarmDTOConverterImpl implements AlarmDTOConverter {

    @Override
    public AlarmResponseDto toDTO(Alarms alarms) {
        if ( alarms == null ) {
            return null;
        }

        AlarmResponseDto.AlarmResponseDtoBuilder alarmResponseDto = AlarmResponseDto.builder();

        alarmResponseDto.id( alarms.getId() );
        alarmResponseDto.memberId( alarms.getMemberId() );
        alarmResponseDto.type( alarms.getType() );
        alarmResponseDto.text( alarms.getText() );
        if ( alarms.getRegDate() != null ) {
            alarmResponseDto.regDate( DateTimeFormatter.ISO_LOCAL_DATE_TIME.format( alarms.getRegDate() ) );
        }
        alarmResponseDto.isChecked( alarms.getIsChecked() );
        alarmResponseDto.getAlarm( alarms.getGetAlarm() );

        return alarmResponseDto.build();
    }
}
