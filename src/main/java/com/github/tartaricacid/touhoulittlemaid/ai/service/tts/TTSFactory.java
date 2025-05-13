package com.github.tartaricacid.touhoulittlemaid.ai.service.tts;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.Site;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.fishaudio.TTSFishAudioClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.fishaudio.request.Format;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.fishaudio.request.OpusBitRate;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.fishaudio.request.TTSFishAudioRequest;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.gptsovits.TTSGptSovitsClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.gptsovits.request.TTSGptSovitsRequest;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.player2.TTSLanguage;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.player2.TTSPlayer2Client;
import com.github.tartaricacid.touhoulittlemaid.ai.service.tts.player2.request.TTSPlayer2Request;
import com.mojang.authlib.minecraft.client.MinecraftClient;

import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.Optional;

public final class TTSFactory {

    // only run this once
    private static boolean ranFailedTTSMessage = false;

    @Nullable
    public static TTSClient<?> getTtsClient(HttpClient client, Site site) {
        if (site.getApiType().equals(TTSApiType.FISH_AUDIO.getName())) {
            String ttsApiKey = site.getApiKey();
            String ttsBaseUrl = site.getUrl();
            return TTSFishAudioClient.create(client)
                    .apiKey(ttsApiKey)
                    .baseUrl(ttsBaseUrl);
        }

        if (site.getApiType().equals(TTSApiType.GPT_SOVITS.getName())) {
            String ttsApiKey = site.getApiKey();
            String ttsBaseUrl = site.getUrl();
            return TTSGptSovitsClient.create(client)
                    .apiKey(ttsApiKey)
                    .baseUrl(ttsBaseUrl);
        }

        if (site.getApiType().equals(TTSApiType.PLAYER2.getName())) {
            String ttsBaseUrl = site.getUrl();
            return TTSPlayer2Client.create(client)
                    .baseUrl(ttsBaseUrl);
        }

        return null;
    }

    @Nullable
    public static TTSRequest getTtsRequest(Site site, String ttsText, String ttsLang, String model) {

        if (site.getApiType().equals(TTSApiType.FISH_AUDIO.getName())) {
            return TTSFishAudioRequest.create()
                    .setReferenceId(model)
                    .setFormat(Format.OPUS)
                    // OPUS 极低比特率情况下，音质效果也还不错
                    .setOpusBitrate(OpusBitRate.LOWEST)
                    .setText(ttsText);
        }

        if (site.getApiType().equals(TTSApiType.GPT_SOVITS.getName())) {
            return TTSGptSovitsRequest.create()
                    .setText(ttsText)
                    .setTextLang(ttsLang)
                    .setSiteExtraArgs(site);
        }

        if (site.getApiType().equals(TTSApiType.PLAYER2.getName())) {

            TTSLanguage defaultLang = TTSLanguage.AMERICAN_ENGLISH;

            // make sure language is supported by player2
            Optional<TTSLanguage> lang = TTSLanguage.fromIdApproximate(ttsLang);
            if (lang.isEmpty()) {
                if (ttsLang != null) {
                    if (!ranFailedTTSMessage) {
                        // Let user know that given lang isn't valid and print a list of valid langs
                        String message = "tts language \"" + ttsLang + "\" not valid for player2.\n\n"
                                +  "Valid languages are: ["
                                + String.join(", ", Arrays.stream(TTSLanguage.values()).map(TTSLanguage::getId).toList())
                                + "].\n\nDefaulting to " + defaultLang.getId() + ".";
                        MutableComponent messageComp = Component.literal(message);
                        Minecraft.getInstance().player.sendSystemMessage(messageComp);
                        ranFailedTTSMessage = true;
                    }
                }
                lang = Optional.of(defaultLang);
            } else {
                // allow another failure if we switch.
                ranFailedTTSMessage = false;
            }

            String ttsLangId = lang.get().getId();

            return TTSPlayer2Request.create()
                    .setText(ttsText)
                    .setVoiceLanguage(ttsLangId)
                    .setVoiceGender("female")
                    .setSpeed(1)
                    .setPlayInApp(true)
                    // TODO: Figure out where to get this from
                    .setVoiceIds(Arrays.asList());
        }

        return null;
    }
}
