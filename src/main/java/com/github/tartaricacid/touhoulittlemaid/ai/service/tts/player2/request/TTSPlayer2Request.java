package com.github.tartaricacid.touhoulittlemaid.ai.service.tts.player2.request;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.Site;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSRequest;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TTSPlayer2Request implements TTSRequest {
    @SerializedName("play_in_app")
    private boolean playInApp;

    @SerializedName("speed")
    private float speed;

    @SerializedName("language")
    private String language;

    @SerializedName("text")
    private String text;

    @SerializedName("voice_ids")
    private List<String> voiceIds;

    public static TTSPlayer2Request create() {
        return new TTSPlayer2Request();
    }

    private TTSPlayer2Request() {
    }

    public TTSPlayer2Request setText(String text) {
        this.text = text;
        return this;
    }
    public TTSPlayer2Request setSpeed(float speed) {
        this.speed = speed;
        return this;
    }
    public TTSPlayer2Request setPlayInApp(boolean playInApp) {
        this.playInApp = playInApp;
        return this;
    }
    public TTSPlayer2Request setLanguage(String language) {
        this.language = language;
        return this;
    }
    public TTSPlayer2Request setVoiceIds(List<String> voiceIds) {
        this.voiceIds = voiceIds;
        return this;
    }

}
