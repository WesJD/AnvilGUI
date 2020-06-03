package net.wesjd.anvilgui.version;

import net.minecraft.server.v1_7_R4.*;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * {@link VersionWrapper} implemented for NMS version 1_7_R4
 *
 * @author Hasan Demirtaş
 * @since 1.3.0
 */
public final class Wrapper1_7_R4 implements VersionWrapper {

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
        return toNMS(player).nextContainerCounter();
    }

    @Override
    public void handleInventoryCloseEvent(final Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }

    @Override
    public void sendPacketOpenWindow(final Player player, final int containerId, final String guiTitle) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, 8, "Repairing", 9, true));
    }

    @Override
    public void sendPacketCloseWindow(final Player player, final int containerId) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void setActiveContainerDefault(final Player player) {
        toNMS(player).activeContainer = toNMS(player).defaultContainer;
    }

    @Override
    public void setActiveContainer(final Player player, final Object container) {
        toNMS(player).activeContainer = (Container) container;
    }

    @Override
    public void setActiveContainerId(final Object container, final int containerId) {
        ((Container) container).windowId = containerId;
    }

    @Override
    public void addActiveContainerSlotListener(final Object container, final Player player) {
        ((Container) container).addSlotListener(toNMS(player));
    }

    @Override
    public Inventory toBukkitInventory(final Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    @Override
    public Object newContainerAnvil(final Player player, final String guiTitle) {
        return new Wrapper1_7_R4.AnvilContainer(toNMS(player));
    }

    /**
     * Modifications to ContainerAnvil that makes it so you don't have to have xp to use this anvil
     */
    private static final class AnvilContainer extends ContainerAnvil {

        private AnvilContainer(final EntityHuman entityhuman) {
            super(entityhuman.inventory, entityhuman.world, 0, 0, 0, entityhuman);
        }

        @Override
        public boolean a(final EntityHuman human) {
            return true;
        }

    }

}
