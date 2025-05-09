package com.github.tartaricacid.touhoulittlemaid.ai.service.tts;

public enum TTSApiType {
    FISH_AUDIO("fish-audio"),
    GPT_SOVITS("gpt-sovits"),
    PLAYER2("player2"),
    SYSTEM("system");

    private final String name;

    TTSApiType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
