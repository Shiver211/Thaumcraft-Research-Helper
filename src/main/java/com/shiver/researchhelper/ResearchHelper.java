package com.shiver.researchhelper;

import com.shiver.researchhelper.core.GraphBuilder;
import com.shiver.researchhelper.item.ItemResearchCompass;
import com.shiver.researchhelper.tab.CreativeTabResearchHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:thaumcraft")
public class ResearchHelper {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.Instance(Tags.MOD_ID)
    public static ResearchHelper instance;

    @SidedProxy(clientSide = "com.shiver.researchhelper.client.ClientProxy",
                serverSide = "com.shiver.researchhelper.CommonProxy")
    public static CommonProxy proxy;

    public static ItemResearchCompass itemResearchCompass;
    public static CreativeTabs creativeTab;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("ResearchHelper starting up.");
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    /**
     * 在所有模组加载完成后构建研究图谱。
     * 仅在客户端构建，因为图谱仅用于显示。
     */
    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        if (event.getSide().isClient()) {
            GraphBuilder.build();
        }
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            creativeTab = new CreativeTabResearchHelper();

            itemResearchCompass = new ItemResearchCompass();
            itemResearchCompass.setRegistryName(Tags.MOD_ID, "research_compass");
            itemResearchCompass.setTranslationKey(Tags.MOD_ID + ".research_compass");
            itemResearchCompass.setCreativeTab(creativeTab);
            event.getRegistry().register(itemResearchCompass);
            LOGGER.info("Registered item: {}", itemResearchCompass.getRegistryName());
        }

        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            proxy.registerModels();
        }
    }
}
