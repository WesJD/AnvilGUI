package net.wesjd.anvilgui.version;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Wrapper1_15_R1 implements VersionWrapper {
    private int getRealNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNextContainerId(Player player, Object container) {
        return ((AnvilContainer) container).getContainerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketOpenWindow(Player player, int containerId, String guiTitle) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, new ChatMessage(guiTitle)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).activeContainer = toNMS(player).defaultContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainer(Player player, Object container) {
        toNMS(player).activeContainer = (Container) container;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerId(Object container, int containerId) {
        //noop
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        ((Container) container).addSlotListener(toNMS(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Inventory toBukkitInventory(Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object newContainerAnvil(Player player, String guiTitle) {
        return new AnvilContainer(player, guiTitle);
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

    /**
     * Modifications to ContainerAnvil that makes it so you don't have to have xp to use this anvil
     */
    private class AnvilContainer extends ContainerAnvil {

        public AnvilContainer(Player player, String guiTitle) {
            super(getRealNextContainerId(player), ((CraftPlayer) player).getHandle().inventory,
                    ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(new ChatMessage(guiTitle));
        }

        @Override
        public void e() {
            super.e();
            this.levelCost.set(0);
        }

        public int getContainerId() {
            return windowId;
        }

    }

}