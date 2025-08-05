package com.lighthouse.youthProgram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class YouthProgramResponseDTO {
    private int resultCode;
    private String resultMessage;
    private YouthResult result;

    @Data
    public class YouthResult {
        private Pagging pagging;
        @JsonProperty("youthPolicyList")
        private List<YouthProgramDTO> youthPolicyList;
    }

    @Data
    public static class Pagging {
        private int totCount;
        private int pageNum;
        private int pageSize;
    }
}
