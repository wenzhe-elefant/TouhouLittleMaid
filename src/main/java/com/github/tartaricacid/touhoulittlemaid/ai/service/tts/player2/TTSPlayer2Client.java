package com.github.tartaricacid.touhoulittlemaid.ai.service.tts.player2;

import com.github.tartaricacid.touhoulittlemaid.ai.service.Service;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.player2.request.TTSPlayer2Request;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.player2.response.TTSPlayer2Callback;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

public class TTSPlayer2Client implements TTSClient<TTSPlayer2Request> {

    private static final String PLAYER2_GAME_KEY = "player2-game-key";

    private final HttpClient httpClient;
    private String baseUrl = "";
    private TTSPlayer2Request request;

    public static TTSPlayer2Client create(final HttpClient httpClient) {
        return new TTSPlayer2Client(httpClient);
    }

    private TTSPlayer2Client(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public TTSPlayer2Client baseUrl(final String baseUrl) {
        if (baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            this.baseUrl = baseUrl;
        }
        return this;
    }

    public TTSPlayer2Client request(TTSPlayer2Request request) {
        this.request = request;
        return this;
    }

    public void handle(Consumer<byte[]> consumer, Consumer<Throwable> failConsumer) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                .header(PLAYER2_GAME_KEY, "TouhouLittleMaid")
                .POST(HttpRequest.BodyPublishers.ofString(Service.GSON.toJson(request)))
                .timeout(Duration.ofSeconds(20))
                .uri(URI.create(baseUrl))
                .build();
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
                .whenComplete((response, throwable) -> {
                    TTSPlayer2Callback callback = new TTSPlayer2Callback(consumer);
                    if (throwable != null) {
                        callback.onFailure(httpRequest, throwable);
                        failConsumer.accept(throwable);
                    } else {
                        callback.onResponse(response, failConsumer);
                    }
                });
    }
}
