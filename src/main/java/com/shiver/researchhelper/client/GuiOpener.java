package com.shiver.researchhelper.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class GuiOpener {

    private static boolean pending = false;

    private GuiOpener() {}

    public static void open() {
        pending = true;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (pending && event.phase == TickEvent.Phase.END) {
            pending = false;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.player != null) {
                mc.displayGuiScreen(new GuiResearchSearch());
            }
        }
    }
}
