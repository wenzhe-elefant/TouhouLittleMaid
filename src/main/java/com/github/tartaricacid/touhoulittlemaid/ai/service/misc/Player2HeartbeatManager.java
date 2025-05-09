package com.github.tartaricacid.touhoulittlemaid.ai.service.misc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.net.HttpHeaders;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Player2HeartbeatManager {
    private static final String PLAYER2_GAME_KEY = "player2-game-key";

    private final ScheduledExecutorService heartbeatScheduler;
    private final HttpClient httpClient;

    private ScheduledFuture<?> heartbeatTaskHandle;

    private String baseUrl = "";

    private Player2HeartbeatManager(HttpClient httpClient) {
        this.httpClient = httpClient;

        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public static Player2HeartbeatManager create(final HttpClient httpClient) {
        return new Player2HeartbeatManager(httpClient);
    }

    private static String getHeartbeatUrl() {
        return "/heartbeat";
    }

    public Player2HeartbeatManager baseUrl(final String baseUrl) {
        if (baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            this.baseUrl = baseUrl;
        }
        return this;
    }

    public void startHeartbeats() {
        if (heartbeatTaskHandle != null) {
            heartbeatTaskHandle.cancel(true);
            heartbeatScheduler.shutdown();
            heartbeatTaskHandle = null;
        }
        heartbeatTaskHandle = heartbeatScheduler.scheduleAtFixedRate(() -> {
            sendHeartbeat();
        }, 5, 60, TimeUnit.SECONDS);

    }
    public void stopHeartbeats() {
        if (heartbeatTaskHandle != null) {
            heartbeatTaskHandle.cancel(true);
            heartbeatScheduler.shutdown();
            heartbeatTaskHandle = null;
        }
    }

    private void sendHeartbeat() {
        System.out.println("Sending Heartbeat");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .header(PLAYER2_GAME_KEY, "TouhouLittleMaid")
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(2))
                .uri(URI.create(baseUrl + Player2HeartbeatManager.getHeartbeatUrl()));
        HttpRequest httpRequest = builder.build();
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
            .whenComplete((response, throwable) -> {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (json.has("client_version")) {
                    System.out.println("Heartbeat Successful");
                }
            });
    }
}
