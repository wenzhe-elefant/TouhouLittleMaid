package com.github.tartaricacid.touhoulittlemaid.ai.service.tts.player2;

import java.util.Optional;

/**
 * Valid TTS languages supported by player2 TTS
 */
public enum TTSLanguage {
    AMERICAN_ENGLISH("en_US"),
    BRITISH_ENGLISH("en_GB"),
    JAPANESE("ja_JP"),
    MANDARIN_CHINESE("zh_CN"),
    SPANISH("es_ES"),
    FRENCH("fr_FR"),
    HINDI("hi_IN"),
    ITALIAN("it_IT"),
    BRAZILIAN_PORTUGUESE("pt_BR");

    private final String id;

    TTSLanguage(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Optional<TTSLanguage> fromId(String id) {
        for (TTSLanguage lang : TTSLanguage.values()) {
            if (lang.getId().equals(id)) {
                return Optional.of(lang);
            }
        }
        return Optional.empty();
    }

    public static Optional<TTSLanguage> fromIdApproximate(String idApproximate) {

        // prioritize precise id
        Optional<TTSLanguage> langPrecise = fromId(idApproximate);
        if (langPrecise.isPresent()) {
            return langPrecise;
        }

        // approximate what we pick
        for (TTSLanguage lang : TTSLanguage.values()) {
            String id = lang.getId();
            if (id.equalsIgnoreCase(idApproximate)) {
                return Optional.of(lang);
            }
        }
        return Optional.empty();
    }

}