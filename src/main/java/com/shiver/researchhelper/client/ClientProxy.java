package com.shiver.researchhelper.client;

import com.shiver.researchhelper.CommonProxy;
import com.shiver.researchhelper.Tags;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static com.shiver.researchhelper.ResearchHelper.itemResearchCompass;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(GuiOpener.class);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void registerModels() {
        Item item = itemResearchCompass;
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(Tags.MOD_ID + ":research_compass", "inventory"));
        }
    }

    @Override
    public void openResearchGui() {
        GuiOpener.open();
    }
}
