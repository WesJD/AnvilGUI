package net.wesjd.anvilgui.version;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Wraps versions to be able to easily use different NMS server versions
 * @since 1.0
 */
public interface VersionWrapper {

    /**
     * Gets the next available NMS container id for the player
     * @param player The player to get the next container id of
     * @return The next available NMS container id
     *
     * @since 1.0
     */
    int getNextContainerId(Player player);

    /**
     * Closes the current inventory for the player
     * @param player The player that needs their current inventory closed
     *
     * @since 1.0
     */
    void handleInventoryCloseEvent(Player player);

    /**
     * Sends PacketPlayOutOpenWindow to the player with the container id
     * @param player The player to send the packet to
     * @param containerId The container id to open
     *
     * @since 1.0
     */
    void sendPacketOpenWindow(Player player, int containerId);

    /**
     * Sends PacketPlayOutCloseWindow to the player with the contaienr id
     * @param player The player to send the packet to
     * @param containerId The container id to close
     *
     * @since 1.0
     */
    void sendPacketCloseWindow(Player player, int containerId);

    /**
     * Sets the NMS player's active container to the default one
     * @param player The player to set the active container of
     *
     * @since 1.0
     */
    void setActiveContainerDefault(Player player);

    /**
     * Sets the NMS player's active container to the one supplied
     * @param player The player to set the active container of
     * @param container The container to set as active
     *
     * @since 1.0
     */
    void setActiveContainer(Player player, Object container);

    /**
     * Sets the supplied windowId of the supplied Container
     * @param container The container to set the windowId of
     * @param containerId The new windowId
     *
     * @since 1.0
     */
    void setActiveContainerId(Object container, int containerId);

    /**
     * Adds a slot listener to the supplied container for the player
     * @param container The container to add the slot listener to
     * @param player The player to have as a listener
     *
     * @since 1.0
     */
    void addActiveContainerSlotListener(Object container, Player player);

    /**
     * Gets the {@link Inventory} wrapper of the supplied NMS container
     * @param container The NMS container to get the {@link Inventory} of
     * @return The inventory of the NMS container
     *
     * @since 1.0
     */
    Inventory toBukkitInventory(Object container);

    /**
     * Creates a new ContainerAnvil
     * @param player The player to get the container of
     * @return The Container instance
     *
     * @since 1.0
     */
    Object newContainerAnvil(Player player);

}
