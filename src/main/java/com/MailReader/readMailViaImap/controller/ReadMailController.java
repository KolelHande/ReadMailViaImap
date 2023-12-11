package com.MailReader.readMailViaImap.controller;

import com.MailReader.readMailViaImap.entity.EmailInformation;
import com.MailReader.readMailViaImap.entity.SessionParameter;
import com.MailReader.readMailViaImap.service.ReadEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emails")
public class ReadMailController {
    private final ReadEmailService readEmailService;
    @GetMapping("/read")
    public List<EmailInformation> readEmails(@RequestBody SessionParameter sessionParameter)
    {
        return readEmailService.readEmails(sessionParameter);
    }
}
