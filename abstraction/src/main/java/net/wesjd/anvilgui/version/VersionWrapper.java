package net.wesjd.anvilgui.version;


import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Wraps versions to be able to easily use different NMS server versions
 *
 * @author Wesley Smith
 * @since 1.0
 */
public interface VersionWrapper {

    /**
     * Gets the next available NMS container id for the player
     *
     * @param player    The player to get the next container id of
     * @param container The container that a new id is being generated for
     * @return The next available NMS container id
     */
    int getNextContainerId(Player player, AnvilContainerWrapper container);

    /**
     * Closes the current inventory for the player
     *
     * @param player The player that needs their current inventory closed
     */
    void handleInventoryCloseEvent(Player player);

    /**
     * Sends PacketPlayOutOpenWindow to the player with the container id and window title
     *
     * @param player         The player to send the packet to
     * @param containerId    The container id to open
     * @param inventoryTitle The title of the inventory to be opened (only works in Minecraft 1.14 and above)
     */
    void sendPacketOpenWindow(Player player, int containerId, Object inventoryTitle);

    /**
     * Sends PacketPlayOutCloseWindow to the player with the contaienr id
     *
     * @param player      The player to send the packet to
     * @param containerId The container id to close
     */
    void sendPacketCloseWindow(Player player, int containerId);

    /**
     * Sets the NMS player's active container to the default one
     *
     * @param player The player to set the active container of
     */
    void setActiveContainerDefault(Player player);

    /**
     * Sets the NMS player's active container to the one supplied
     *
     * @param player    The player to set the active container of
     * @param container The container to set as active
     */
    void setActiveContainer(Player player, AnvilContainerWrapper container);

    /**
     * Sets the supplied windowId of the supplied Container
     *
     * @param container   The container to set the windowId of
     * @param containerId The new windowId
     */
    void setActiveContainerId(AnvilContainerWrapper container, int containerId);

    /**
     * Adds a slot listener to the supplied container for the player
     *
     * @param container The container to add the slot listener to
     * @param player    The player to have as a listener
     */
    void addActiveContainerSlotListener(AnvilContainerWrapper container, Player player);

    /**
     * Creates a new ContainerAnvil
     *
     * @param player The player to get the container of
     * @param title  The title of the anvil inventory
     * @return The Container instance
     */
    AnvilContainerWrapper newContainerAnvil(Player player, Object title);

    /**
     * Checks if the current Minecraft version actually supports custom titles
     *
     * @return The current supported state
     */
    default boolean isCustomTitleSupported() {
        return true;
    }

    /**
     * Creates a new chat component that does not handle the content in any special way
     *
     * @param content The content to display
     * @return Version-specific ChatComponent instance
     */
    Object literalChatComponent(String content);

    /**
     * Creates a new rich chat component from the provided json
     *
     * @param json The component to parse
     * @return Version-specific ChatComponent instance
     */
    Object jsonChatComponent(String json);

    /**
     * Interface implemented by the custom NMS AnvilContainer used to interact with it directly
     */
    interface AnvilContainerWrapper {

        /**
         * Retrieves the raw text that has been entered into the Anvil at the moment
         * <br><br>
         * This field is marked as public in the Minecraft AnvilContainer only from Minecraft 1.11 and upwards
         *
         * @return The raw text in the rename field
         */
        default String getRenameText() {
            return null;
        }

        /**
         * Sets the provided text as the literal hovername of the item in the left input slot
         *
         * @param text The text to set
         */
        default void setRenameText(String text) {}

        /**
         * Gets the {@link Inventory} wrapper of the NMS container
         *
         * @return The inventory of the NMS container
         */
        Inventory getBukkitInventory();
    }
}
