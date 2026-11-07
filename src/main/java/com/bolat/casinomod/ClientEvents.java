package com.bolat.casinomod;

import com.bolat.casinomod.menu.CasinoScreen;
import com.bolat.casinomod.registry.ModMenus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterMenuScreensEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CasinoMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.CASINO_MENU.get(), CasinoScreen::new);
    }
}
