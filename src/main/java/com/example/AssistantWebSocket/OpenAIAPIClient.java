//package com.example.AssistantWebSocket;
//
//import okhttp3.*;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import java.io.IOException;
//import java.util.concurrent.TimeUnit;
//
//public class OpenAIAPIClient {
//    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
//    private static final String API_KEY = "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 ";
//    private final OkHttpClient client;
//
//    public OpenAIAPIClient() {
//        this.client = new OkHttpClient.Builder()
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(30, TimeUnit.SECONDS)
//                .build();
//    }
//
//    public void processImage(String imageUrl, Callback callback) throws IOException {
//        JSONObject jsonPayload = new JSONObject();
//        jsonPayload.put("model", "gpt-4o-2024-08-06");
//
//        JSONArray messages = new JSONArray();
//
//        // System message for context
//        JSONObject systemMessage = new JSONObject();
//        systemMessage.put("role", "system");
//        systemMessage.put("content", "You are an AI interview assistant. Analyze the image content and generate appropriate responses. If the image contains a programming question, solve it in Java and Python. Otherwise, provide a detailed and relevant response.");
//        messages.put(systemMessage);
//
//        // User message with image input
//        JSONObject userMessage = new JSONObject();
//        userMessage.put("role", "user");
//
//        JSONArray contentArray = new JSONArray();
//        JSONObject imagePart = new JSONObject();
//        imagePart.put("type", "image_url");
//        imagePart.put("image_url", new JSONObject().put("url", imageUrl));
//        contentArray.put(imagePart);
//
//        userMessage.put("content", contentArray);
//        messages.put(userMessage);
//        jsonPayload.put("messages", messages);
//
//        RequestBody requestBody = RequestBody.create(jsonPayload.toString(), MediaType.get("application/json"));
//        Request request = new Request.Builder()
//                .url(OPENAI_API_URL)
//                .addHeader("Authorization", "Bearer " + API_KEY)
//                .addHeader("Content-Type", "application/json")
//                .post(requestBody)
//                .build();
//
//        client.newCall(request).enqueue(callback);
//    }
//}
package com.example.AssistantWebSocket;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OpenAIAPIClient {
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";

    private final OkHttpClient client;

    // Global conversation history: stores messages in order (system, user, assistant, etc.)
    private final JSONArray conversationHistory;

    public OpenAIAPIClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Initialize the conversation history and add a system context message
        conversationHistory = new JSONArray();
        addSystemMessage("You are an AI Trder professional assistant. Analyze the image content and generate appropriate responses. " +
                "If the image contains a programming question, solve it in Java and Python. Otherwise, provide a detailed and relevant response.");
    }

    // Helper method to add a system message
    private void addSystemMessage(String content) {
        JSONObject message = new JSONObject();
        message.put("role", "system");
        message.put("content", content);
        conversationHistory.put(message);
    }

    // Helper method to add a message to the conversation history
    private void addMessage(String role, Object content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        conversationHistory.put(message);
    }

    /**
     * Processes an image input by the user.
     * The method creates a new conversation message with an image URL, updates the history,
     * and sends the entire history to the OpenAI API.
     *
     * @param imageUrl the URL of the image input.
     * @param callback the OkHttp callback to handle the response.
     * @throws IOException if an IO error occurs.
     */
    public void processImage(String imageUrl, Callback callback) throws IOException {
        // Create a JSON object representing the user message that contains an image
        JSONArray contentArray = new JSONArray();
        JSONObject imagePart = new JSONObject();
        imagePart.put("type", "image_url");
        imagePart.put("image_url", new JSONObject().put("url", imageUrl));
        contentArray.put(imagePart);

        // Add the image message to history
        addMessage("user", contentArray);

        // Build the complete payload including the conversation history
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("model", "gpt-4o-2024-08-06");
        jsonPayload.put("messages", conversationHistory);

        RequestBody requestBody = RequestBody.create(
                jsonPayload.toString(),
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        // When the response is received, consider adding the assistant response to the conversation history
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Extract the assistant response from API response; this example assumes a JSON reply with choices field
                String responseStr = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseStr);
                if (jsonResponse.has("choices")) {
                    String assistantReply = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    // Update conversation history with assistant message
                    addMessage("assistant", assistantReply);
                }
                // Forward the response to the original callback
                callback.onResponse(call, response.newBuilder().body(ResponseBody.create(responseStr, response.body().contentType())).build());
            }
        });
    }

    /**
     * Processes a text input from the user.
     * This method adds the text to the conversation history and sends the complete context to the OpenAI API.
     *
     * @param text the text input from the user.
     * @param callback the OkHttp callback to handle the response.
     * @throws IOException if an IO error occurs.
     */
    public void processText(String text, Callback callback) throws IOException {
        // Add user text to conversation history
        addMessage("user", text);

        // Build the complete payload including conversation history
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("model", "gpt-4o-2024-08-06");
        jsonPayload.put("messages", conversationHistory);

        RequestBody requestBody = RequestBody.create(
                jsonPayload.toString(),
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        // Send the request and update conversation history with the assistant response when received
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseStr);
                if (jsonResponse.has("choices")) {
                    String assistantReply = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    // Update conversation history with the assistant's reply
                    addMessage("assistant", assistantReply);
                }
                // Return the response via the callback
                callback.onResponse(call, response.newBuilder().body(ResponseBody.create(responseStr, response.body().contentType())).build());
            }
        });
    }
}
