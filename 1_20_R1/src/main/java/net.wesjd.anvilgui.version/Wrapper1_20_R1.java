package net.wesjd.anvilgui.version;


import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.*;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class Wrapper1_20_R1 implements VersionWrapper {
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
    public int getNextContainerId(Player player, AnvilContainerWrapper container) {
        return ((AnvilContainer) container).getContainerId();
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
        toNMS(player).r(); // r -> doCloseContainer
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId, Object inventoryTitle) {
        toNMS(player).c.a(new PacketPlayOutOpenWindow(containerId, Containers.h, (IChatBaseComponent) inventoryTitle));
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).c.a(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).bR = toNMS(player).bQ;
    }

    @Override
    public void setActiveContainer(Player player, AnvilContainerWrapper container) {
        toNMS(player).bR = (Container) container;
    }

    @Override
    public void setActiveContainerId(AnvilContainerWrapper container, int containerId) {}

    @Override
    public void addActiveContainerSlotListener(AnvilContainerWrapper container, Player player) {
        toNMS(player).a((Container) container);
    }

    @Override
    public AnvilContainerWrapper newContainerAnvil(Player player, Object title) {
        return new AnvilContainer(player, getRealNextContainerId(player), (IChatBaseComponent) title);
    }

    @Override
    public Object literalChatComponent(String content) {
        return IChatBaseComponent.b(content);
    }

    @Override
    public Object jsonChatComponent(String json) {
        return IChatBaseComponent.ChatSerializer.a(json);
    }

    private static class AnvilContainer extends ContainerAnvil implements AnvilContainerWrapper {
        public AnvilContainer(Player player, int containerId, IChatBaseComponent guiTitle) {
            super(
                    containerId,
                    ((CraftPlayer) player).getHandle().fN(),
                    ContainerAccess.a(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(guiTitle);
        }

        @Override
        public void m() {
            // If the output is empty copy the left input into the output
            Slot output = this.b(2);
            if (!output.f()) {
                output.e(this.b(0).e().p());
            }

            this.w.a(0);

            // Sync to the client
            this.b();
            this.d();
        }

        @Override
        public void b(EntityHuman player) {}

        @Override
        protected void a(EntityHuman player, IInventory container) {}

        public int getContainerId() {
            return this.j;
        }

        @Override
        public String getRenameText() {
            return this.v;
        }

        @Override
        public void setRenameText(String text) {
            // If an item is present in the left input slot change its hover name to the literal text.
            Slot inputLeft = b(0);
            if (inputLeft.f()) {
                inputLeft.e().a(IChatBaseComponent.b(text));
            }
        }

        @Override
        public Inventory getBukkitInventory() {
            return getBukkitView().getTopInventory();
        }
    }
}
