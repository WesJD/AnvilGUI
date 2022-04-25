package net.wesjd.anvilgui;

import net.wesjd.anvilgui.version.VersionMatcher;
import net.wesjd.anvilgui.version.VersionWrapper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * An anvil gui, used for gathering a user's input
 *
 * @author Wesley Smith
 * @since 1.0
 */
public class AnvilGUI {

    /**
     * The local {@link VersionWrapper} object for the server's version
     */
    private static final VersionWrapper WRAPPER = new VersionMatcher().match();

    /**
     * The {@link Plugin} that this anvil GUI is associated with
     */
    private static final Plugin PLUGIN = JavaPlugin.getProvidingPlugin(AnvilGUI.class);
    /**
     * The player who has the GUI open
     */
    private final Player player;
    /**
     * The title of the anvil inventory
     */
    private String inventoryTitle;
    /**
     * The ItemStack that is in the {@link Slot#INPUT_LEFT} slot.
     */
    private ItemStack inputLeft;
    /**
     * The ItemStack that is in the {@link Slot#INPUT_RIGHT} slot.
     */
    private ItemStack inputRight;
    /**
     * A state that decides where the anvil GUI is able to be closed by the user
     */
    private final boolean preventClose;
    /**
     * An {@link Consumer} that is called when the anvil GUI is closed
     */
    private final Consumer<Player> closeListener;
    /**
     * An {@link BiFunction} that is called when the {@link Slot#OUTPUT} slot has been clicked
     */
    private final BiFunction<Player, String, Response> completeFunction;

    /**
     * An {@link Consumer} that is called when the {@link Slot#INPUT_LEFT} slot has been clicked
     */
    private final Consumer<Player> inputLeftClickListener;
    /**
     * An {@link Consumer} that is called when the {@link Slot#INPUT_RIGHT} slot has been clicked
     */
    private final Consumer<Player> inputRightClickListener;

    /**
     * The container id of the inventory, used for NMS methods
     */
    private int containerId;
    /**
     * The inventory that is used on the Bukkit side of things
     */
    private Inventory inventory;
    /**
     * The listener holder class
     */
    private final ListenUp listener = new ListenUp();

    /**
     * Represents the state of the inventory being open
     */
    private boolean open;

    /**
     * Create an AnvilGUI and open it for the player.
     *
     * @param plugin     A {@link org.bukkit.plugin.java.JavaPlugin} instance
     * @param holder     The {@link Player} to open the inventory for
     * @param insert  What to have the text already set to
     * @param biFunction A {@link BiFunction} that is called when the player clicks the {@link Slot#OUTPUT} slot
     * @throws NullPointerException If the server version isn't supported
     * @deprecated As of version 1.2.3, use {@link AnvilGUI.Builder}
     */
    @Deprecated
    public AnvilGUI(Plugin plugin, Player holder, String insert, BiFunction<Player, String, String> biFunction) {
        this(plugin, holder, "Repair & Name", insert, null, null, false, null, null, null, (player, text) -> {
            String response = biFunction.apply(player, text);
            if (response != null) {
                return Response.text(response);
            } else {
                return Response.close();
            }
        });
    }

    /**
     * Create an AnvilGUI and open it for the player.
     *
     * @param plugin           A {@link org.bukkit.plugin.java.JavaPlugin} instance
     * @param player           The {@link Player} to open the inventory for
     * @param inventoryTitle   What to have the text already set to
     * @param itemText         The name of the item in the first slot of the anvilGui
     * @param inputLeft        The material of the item in the first slot of the anvilGUI
     * @param preventClose     Whether to prevent the inventory from closing
     * @param closeListener    A {@link Consumer} when the inventory closes
     * @param completeFunction A {@link BiFunction} that is called when the player clicks the {@link Slot#OUTPUT} slot
     */
    private AnvilGUI(
            Plugin plugin,
            Player player,
            String inventoryTitle,
            String itemText,
            ItemStack inputLeft,
            ItemStack inputRight,
            boolean preventClose,
            Consumer<Player> closeListener,
            Consumer<Player> inputLeftClickListener,
            Consumer<Player> inputRightClickListener,
            BiFunction<Player, String, Response> completeFunction
    ) {
        this.player = player;
        this.inventoryTitle = inventoryTitle;
        this.inputLeft = inputLeft;
        this.inputRight = inputRight;
        this.preventClose = preventClose;
        this.closeListener = closeListener;
        this.inputLeftClickListener = inputLeftClickListener;
        this.inputRightClickListener = inputRightClickListener;
        this.completeFunction = completeFunction;

        if (itemText != null) {
            if (inputLeft == null) {
                this.inputLeft = new ItemStack(Material.PAPER);
            }

            ItemMeta paperMeta = this.inputLeft.getItemMeta();
            paperMeta.setDisplayName(itemText);
            this.inputLeft.setItemMeta(paperMeta);
        }

        openInventory();
    }

    /**
     * Opens the anvil GUI
     */
    private void openInventory() {
        WRAPPER.handleInventoryCloseEvent(player);
        WRAPPER.setActiveContainerDefault(player);

        Bukkit.getPluginManager().registerEvents(listener, PLUGIN);

        final Object container = WRAPPER.newContainerAnvil(player, inventoryTitle);

        inventory = WRAPPER.toBukkitInventory(container);
        inventory.setItem(Slot.INPUT_LEFT, this.inputLeft);
        if (this.inputRight != null) {
            inventory.setItem(Slot.INPUT_RIGHT, this.inputRight);
        }

        containerId = WRAPPER.getNextContainerId(player, container);
        WRAPPER.sendPacketOpenWindow(player, containerId, inventoryTitle);
        WRAPPER.setActiveContainer(player, container);
        WRAPPER.setActiveContainerId(container, containerId);
        WRAPPER.addActiveContainerSlotListener(container, player);
        open = true;
    }

    /**
     * Closes the inventory if it's open.
     */
    public void closeInventory() {
        closeInventory(true);
    }

    /**
     * Closes the inventory if it's open, only sending the close inventory packets if the arg is true
     * @param sendClosePacket Whether to send the close inventory event, packet, etc
     */
    private void closeInventory(boolean sendClosePacket) {
        if (!open) {
            return;
        }

        open = false;

        HandlerList.unregisterAll(listener);

        if (sendClosePacket) {
            WRAPPER.handleInventoryCloseEvent(player);
            WRAPPER.setActiveContainerDefault(player);
            WRAPPER.sendPacketCloseWindow(player, containerId);
        }

        if (closeListener != null) {
            closeListener.accept(player);
        }
    }

    /**
     * Returns the Bukkit inventory for this anvil gui
     *
     * @return the {@link Inventory} for this anvil gui
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Simply holds the listeners for the GUI
     */
    private class ListenUp implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (
                    event.getInventory().equals(inventory) &&
					(event.getRawSlot() < 3 || event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))
            ) {
                event.setCancelled(true);
                final Player clicker = (Player) event.getWhoClicked();
                if (event.getRawSlot() == Slot.OUTPUT) {
                    final ItemStack clicked = inventory.getItem(Slot.OUTPUT);
                    if (clicked == null || clicked.getType() == Material.AIR) return;

                    final Response response = completeFunction.apply(
                            clicker,
                            clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : ""
                    );
                    if (response.getText() != null) {
                        final ItemMeta meta = clicked.getItemMeta();
                        meta.setDisplayName(response.getText());
                        clicked.setItemMeta(meta);
                        inventory.setItem(Slot.INPUT_LEFT, clicked);
                    } else if (response.getInventoryToOpen() != null) {
                        clicker.openInventory(response.getInventoryToOpen());
                    } else {
                        closeInventory();
                    }
                } else if (event.getRawSlot() == Slot.INPUT_LEFT) {
                    if (inputLeftClickListener != null) {
                        inputLeftClickListener.accept(player);
                    }
                } else if (event.getRawSlot() == Slot.INPUT_RIGHT) {
                    if (inputRightClickListener != null) {
                        inputRightClickListener.accept(player);
                    }
                }
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
            if (event.getInventory().equals(inventory)) {
                for (int slot : Slot.values()) {
                    if (event.getRawSlots().contains(slot)) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if (open && event.getInventory().equals(inventory)) {
                closeInventory(false);
                if (preventClose) {
                    Bukkit.getScheduler().runTask(PLUGIN, AnvilGUI.this::openInventory);
                }
            }
        }

    }

    /**
     * A builder class for an {@link AnvilGUI} object
     */
    public static class Builder {

        /**
         * An {@link Consumer} that is called when the anvil GUI is closed
         */
        private Consumer<Player> closeListener;
        /**
         * A state that decides where the anvil GUI is able to be closed by the user
         */
        private boolean preventClose = false;
        /**
         * An {@link Consumer} that is called when the {@link Slot#INPUT_LEFT} slot has been clicked
         */
        private Consumer<Player> inputLeftClickListener;
        /**
         * An {@link Consumer} that is called when the {@link Slot#INPUT_RIGHT} slot has been clicked
         */
        private Consumer<Player> inputRightClickListener;
        /**
         * An {@link BiFunction} that is called when the anvil output slot has been clicked
         */
        private BiFunction<Player, String, Response> completeFunction;
        /**
         * The {@link Plugin} that this anvil GUI is associated with
         */
        private Plugin plugin;
        /**
         * The text that will be displayed to the user
         */
        private String title = "Repair & Name";
        /**
         * The starting text on the item
         */
        private String itemText;
        /**
         * An {@link ItemStack} to be put in the left input slot
         */
        private ItemStack itemLeft;
        /**
         * An {@link ItemStack} to be put in the right input slot
         */
        private ItemStack itemRight;

        /**
         * Prevents the closing of the anvil GUI by the user
         *
         * @return The {@link Builder} instance
         */
        public Builder preventClose() {
            preventClose = true;
            return this;
        }

        /**
         * Listens for when the inventory is closed
         *
         * @param closeListener An {@link Consumer} that is called when the anvil GUI is closed
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException when the closeListener is null
         */
        public Builder onClose(Consumer<Player> closeListener) {
            Validate.notNull(closeListener, "closeListener cannot be null");
            this.closeListener = closeListener;
            return this;
        }

        /**
         * Listens for when the first input slot is clicked
         *
         * @param inputLeftClickListener An {@link Consumer} that is called when the first input slot is clicked
         * @return The {@link Builder} instance
         */
        public Builder onLeftInputClick(Consumer<Player> inputLeftClickListener) {
            this.inputLeftClickListener = inputLeftClickListener;
            return this;
        }

        /**
         * Listens for when the second input slot is clicked
         *
         * @param inputRightClickListener An {@link Consumer} that is called when the second input slot is clicked
         * @return The {@link Builder} instance
         */
        public Builder onRightInputClick(Consumer<Player> inputRightClickListener) {
            this.inputRightClickListener = inputRightClickListener;
            return this;
        }

        /**
         * Handles the inventory output slot when it is clicked
         *
         * @param completeFunction An {@link BiFunction} that is called when the user clicks the output slot
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException when the completeFunction is null
         */
        public Builder onComplete(BiFunction<Player, String, Response> completeFunction) {
            Validate.notNull(completeFunction, "Complete function cannot be null");
            this.completeFunction = completeFunction;
            return this;
        }

        /**
         * Sets the inital item-text that is displayed to the user
         *
         * @param text The initial name of the item in the anvil
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException if the text is null
         */
        public Builder text(String text) {
            Validate.notNull(text, "Text cannot be null");
            this.itemText = text;
            return this;
        }

        /**
         * Sets the AnvilGUI title that is to be displayed to the user
         *
         * @param title The title that is to be displayed to the user
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException if the title is null
         */
        public Builder title(String title) {
            Validate.notNull(title, "title cannot be null");
            this.title = title;
            return this;
        }

        /**
         * Sets the {@link ItemStack} to be put in the first slot
         *
         * @param item The {@link ItemStack} to be put in the first slot
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException if the {@link ItemStack} is null
         * @deprecated As of version 1.4.0, use {@link AnvilGUI.Builder#itemLeft}
         */
        @Deprecated
        public Builder item(ItemStack item) {
            return itemLeft(item);
        }

        /**
         * Sets the {@link ItemStack} to be put in the first slot
         *
         * @param item The {@link ItemStack} to be put in the first slot
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException if the {@link ItemStack} is null
         */
        public Builder itemLeft(ItemStack item) {
            Validate.notNull(item, "item cannot be null");
            this.itemLeft = item;
            return this;
        }

        /**
         * Sets the {@link ItemStack} to be put in the second slot
         *
         * @param item The {@link ItemStack} to be put in the second slot
         * @return The {@link Builder} instance
         */
        public Builder itemRight(ItemStack item) {
            this.itemRight = item;
            return this;
        }

        /**
         * Creates the anvil GUI and opens it for the player
         *
         * @param player The {@link Player} the anvil GUI should open for
         * @return The {@link AnvilGUI} instance from this builder
         * @throws IllegalArgumentException when the onComplete function, plugin, or player is null
         */
        public AnvilGUI open(Player player) {
            Validate.notNull(plugin, "Plugin cannot be null");
            Validate.notNull(completeFunction, "Complete function cannot be null");
            Validate.notNull(player, "Player cannot be null");
            return new AnvilGUI(plugin, player, title, itemText, itemLeft, itemRight, preventClose, closeListener, inputLeftClickListener, inputRightClickListener, completeFunction);
        }

    }

    /**
     * Represents a response when the player clicks the output item in the anvil GUI
     */
    public static class Response {

        /**
         * The text that is to be displayed to the user
         */
        private final String text;
        private final Inventory openInventory;

        /**
         * Creates a response to the user's input
         *
         * @param text The text that is to be displayed to the user, which can be null to close the inventory
         */
        private Response(String text, Inventory openInventory) {
            this.text = text;
            this.openInventory = openInventory;
        }

        /**
         * Gets the text that is to be displayed to the user
         *
         * @return The text that is to be displayed to the user
         */
        public String getText() {
            return text;
        }

        /**
         * Gets the inventory that should be opened
         *
         * @return The inventory that should be opened
         */
        public Inventory getInventoryToOpen() {
            return openInventory;
        }

        /**
         * Returns an {@link Response} object for when the anvil GUI is to close
         *
         * @return An {@link Response} object for when the anvil GUI is to close
         */
        public static Response close() {
            return new Response(null, null);
        }

        /**
         * Returns an {@link Response} object for when the anvil GUI is to display text to the user
         *
         * @param text The text that is to be displayed to the user
         * @return An {@link Response} object for when the anvil GUI is to display text to the user
         */
        public static Response text(String text) {
            return new Response(text, null);
        }

        /**
         * Returns an {@link Response} object for when the GUI should open the provided inventory
         *
         * @param inventory The inventory to open
         * @return The {@link Response} to return
         */
        public static Response openInventory(Inventory inventory) {
            return new Response(null, inventory);
        }

    }

    /**
     * Class wrapping the magic constants of slot numbers in an anvil GUI
     */
    public static class Slot {

        private static final int[] values = new int[]{Slot.INPUT_LEFT, Slot.INPUT_RIGHT, Slot.OUTPUT};

        /**
         * The slot on the far left, where the first input is inserted. An {@link ItemStack} is always inserted
         * here to be renamed
         */
        public static final int INPUT_LEFT = 0;
        /**
         * Not used, but in a real anvil you are able to put the second item you want to combine here
         */
        public static final int INPUT_RIGHT = 1;
        /**
         * The output slot, where an item is put when two items are combined from {@link #INPUT_LEFT} and
         * {@link #INPUT_RIGHT} or {@link #INPUT_LEFT} is renamed
         */
        public static final int OUTPUT = 2;

        /**
         * Get all anvil slot values
         *
         * @return The array containing all possible anvil slots
         */
        public static int[] values() {
            return values;
        }
    }

}
