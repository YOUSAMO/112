package com.example.petbridge.DTO;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

//관리자 2명으로 해서 가정한다 .
//서비스 컨트롤러 레포지티로 정보 전달할때 사용할 떄 효율적이다ㅣ
public class SessionAdminDTO {


    private String aId;//관리자 아이디
    private String aPass;//관리자 비밀번호
    private String aName;//관리자 이름

}
