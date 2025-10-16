package com.example.AssistantWebSocket;


import autovalue.shaded.com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.apache.http.HttpException;

import java.io.IOException;


public class Gemini {

//    Client client = new Client();

    Client client = Client.builder().apiKey("").build();


    public void processScreenShare(Byte[] base64Image) throws HttpException, IOException {

        // Create text part for the prompt
        Part textPart = Part.builder().text("i am sharing you screen share of my computer i want you to tell me what you see and generate content from what you can see").build();

        // Create image part with inline data (assuming API supports base64)
        // Hypothetical syntax; adjust based on actual Gemini API documentation
//        String base64Image = "your_base64_string_here"; // Your base64-encoded image string
        Part imagePart = Part.fromJson(
                "{\"inlineData\":{\"mimeType\":\"image/jpeg\",\"data\":\"" + base64Image + "\"}}"
        );

        // Build content with both parts
        Content content = Content.builder()
                .role("user")
                .parts(ImmutableList.of(textPart, imagePart))
                .build();

        // Use Gemini client to generate content
        GenerateContentResponse response = client.models.generateContent(
                "gemini-2.0-flash-001", content, null
        );

        String responseText = response.text();
        System.out.println(responseText);
    }
}
