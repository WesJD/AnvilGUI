package net.wesjd.anvilgui.version;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class Wrapper1_15_R1 implements VersionWrapper {

    /**
     * Turns a {@link Player} into an NMS one
     *
     * @param player The player to be converted
     * @return the NMS EntityPlayer
     */
    private static EntityPlayer toNMS(final Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    @Override
    public int getNextContainerId(final Player player, final Object container) {
        return ((Wrapper1_15_R1.AnvilContainer) container).getContainerId();
    }

    @Override
    public void handleInventoryCloseEvent(final Player player) {
        CraftEventFactory.handleInventoryCloseEvent(Wrapper1_15_R1.toNMS(player));
    }

    @Override
    public void sendPacketOpenWindow(final Player player, final int containerId, final String guiTitle) {
        Wrapper1_15_R1.toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, new ChatMessage(guiTitle)));
    }

    @Override
    public void sendPacketCloseWindow(final Player player, final int containerId) {
        Wrapper1_15_R1.toNMS(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void setActiveContainerDefault(final Player player) {
        Wrapper1_15_R1.toNMS(player).activeContainer = Wrapper1_15_R1.toNMS(player).defaultContainer;
    }

    @Override
    public void setActiveContainer(final Player player, final Object container) {
        Wrapper1_15_R1.toNMS(player).activeContainer = (Container) container;
    }

    @Override
    public void setActiveContainerId(final Object container, final int containerId) {
        //noop
    }

    @Override
    public void addActiveContainerSlotListener(final Object container, final Player player) {
        ((Container) container).addSlotListener(Wrapper1_15_R1.toNMS(player));
    }

    @Override
    public Inventory toBukkitInventory(final Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    @Override
    public Object newContainerAnvil(final Player player, final String guiTitle) {
        return new Wrapper1_15_R1.AnvilContainer(this.getRealNextContainerId(player), player, guiTitle);
    }

    private int getRealNextContainerId(final Player player) {
        return Wrapper1_15_R1.toNMS(player).nextContainerCounter();
    }

    /**
     * Modifications to ContainerAnvil that makes it so you don't have to have xp to use this anvil
     */
    private static final class AnvilContainer extends ContainerAnvil {

        private AnvilContainer(final int containerId, final Player player, final String guiTitle) {
            super(containerId, ((CraftHumanEntity) player).getHandle().inventory,
                ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            this.setTitle(new ChatMessage(guiTitle));
        }

        @Override
        public void e() {
            super.e();
            this.levelCost.set(0);
        }

        public int getContainerId() {
            return this.windowId;
        }

    }

}
