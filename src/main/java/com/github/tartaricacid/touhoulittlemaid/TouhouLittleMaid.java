package com.github.tartaricacid.touhoulittlemaid;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.AvailableSites;
import com.github.tartaricacid.touhoulittlemaid.ai.service.Service;
import com.github.tartaricacid.touhoulittlemaid.ai.service.misc.Player2HeartbeatManager;
import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.compat.aquaculture.AquacultureCompat;
import com.github.tartaricacid.touhoulittlemaid.config.GeneralConfig;
import com.github.tartaricacid.touhoulittlemaid.config.ServerConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.init.InitBlocks;
import com.github.tartaricacid.touhoulittlemaid.init.InitCommand;
import com.github.tartaricacid.touhoulittlemaid.init.InitContainer;
import com.github.tartaricacid.touhoulittlemaid.init.InitCreativeTabs;
import com.github.tartaricacid.touhoulittlemaid.init.InitEnchantments;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.init.InitLootModifier;
import com.github.tartaricacid.touhoulittlemaid.init.InitPoi;
import com.github.tartaricacid.touhoulittlemaid.init.InitRecipes;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.init.InitTrigger;
import com.google.common.collect.Lists;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TouhouLittleMaid.MOD_ID)
public final class TouhouLittleMaid {
    public static final String MOD_ID = "touhou_little_maid";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static List<ILittleMaid> EXTENSIONS = Lists.newArrayList();

    public TouhouLittleMaid() {
        initRegister(FMLJavaModLoadingContext.get().getModEventBus());
        InitTrigger.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GeneralConfig.init());
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.init());
        ChatBubbleManger.initDefaultChat();

        AquacultureCompat.init();
    }

    private static void initRegister(IEventBus eventBus) {
        InitEntities.ENTITY_TYPES.register(eventBus);
        InitEntities.ATTRIBUTES.register(eventBus);
        InitEntities.MEMORY_MODULE_TYPES.register(eventBus);
        InitEntities.SENSOR_TYPES.register(eventBus);
        InitEntities.SCHEDULES.register(eventBus);
        InitEntities.DATA_SERIALIZERS.register(eventBus);
        InitEntities.ACTIVITIES.register(eventBus);
        InitBlocks.BLOCKS.register(eventBus);
        InitBlocks.TILE_ENTITIES.register(eventBus);
        InitItems.ITEMS.register(eventBus);
        InitEnchantments.ENCHANTMENTS.register(eventBus);
        InitCreativeTabs.TABS.register(eventBus);
        InitContainer.CONTAINER_TYPE.register(eventBus);
        InitSounds.SOUNDS.register(eventBus);
        InitRecipes.RECIPE_SERIALIZERS.register(eventBus);
        InitLootModifier.GLOBAL_LOOT_MODIFIER_SERIALIZER.register(eventBus);
        InitCommand.ARGUMENT_TYPE.register(eventBus);
        InitPoi.POI_TYPES.register(eventBus);
    }
}
