//package com.example.AssistantWebSocket;
//
//
//import org.apache.tomcat.util.json.JSONParser;
//import org.checkerframework.checker.units.qual.A;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import java.security.Principal;
//
//@Controller
//public class MessageController {
//
//    private final Gemini gemini = new Gemini();
//
//    private final GrokAPIClient grokAPIClient = new GrokAPIClient();
//
//    private final ImageUtils imageUtils = new ImageUtils();
//
//    @Autowired
//    private SimpMessagingTemplate simpMessagingTemplate;
//
//
////    private final OpenAIGrok grok = new OpenAIGrok("xai-4fVkQMRvt0CL9ftTYOOEK9VrihSGDGUj7q37i14rworjAoNB3AKbifggB26bPLuNDE2efUSxKZ1y5Cyh");
//
//    @MessageMapping("/chat")
////    @SendTo("/topic/messages")
//    public String send(@Payload Message message, Principal principal) throws Exception {
//        // Simulate processing delay
////        Thread.sleep(1000);
//        System.out.println( "message gotten" + message);
////        System.out.println(grokAPIClient.processImageBase64(imageUtils.urlToBase64(message.getText(), 0.3f,400)));
//
//        System.out.println("session id " + principal.getName());
//        String response = grokAPIClient.processImageBase64(imageUtils.urlToBase64(message.getText(), 0.3f, 400));
////        System.out.println(response);
//
//        // Send the response to the specific user using their session ID
//        simpMessagingTemplate.convertAndSendToUser(principal.getName(), "/queue/messages", response);
//        return response;
//    }
//}


package com.example.AssistantWebSocket;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.security.Principal;

@Controller
public class MessageController {

    private final Gemini gemini = new Gemini();
    private final GrokAPIClient grokAPIClient = new GrokAPIClient();
    private final ImageUtils imageUtils = new ImageUtils();
    private final OpenAIAPIClient openAIAPIClient = new OpenAIAPIClient();
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat")
    public void send(@Payload Message message, Principal principal) throws IOException {
        System.out.println("Message received: " + message);
        System.out.println("Session ID: " + principal.getName());

        // Convert image URL (or any string) to a base64 encoded image string.
        // Adjust parameters as needed.
//        String base64Image = imageUtils.urlToBase64(message.getText(), 0.3f, 400);

        try {
            openAIAPIClient.processImage(message.getText(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String errorMessage = "Error processing image: " + e.getMessage();
                    System.err.println(errorMessage);
                    // Send error response back to the user
                    simpMessagingTemplate.convertAndSendToUser(
                            principal.getName(),
                            "/queue/messages",
                            errorMessage
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorMessage = "Unexpected response code: " + response.code() + ", " + response.body().string();
                        simpMessagingTemplate.convertAndSendToUser(
                                principal.getName(),
                                "/queue/messages",
                                errorMessage
                        );
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    String resultContent = jsonResponse
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    // Send the successful response back to the specific user
                    simpMessagingTemplate.convertAndSendToUser(
                            principal.getName(),
                            "/queue/messages",
                            resultContent
                    );
                }
            });
        } catch (IOException ex) {
            String errorMessage = "Error: " + ex.getMessage();
            simpMessagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/messages",
                    errorMessage
            );
        }
    }

    @MessageMapping("/chat/text")
    public void message(@Payload Message message, Principal principal) throws IOException {

        System.out.println("Message received: " + message);
        System.out.println("Session ID: " + principal.getName());
        try{

            openAIAPIClient.processText(message.getText(), new Callback() {

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    String errorMessage = "Error processing text: " + e.getMessage();
                    System.err.println(errorMessage);
                    // Send error response back to the user
                    simpMessagingTemplate.convertAndSendToUser(
                            principal.getName(),
                            "/queue/messages",
                            errorMessage
                    );
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorMessage = "Unexpected response code: " + response.code() + ", " + response.body().string();
                        simpMessagingTemplate.convertAndSendToUser(
                                principal.getName(),
                                "/queue/messages",
                                errorMessage
                        );
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    String resultContent = jsonResponse
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");


                    System.out.println(resultContent);
                    // Send the successful response back to the specific user
                    simpMessagingTemplate.convertAndSendToUser(
                            principal.getName(),
                            "/queue/messages",
                            resultContent
                    );
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @MessageMapping("/chat/text/trades")
    public void tradeai(@Payload Message message, Principal principal) throws IOException {

        System.out.println("Message received: " + message);
        System.out.println("Session ID: " + principal.getName());
        try{

            openAIAPIClient.processText(message.getText(), new Callback() {

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    String errorMessage = "Error processing text: " + e.getMessage();
                    System.err.println(errorMessage);
                    // Send error response back to the user
                    simpMessagingTemplate.convertAndSendToUser(
                            message.getFrom(),
                            "/queue/messages",
                            errorMessage
                    );
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorMessage = "Unexpected response code: " + response.code() + ", " + response.body().string();
                        simpMessagingTemplate.convertAndSendToUser(
                                message.getFrom(),
                                "/queue/messages",
                                errorMessage
                        );
                        return;
                    }
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    String resultContent = jsonResponse
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");


                    System.out.println(resultContent);
                    // Send the successful response back to the specific user
                    simpMessagingTemplate.convertAndSendToUser(
                            message.getFrom(),
                            "/queue/messages",
                            resultContent
                    );
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @MessageMapping("/chat/trades")
    public void trades(@Payload Message message, Principal principal) throws IOException {

        System.out.println("Message received: " + message);
        System.out.println("Session ID: " + principal.getName());

        simpMessagingTemplate.convertAndSendToUser(
                            message.getFrom(),
                            "/queue/messages",
                            message.getText()
                    );
    }
}
