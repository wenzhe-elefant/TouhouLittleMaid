package com.github.tartaricacid.touhoulittlemaid.ai.service.tts.player2.request;

import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.TTSRequest;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TTSPlayer2Request implements TTSRequest {
    @SerializedName("play_in_app")
    private boolean playInApp;

    @SerializedName("speed")
    private float speed;

    @SerializedName("voice_language")
    private String voiceLanguage;

    @SerializedName("voice_gender")
    private String voiceGender;

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
    public TTSPlayer2Request setVoiceLanguage(String voiceLanguage) {
        this.voiceLanguage = voiceLanguage;
        return this;
    }
    public TTSPlayer2Request setVoiceGender(String voiceGender) {
        this.voiceGender = voiceGender;
        return this;
    }
    public TTSPlayer2Request setVoiceIds(List<String> voiceIds) {
        this.voiceIds = voiceIds;
        return this;
    }

}
