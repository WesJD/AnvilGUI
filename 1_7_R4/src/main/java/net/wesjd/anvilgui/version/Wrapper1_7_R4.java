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
public class Wrapper1_7_R4 implements VersionWrapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNextContainerId(final Player player, final Object container) {
        return this.toNMS(player).nextContainerCounter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleInventoryCloseEvent(final Player player) {
        CraftEventFactory.handleInventoryCloseEvent(this.toNMS(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketOpenWindow(final Player player, final int containerId, final Object guiTitle) {
        this.toNMS(player)
                .playerConnection
                .sendPacket(new PacketPlayOutOpenWindow(containerId, 8, (String) guiTitle, 9, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketCloseWindow(final Player player, final int containerId) {
        this.toNMS(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerDefault(final Player player) {
        this.toNMS(player).activeContainer = this.toNMS(player).defaultContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainer(final Player player, final Object container) {
        this.toNMS(player).activeContainer = (Container) container;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerId(final Object container, final int containerId) {
        ((Container) container).windowId = containerId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addActiveContainerSlotListener(final Object container, final Player player) {
        ((Container) container).addSlotListener(this.toNMS(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Inventory toBukkitInventory(final Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object newContainerAnvil(final Player player, final Object guiTitle) {
        return new AnvilContainer(this.toNMS(player));
    }

    @Override
    public Object literalChatComponent(String content) {
        return content;
    }

    @Override
    public Object jsonChatComponent(String json) {
        throw new UnsupportedOperationException("Rich text components are only available from Minecraft 1.8 and up");
    }

    /**
     * Turns a {@link Player} into an NMS one
     *
     * @param player The player to be converted
     * @return the NMS EntityPlayer
     */
    private EntityPlayer toNMS(final Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * Modifications to ContainerAnvil that makes it so you don't have to have xp to use this anvil
     */
    private class AnvilContainer extends ContainerAnvil {

        public AnvilContainer(final EntityHuman entityhuman) {
            super(entityhuman.inventory, entityhuman.world, 0, 0, 0, entityhuman);
        }

        @Override
        public boolean a(final EntityHuman human) {
            return true;
        }

        @Override
        public void b(EntityHuman entityhuman) {}
    }
}
