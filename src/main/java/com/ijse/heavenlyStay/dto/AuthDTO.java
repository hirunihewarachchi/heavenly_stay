package com.ijse.heavenlyStay.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthDTO {
    private String userName;
    private String password;
}