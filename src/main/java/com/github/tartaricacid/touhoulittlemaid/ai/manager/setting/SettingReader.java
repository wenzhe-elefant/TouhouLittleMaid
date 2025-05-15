package com.github.tartaricacid.touhoulittlemaid.ai.manager.setting;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatData;
import com.github.tartaricacid.touhoulittlemaid.ai.service.Service;
import com.github.tartaricacid.touhoulittlemaid.ai.service.chat.openai.ChatClient;
import com.github.tartaricacid.touhoulittlemaid.ai.service.chat.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.ai.service.chat.openai.request.ResponseFormat;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.models.PlayerMaidModels;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.util.ParseI18n;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SettingReader {
    private static final String SETTING_FOLDER_NAME = "settings";
    private static final Path SETTINGS_FOLDER = Paths.get("config", TouhouLittleMaid.MOD_ID, SETTING_FOLDER_NAME);
    private static final Map<String, CharacterSetting> SETTINGS = Maps.newHashMap();
    private static final Set<String> PROMPT_CHECKED_CHARACTER_NAMES = Sets.newHashSet();
    private static final String YAML = ".yml";

    public static void clear() {
        synchronized (SETTINGS) {
            SETTINGS.clear();
        }
    }

    public static void reloadSettings() {
        if (!Files.exists(SETTINGS_FOLDER)) {
            try {
                Files.createDirectories(SETTINGS_FOLDER);
            } catch (IOException e) {
                TouhouLittleMaid.LOGGER.error("Failed to create settings folder", e);
            }
        }
        // 配置文件里可以读 8 层
        try {
            readConfigSetting(SETTINGS_FOLDER, 8);
        } catch (IOException e) {
            TouhouLittleMaid.LOGGER.error("Failed to read settings file", e);
        }
    }

    public static void readCustomPack(Path rootPath, String domain) {
        Path folder = rootPath.resolve("assets").resolve(domain).resolve(SETTING_FOLDER_NAME);
        if (!folder.toFile().isDirectory()) {
            return;
        }
        try {
            // 模型包里只能读一层
            readConfigSetting(folder, 1);
        } catch (IOException e) {
            TouhouLittleMaid.LOGGER.error("Failed to read settings from " + folder, e);
        }
    }

    public static void readCustomPack(ZipFile zipFile, String domain) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        String folder = String.format("assets/%s/%s/", domain, SETTING_FOLDER_NAME);
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (!entry.isDirectory() && entryName.startsWith(folder) && entryName.endsWith(YAML)) {
                TouhouLittleMaid.LOGGER.debug("Loading settings from {}", entryName);
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    CharacterSetting setting = new CharacterSetting(inputStream);
                    synchronized (SETTINGS) {
                        setting.getModelId().forEach(id -> SETTINGS.put(id, setting));
                    }
                } catch (IOException e) {
                    TouhouLittleMaid.LOGGER.error("Failed to read settings from {}", entryName, e);
                }
            }
        }
    }

    private static void readConfigSetting(Path settingFolder, int maxDepth) throws IOException {
        Files.walkFileTree(settingFolder, EnumSet.noneOf(FileVisitOption.class), maxDepth, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attributes) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(YAML)) {
                    CharacterSetting setting = new CharacterSetting(file.toFile());
                    synchronized (SETTINGS) {
                        setting.getModelId().forEach(id -> SETTINGS.put(id, setting));
                    }
                }
                return super.visitFile(file, attributes);
            }
        });
    }

    private static CharacterSetting generateConfigFromDescription(String modelId, String description) {

        // Make indentation work by removing it entirely.
        description = description.replace("\n", " ");
        if (description.startsWith("Your reply: ")) {
            description = description.substring("Your reply: ".length());
        }
        // remove quotes
        if (description.startsWith("\"") && description.endsWith("\"")) {
            description = description.substring(1, description.length() - 1);
        }
        description = description.trim();

        String raw = String.format("""
meta:
    author: auto_generated
    model_id:
        - %s
setting: |
    # Basic settings
    %s
    # Custom Setting
    ${custom_setting}
    # 获取当前游戏上下文
    当前的时间为：${game_time}，当前的天气是：${weather}。
    你所在的维度为：${dimension}，你所处的生物群系为：${biome}。
    你的右手拿着：${mainhand_item}，你的左手拿着：${offhand_item}。
    你背包内有这些物品：${inventory_items}，你穿戴的护甲：${armor_items}。
    你的当前血量是：${healthy}，你身上有这些药水效果：${effects}。
    我的当前血量是：${owner_healthy}。

    # Output character limit
    # 输出限制
    回复长度建议限制在64个字符以内。

    # Format requirements
    # 格式要求
    回复中不包含行为或表情类的旁白性质的词语。
    输出格式为 JSON 格式：${output_json_format}。

    # Response language type
    # 回复语言类型
    文字（chat_text）字段为${chat_language}回复，语音（tts_text）字段为${chat_language}回复翻译过来的${tts_language}回复。
""", modelId, description);

        try {
            return new CharacterSetting(new ByteArrayInputStream(raw.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateMaidSettingsIfNotPresent(MaidAIChatData chatData, String modelId) {
        // one at a time guard
        if (PROMPT_CHECKED_CHARACTER_NAMES.contains(modelId)) {
            return;
        }
        PROMPT_CHECKED_CHARACTER_NAMES.add(modelId);

        CharacterSetting result;
        synchronized (SETTINGS) {
            result = SETTINGS.get(modelId);
        }
        if (result == null) {

            String systemPrompt = "You are writing a simple description on who the following maid is, to be further used for chat prompts. "
                    + "Information will be given and you must reply with ONLY a prompt text within the quotations (do NOT include the quotations).\n"
                    + "Some maids are from the popular Touhou Project series, so if you are aware use information from said series to prompt this model.\n"
                    + "Here is an example input and an example output:\n"
                    + "Input: \"Name: Reimu Hakurei. Description: Shrine Maiden of Paradise.\"\n"
                    + "Your reply (IN BETWEEN the quotes): \"You are Reimu Hakurei, the Shrine Maiden of Paradise. You are sympathetic, your shrine is a somewhat popular destination in your home town of Gensokyo, and you spend your time maintaining the shrine and exterminating youkai.\n" +
                    "Call me \"${owner_name}\" with an easygoing and carefree tone.\n" +
                    "You are somewhat simpleminded and open about your emotions and whatever ideas come to the top of your head. Straightforward but likeable.\n" +
                    "\"";

            MaidModelInfo info = CustomPackLoader.MAID_MODELS.getInfo(modelId).orElse(new MaidModelInfo());

            String prompt = "Name: " + I18n.get(ParseI18n.getI18nKey(info.getName())) + ". Description: " + String.join(", ", info.getDescription().stream().map(d -> I18n.get(ParseI18n.getI18nKey(d))).toList())  + ".";

            // run the prompt
            String model = chatData.getChatModel();
            double chatTemperature = 0.5;
            ChatCompletion chatCompletion = ChatCompletion.create()
                    .model(model)
                    .temperature(chatTemperature)
                    .setResponseFormat(ResponseFormat.text())
                    .systemChat(systemPrompt)
                    .assistantChat(prompt);
            if (chatData.getChatSite() == null) {
                // no chat site, don't prompt anything.
                return;
            }
            ChatClient chatClient = Service.getChatClient(chatData.getChatSite());
            chatClient.chat(chatCompletion).handle(chatCompletionResponse -> {
                String ourDescription = chatCompletionResponse.getFirstChoiceMessage();
                synchronized (SETTINGS) {
                    try {
                        SETTINGS.put(modelId, generateConfigFromDescription(modelId, ourDescription));
                    } catch (Exception e) {
                        System.out.println("FAILED to generate config for modelId: " + modelId);
                        e.printStackTrace();
                    }
                }
            }, Throwable::printStackTrace);
        }
    }

    public static Optional<CharacterSetting> getSetting(@NotNull String modelId) {
        CharacterSetting result;
        synchronized (SETTINGS) {
            result = SETTINGS.get(modelId);
        }

        // always ensure something is present
        if (result == null) {
            MaidModelInfo info = CustomPackLoader.MAID_MODELS.getInfo(modelId).orElse(new MaidModelInfo());

            String genericDescription = String.format("You are a maid here to help the player out. Your name is %s with the provided description: %s. Please be very accomodating and nice to the player, you are eager to help! wow!", I18n.get(I18n.get(ParseI18n.getI18nKey(info.getName()))), String.join(", ", info.getDescription().stream().map(d -> I18n.get(I18n.get(ParseI18n.getI18nKey(d)))).toList()));
            result = generateConfigFromDescription(modelId, genericDescription);
        }

        return Optional.of(result);
    }

    public static Set<String> getAllSettingKeys() {
        synchronized (SETTINGS) {return SETTINGS.keySet();}
    }
}
