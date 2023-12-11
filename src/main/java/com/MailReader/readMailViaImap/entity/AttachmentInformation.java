package com.MailReader.readMailViaImap.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AttachmentInformation {
    private String fileName;
    private String downloadLink;
}
