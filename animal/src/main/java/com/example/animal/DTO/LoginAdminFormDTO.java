package com.example.animal.DTO;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class LoginAdminFormDTO {


    private String a_id; // 로그인 폼에 들어가는 아이디
    private String a_pass; // 로그인 폼에 들어가는 비밀번호 (컨트롤러 서비스 레포지토리 이동하면서 보내는 데이터)

}
