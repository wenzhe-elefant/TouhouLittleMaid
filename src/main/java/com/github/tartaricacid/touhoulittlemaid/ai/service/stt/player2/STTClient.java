package com.github.tartaricacid.touhoulittlemaid.ai.service.stt.player2;

import com.github.tartaricacid.touhoulittlemaid.ai.service.stt.player2.response.Message;
import com.github.tartaricacid.touhoulittlemaid.ai.service.stt.player2.response.STTCallback;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

public class STTClient {
    private final HttpClient httpClient;
    private String baseUrl = "";
    private String siteName = "";

    private STTClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static STTClient create(final HttpClient httpClient) {
        return new STTClient(httpClient);
    }

    private static String getStartUrl() {
        return "/start";
    }

    private static String getStopUrl() {
        return "/stop";
    }

    public STTClient baseUrl(final String baseUrl) {
        if (baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            this.baseUrl = baseUrl;
        }
        return this;
    }

    public STTClient siteName(final String siteName) {
        this.siteName = siteName;
        return this;
    }

    public void start(Consumer<Message> consumer, Consumer<Throwable> failConsumer) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
                .POST(HttpRequest.BodyPublishers.ofString("{\"timeout\":30}"))
                .timeout(Duration.ofSeconds(20))
                .uri(URI.create(baseUrl + STTClient.getStartUrl()));
        if (Objects.equals(this.siteName, "player2")) {
            builder.header("player2-game-key", "TouHouLittleMaid");
        }
        HttpRequest httpRequest = builder.build();
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, throwable) -> {
                    STTCallback callback = new STTCallback(consumer);
                    if (throwable != null) {
                        callback.onFailure(httpRequest, throwable);
                        failConsumer.accept(throwable);
                    } else {
                        callback.onResponse(response, failConsumer);
                    }
                });
    }

    public void stop(Consumer<Message> consumer, Consumer<Throwable> failConsumer) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(20))
                .uri(URI.create(baseUrl + STTClient.getStopUrl()));
        if (Objects.equals(this.siteName, "player2")) {
            builder.header("player2-game-key", "TouHouLittleMaid");
        }
        HttpRequest httpRequest = builder.build();
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, throwable) -> {
                    STTCallback callback = new STTCallback(consumer);
                    if (throwable != null) {
                        callback.onFailure(httpRequest, throwable);
                        failConsumer.accept(throwable);
                    } else {
                        callback.onResponse(response, failConsumer);
                    }
                });
    }
}
