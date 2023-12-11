package com.MailReader.readMailViaImap.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailInformation {
    private String subject;

    private String from;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Istanbul")
    private Date sentDate;

    private String content;

    private List<AttachmentInformation> attachmentInformation;

    private Integer messageNumber;

}
