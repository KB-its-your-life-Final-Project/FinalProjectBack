package com.lighthouse.transactions.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
/** XML API를 받기 위한 DTO
 *  Body의 Items만 사용한다
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionApiDTO<T> {
    @JacksonXmlProperty(localName = "header")
    private Header header;
    @JacksonXmlProperty(localName = "body")
    private Body<T> body;

    @Setter
    @Getter
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }
    @Setter
    @Getter
    public static class Body<T> {
        @JacksonXmlProperty(localName = "numOfRows")
        private int numOfRows;

        @JacksonXmlProperty(localName = "pageNo")
        private int pageNo;

        @JacksonXmlProperty(localName = "totalCount")
        private int totalCount;

        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        private List<T> items;
    }
}
