package com.example.promptengineeringai.web;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class OpenAiRestController {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    private ChatClient chatClient;

    public OpenAiRestController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    public String chat(String message){
        String reponse = chatClient.call(message);

        return  reponse;
    }

    @GetMapping("/movies")
    public Map movies(@RequestParam(name = "categorie", defaultValue = "action") String categorie,
                         @RequestParam(name = "annee", defaultValue = "2019") String annee) throws JsonProcessingException {
        OpenAiApi aiApi = new OpenAiApi(apiKey);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel("gpt-4-turbo-preview")
                .withTemperature(0F)
                .withMaxTokens(300)
                .build();
        OpenAiChatClient openAiChatClient = new OpenAiChatClient( aiApi, options);
        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(
                """
                j'ai besoin que tu me donnes le meilleur film de la catégorie donner : {categorie}
                de l'annee donner : {annee}
                la sortie doit être au format json comprenant les champs suivants :
                - categorie < donne la catehorie >
                - annee < donne l'annee >
                - titre < le titre du film >
                - producteur ou productrice < le producteur ou la productrice du film >
                - acteur < une liste de quelque acteur du film >
                - resumer < un petit resumer du film >
                n'ajoute pas ```json   ```
                """
        );

        Prompt prompt = promptTemplate.create(Map.of("categorie",categorie,"annee",annee));
        ChatResponse response = openAiChatClient.call(prompt);

        String content = response.getResult().getOutput().getContent();
        return new ObjectMapper().readValue(content, Map.class);
    }

//http://localhost:8899/sentiment-analysis?revue=J'ai acheté un ordinateur portable et je suis très satisfait de l'écran et du clavier.
    @GetMapping("/sentiment-analysis")
    public Map sentimentAnalysis(String revue) throws JsonProcessingException {
            OpenAiApi aiApi = new OpenAiApi(apiKey);
            OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                    .withModel("gpt-4-turbo-preview")
                    .withTemperature(0F)
                    .withMaxTokens(300)
                    .build();
            OpenAiChatClient openAiChatClient = new OpenAiChatClient(aiApi, openAiChatOptions);

            String systemMessageText = """
                    Effectuez une analyse des sentiments basée sur les aspects des avis sur les ordinateurs portables présentés dans l'entrée délimitée par trois backticks, c'est-à-dire: ```
                    Dans chaque revue, il peut y avoir un ou plusieurs des aspects suivants : écran, clavier et souris.,
                    Pour chaque revue présentée en entrée :",
                    - Identifier s'il y a l'un des 3 aspects (écran, clavier, souris) présents dans la revue.",
                    - Attribuer une polarité de sentiment (positive, négative ou neutre) pour chaque aspect.",
                    - Organiser votre réponse en un objet JSON avec les en-têtes suivants :",
                    	- catégorie : [liste des aspects]",
                    	- polarité : [liste des polarités correspondantes pour chaque aspect]
                    n'ajoute pas ```json   ```
                    	""";
            SystemMessage systemMessage = new SystemMessage(systemMessageText);
            UserMessage userMessage = new UserMessage("```" + revue + "```");
            Prompt zeroShotPrompt = new Prompt(List.of(systemMessage, userMessage)); // Zero-shot prompt

            UserMessage userMessage1 = new UserMessage("```l'écran est très bon, mais le clavier est mauvais, et la souris est moyenne.```");
            SystemMessage systemMessage1 = new SystemMessage("{\"catégorie\": [\"écran\", \"clavier\", \"souris\"],\n\"polarité\": [\"positive\", \"négative\", \"neutre\"]}");
            Prompt fewShotPrompt = new Prompt(List.of(systemMessage, userMessage1,systemMessage1, userMessage)); // Few-shot prompt

            ChatResponse response = openAiChatClient.call(fewShotPrompt);
            String content = response.getResult().getOutput().getContent();
            return new ObjectMapper().readValue(content, Map.class);

    }
}
