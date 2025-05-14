package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.AvailableSites;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.SettingReader;
import com.github.tartaricacid.touhoulittlemaid.client.event.PressAIChatKeyEvent;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.ai.AIChatScreen;
import com.github.tartaricacid.touhoulittlemaid.util.ByteBufUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SyncAiSettingMessage {
    private final Set<String> settings;
    private final Map<String, List<String>> chatSites;
    private final Map<String, List<String>> ttsSites;

    public SyncAiSettingMessage() {
        this.settings = SettingReader.getAllSettingKeys();
        this.chatSites = AvailableSites.getClientChatSites();
        this.ttsSites = AvailableSites.getClientTtsSites();
    }

    public SyncAiSettingMessage(Set<String> settings, Map<String, List<String>> chatSites, Map<String, List<String>> ttsSites) {
        this.settings = settings;
        this.chatSites = chatSites;
        this.ttsSites = ttsSites;
    }

    public static void encode(SyncAiSettingMessage message, FriendlyByteBuf buf) {
        ByteBufUtils.writeStringSet(message.settings, buf);
        ByteBufUtils.writeSites(message.chatSites, buf);
        ByteBufUtils.writeSites(message.ttsSites, buf);
    }

    public static SyncAiSettingMessage decode(FriendlyByteBuf buf) {
        Set<String> settings = ByteBufUtils.readStringSet(buf);
        Map<String, List<String>> chatSites = ByteBufUtils.readSites(buf);
        Map<String, List<String>> ttsSites = ByteBufUtils.readSites(buf);
        return new SyncAiSettingMessage(settings, chatSites, ttsSites);
    }

    public static void handle(SyncAiSettingMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handle(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SyncAiSettingMessage message) {
        AIChatScreen.CLIENT_CHAT_SITES.clear();
        AIChatScreen.CLIENT_CHAT_SITES.putAll(message.chatSites);
        AIChatScreen.CLIENT_TTS_SITES.clear();
        AIChatScreen.CLIENT_TTS_SITES.putAll(message.ttsSites);
    }
}
