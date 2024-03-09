package com.example.promptengineeringai.web;


import lombok.AllArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class OpenAiRestController {

    private ChatClient chatClient;

    @GetMapping("/chat")
    public String chat(String message){
        String reponse = chatClient.call(message);

        return  reponse;
    }
}
