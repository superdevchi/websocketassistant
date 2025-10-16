package com.example.AssistantWebSocket;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class GrokAPIClient {
    private static final String GROK_API_URL = "https://api.x.ai/v1/chat/completions";
    private static final String API_KEY = "";
    private final OkHttpClient client;
    private static final int MAX_TOKENS = 32768;

    public GrokAPIClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS) // Increase read timeout if necessary
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void processImageBase64(String base64Image, Callback callback) throws IOException {
        int tokenCount = base64Image.length() / 4 + 50;
        if (tokenCount > MAX_TOKENS) {
            throw new IOException("Base64 string too large: " + tokenCount + " tokens exceed limit of " + MAX_TOKENS);
        }

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("model", "grok-2-vision-1212");

        JSONArray messages = new JSONArray();

        // System role message
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are an interview assistant answer all possible solutions to content sent to you.");
        messages.put(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");

        // Content array with text and image
        JSONArray contentArray = new JSONArray();
//        JSONObject textPart = new JSONObject();
//        textPart.put("type", "text");
//        textPart.put("text", "extract all text from image and from what you can extract provide answers to them");
//        contentArray.put(textPart);

        JSONObject imagePart = new JSONObject();
        imagePart.put("type", "image_url");
        imagePart.put("image_url", new JSONObject().put("url", "data:image/jpeg;base64," + base64Image));
        contentArray.put(imagePart);

        userMessage.put("content", contentArray);
        messages.put(userMessage);
        jsonPayload.put("messages", messages);

        RequestBody requestBody = RequestBody.create(
                jsonPayload.toString(), MediaType.get("application/json")
        );
        Request request = new Request.Builder()
                .url(GROK_API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
