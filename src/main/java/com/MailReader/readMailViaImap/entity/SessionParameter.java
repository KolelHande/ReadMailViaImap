package com.MailReader.readMailViaImap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SessionParameter {
    private String hostName;

    private String password;

    private String userName;
}
