package com.lighthouse.alarm.converter;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.entity.Alarms;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlarmDTOConverter {
    AlarmResponseDto toDTO(Alarms alarms);
}
