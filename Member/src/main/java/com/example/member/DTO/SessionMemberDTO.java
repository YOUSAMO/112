package com.example.member.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SessionMemberDTO {

    private String u_id;
    private String u_pass;
    private String u_name;
    private String u_pnumber;
    private String u_email;
    private String u_gender;


}
