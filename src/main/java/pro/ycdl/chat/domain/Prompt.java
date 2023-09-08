package pro.ycdl.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Prompt {

    private String question;
    private String ip;
    private String data;
    private String uid;
    private Integer requestTime;
}
