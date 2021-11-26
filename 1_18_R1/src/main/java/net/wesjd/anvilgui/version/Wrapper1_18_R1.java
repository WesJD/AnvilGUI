package net.wesjd.anvilgui.version;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Containers;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class Wrapper1_18_R1 implements VersionWrapper {
    private int getRealNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }

    /**
     * Turns a {@link Player} into an NMS one
     *
     * @param player The player to be converted
     * @return the NMS EntityPlayer
     */
    private EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    @Override
    public int getNextContainerId(Player player, Object container) {
        return ((AnvilContainer) container).getContainerId();
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId, String inventoryTitle) {
        toNMS(player).b.a(new PacketPlayOutOpenWindow(containerId, Containers.h, new ChatComponentText(inventoryTitle)));
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).b.a(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        (toNMS(player)).bW = (toNMS(player)).bV;
    }

    @Override
    public void setActiveContainer(Player player, Object container) {
        (toNMS(player)).bW = (Container) container;
    }

    @Override
    public void setActiveContainerId(Object container, int containerId) {

    }

    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        toNMS(player).a((Container) container);
    }

    @Override
    public Inventory toBukkitInventory(Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    @Override
    public Object newContainerAnvil(Player player, String title) {
        return new AnvilContainer(player, getRealNextContainerId(player), title);
    }

    private static class AnvilContainer extends ContainerAnvil {
        public AnvilContainer(Player player, int containerId, String guiTitle) {
            super(containerId, ((CraftPlayer) player).getHandle().fq(),
                    ContainerAccess.a(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(new ChatMessage(guiTitle));
        }

        @Override
        public void l() {
            super.l();
            this.w.a(0);
        }

        @Override
        public void b(EntityHuman player) {}

        @Override
        protected void a(EntityHuman player, IInventory container) {}

        public int getContainerId() {
            return this.j;
        }
    }
}
