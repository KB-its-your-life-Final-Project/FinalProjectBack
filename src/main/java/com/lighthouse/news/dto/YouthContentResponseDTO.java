package com.lighthouse.news.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class YouthContentResponseDTO {
    private int resultCode;
    private String resultMessage;
    private YouthResult result;

    @Data
    public class YouthResult {
        private Pagging pagging;
        @JsonProperty("youthPolicyList")
        private List<NewsDTO> youthPolicyList;
    }

    @Data
    public static class Pagging {
        private int totCount;
        private int pageNum;
        private int pageSize;
    }
}
