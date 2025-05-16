package com.github.tartaricacid.touhoulittlemaid.ai.manager.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseChat {
    @SerializedName("chat_text")
    public String chatText = "";

    @SerializedName("tts_text")
    public String ttsText = "";

    @SerializedName("optional_command")
    public String optionalCommand = "";

    public String getChatText() {
        return chatText;
    }

    public String getTtsText() {
        return ttsText;
    }

    public String getOptionalCommand() {
        return optionalCommand;
    }
}
