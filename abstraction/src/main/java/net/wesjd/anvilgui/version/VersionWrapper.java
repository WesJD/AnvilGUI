package net.wesjd.anvilgui.version;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Wraps versions to be able to easily use different NMS server versions
 * @author Wesley Smith
 * @since 1.0
 */
public interface VersionWrapper {

    /**
     * Gets the next available NMS container id for the player
     * @param player The player to get the next container id of
     * @param container The container that a new id is being generated for
     * @return The next available NMS container id
     */
    int getNextContainerId(Player player, Object container);

    /**
     * Closes the current inventory for the player
     * @param player The player that needs their current inventory closed
     */
    void handleInventoryCloseEvent(Player player);

    /**
     * Sends PacketPlayOutOpenWindow to the player with the container id and window title
     * @param player The player to send the packet to
     * @param containerId The container id to open
     * @param inventoryTitle The title of the inventory to be opened (only works in Minecraft 1.14 and above)
     */
    void sendPacketOpenWindow(Player player, int containerId, String inventoryTitle);

    /**
     * Sends PacketPlayOutCloseWindow to the player with the contaienr id
     * @param player The player to send the packet to
     * @param containerId The container id to close
     */
    void sendPacketCloseWindow(Player player, int containerId);

    /**
     * Sets the NMS player's active container to the default one
     * @param player The player to set the active container of
     */
    void setActiveContainerDefault(Player player);

    /**
     * Sets the NMS player's active container to the one supplied
     * @param player The player to set the active container of
     * @param container The container to set as active
     */
    void setActiveContainer(Player player, Object container);

    /**
     * Sets the supplied windowId of the supplied Container
     * @param container The container to set the windowId of
     * @param containerId The new windowId
     */
    void setActiveContainerId(Object container, int containerId);

    /**
     * Adds a slot listener to the supplied container for the player
     * @param container The container to add the slot listener to
     * @param player The player to have as a listener
     */
    void addActiveContainerSlotListener(Object container, Player player);

    /**
     * Gets the {@link Inventory} wrapper of the supplied NMS container
     * @param container The NMS container to get the {@link Inventory} of
     * @return The inventory of the NMS container
     */
    Inventory toBukkitInventory(Object container);

    /**
     * Creates a new ContainerAnvil
     * @param player The player to get the container of
     * @param title The title of the anvil inventory
     * @return The Container instance
     */
    Object newContainerAnvil(Player player, String title);

}
