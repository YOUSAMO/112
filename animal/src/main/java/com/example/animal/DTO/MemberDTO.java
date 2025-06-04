package com.example.animal.DTO;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberDTO {
    private String u_id;
    private String u_pass;
    private String u_name;
    private String u_pnumber;
    private String u_email;
    private String u_gender;


    // 전화번호 3개 필드
    private String u_pnumber1;
    private String u_pnumber2;
    private String u_pnumber3;

    // 이메일 아이디, 도메인
    private String u_email_id;
    private String u_email_domain;




    public String getFullPnumber() {
        if (u_pnumber1 != null && u_pnumber2 != null && u_pnumber3 != null) {
            return u_pnumber1 + "-" + u_pnumber2 + "-" + u_pnumber3;
        }
        return null;
    }

    public String getFullEmail() {
        if (u_email_id != null && u_email_domain != null) {
            return u_email_id + "@" +u_email_domain;
        }
        return null;
    }




}