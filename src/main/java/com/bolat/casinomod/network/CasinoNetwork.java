package com.bolat.casinomod.network;

import com.bolat.casinomod.CasinoMod;
import com.bolat.casinomod.menu.CasinoMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CasinoNetwork {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(CasinoMod.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private static boolean registered = false;
    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.messageBuilder(SelectPrizePacket.class, 0)
            .encoder(SelectPrizePacket::encode).decoder(SelectPrizePacket::new)
            .consumerMainThread(SelectPrizePacket::handle).add();
    }
    public static void selectPrize(String itemId) {
        CHANNEL.sendToServer(new SelectPrizePacket(itemId));
    }
    public static class SelectPrizePacket {
        private final String itemId;
        public SelectPrizePacket(String itemId) { this.itemId = itemId; }
        public SelectPrizePacket(net.minecraft.network.FriendlyByteBuf buf) { this.itemId = buf.readUtf(256); }
        public void encode(net.minecraft.network.FriendlyByteBuf buf) { buf.writeUtf(itemId, 256); }
        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context ctx = supplier.get();
            ctx.enqueueWork(() -> {
                if (ctx.getSender() == null || !(ctx.getSender().containerMenu instanceof CasinoMenu menu)) return;
                ResourceLocation id = ResourceLocation.tryParse(itemId);
                if (id == null) return;
                Item item = ForgeRegistries.ITEMS.getValue(id);
                if (item == null || item == net.minecraft.world.item.Items.AIR) return;
                menu.selectPrize(new ItemStack(item));
            });
            ctx.setPacketHandled(true);
        }
    }
}
