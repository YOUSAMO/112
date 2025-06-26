package com.example.petbridge.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnimalApiDTO {

    // 외부 API의 JSON 필드 이름과 정확히 일치시킵니다.
    private String desertionNo;
    private String happenDt;
    private String happenPlace;

    @JsonProperty("kindNm") // JSON의 kindNm 필드를 kindName 변수에 매핑
    private String kindName;

    private String colorCd;
    private String age;
    private String weight;
    private String noticeNo;
    private String noticeSdt;
    private String noticeEdt;

    // API 응답에 popfile, popfile1, popfile2가 섞여있을 수 있으므로 모두 선언
    private String popfile;
    private String popfile1;
    private String popfile2;

    private String processState;
    private String sexCd;
    private String neuterYn;
    private String specialMark;
    private String careNm;
    private String careAddr;
    private String orgNm;
}