package net.wesjd.anvilgui;

import java.util.function.BiFunction;
import java.util.function.Consumer;
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

/**
 * An anvil gui, used for gathering a user's input
 *
 * @author Wesley Smith
 * @since 1.0
 */
public final class AnvilGUI {

    /**
     * The local {@link VersionWrapper} object for the server's version
     */
    private static final VersionWrapper WRAPPER = new VersionMatcher().match();

    /**
     * The {@link Plugin} that this anvil GUI is associated with
     */
    private final Plugin plugin;

    /**
     * The player who has the GUI open
     */
    private final Player player;

    /**
     * A state that decides where the anvil GUI is able to be closed by the user
     */
    private final boolean preventClose;

    /**
     * An {@link Consumer} that is called when the anvil GUI is closed
     */
    private final Consumer<Player> closeListener;

    /**
     * An {@link BiFunction} that is called when the {@link AnvilGUI.Slot#OUTPUT} slot has been clicked
     */
    private final BiFunction<Player, String, AnvilGUI.Response> completeFunction;

    /**
     * The listener holder class
     */
    private final AnvilGUI.ListenUp listener = new AnvilGUI.ListenUp();

    /**
     * The title of the anvil inventory
     */
    private final String inventoryTitle;

    /**
     * The ItemStack that is in the {@link AnvilGUI.Slot#INPUT_LEFT} slot.
     */
    private ItemStack insert;

    /**
     * The container id of the inventory, used for NMS methods
     */
    private int containerId;

    /**
     * The inventory that is used on the Bukkit side of things
     */
    private Inventory inventory;

    /**
     * Represents the state of the inventory being open
     */
    private boolean open;

    /**
     * Create an AnvilGUI and open it for the player.
     *
     * @param plugin A {@link org.bukkit.plugin.java.JavaPlugin} instance
     * @param holder The {@link Player} to open the inventory for
     * @param insert What to have the text already set to
     * @param biFunction A {@link BiFunction} that is called when the player clicks the {@link AnvilGUI.Slot#OUTPUT} slot
     * @throws NullPointerException If the server version isn't supported
     * @deprecated As of version 1.2.3, use {@link AnvilGUI.Builder}
     */
    @Deprecated
    public AnvilGUI(final Plugin plugin, final Player holder, final String insert, final BiFunction<Player, String, String> biFunction) {
        this(plugin, holder, "Repair & Name", insert, null, false, null, (player, text) -> {
            final String response = biFunction.apply(player, text);
            if (response != null) {
                return AnvilGUI.Response.text(response);
            } else {
                return AnvilGUI.Response.close();
            }
        });
    }

    /**
     * Create an AnvilGUI and open it for the player.
     *
     * @param plugin A {@link org.bukkit.plugin.java.JavaPlugin} instance
     * @param player The {@link Player} to open the inventory for
     * @param inventoryTitle What to have the text already set to
     * @param itemText The name of the item in the first slot of the anvilGui
     * @param insert The material of the item in the first slot of the anvilGUI
     * @param preventClose Whether to prevent the inventory from closing
     * @param closeListener A {@link Consumer} when the inventory closes
     * @param completeFunction A {@link BiFunction} that is called when the player clicks the {@link AnvilGUI.Slot#OUTPUT} slot
     */
    private AnvilGUI(
        final Plugin plugin,
        final Player player,
        final String inventoryTitle,
        final String itemText,
        final ItemStack insert,
        final boolean preventClose,
        final Consumer<Player> closeListener,
        final BiFunction<Player, String, AnvilGUI.Response> completeFunction
    ) {
        this.plugin = plugin;
        this.player = player;
        this.inventoryTitle = inventoryTitle;
        this.insert = insert;
        this.preventClose = preventClose;
        this.closeListener = closeListener;
        this.completeFunction = completeFunction;

        if (itemText != null) {
            if (insert == null) {
                this.insert = new ItemStack(Material.PAPER);
            }

            final ItemMeta paperMeta = this.insert.getItemMeta();
            paperMeta.setDisplayName(itemText);
            this.insert.setItemMeta(paperMeta);
        }

        this.openInventory();
    }

    /**
     * Closes the inventory if it's open.
     */
    public void closeInventory() {
        if (!this.open) {
            return;
        }

        this.open = false;

        AnvilGUI.WRAPPER.handleInventoryCloseEvent(this.player);
        AnvilGUI.WRAPPER.setActiveContainerDefault(this.player);
        AnvilGUI.WRAPPER.sendPacketCloseWindow(this.player, this.containerId);

        HandlerList.unregisterAll(this.listener);

        if (this.closeListener != null) {
            this.closeListener.accept(this.player);
        }
    }

    /**
     * Returns the Bukkit inventory for this anvil gui
     *
     * @return the {@link Inventory} for this anvil gui
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Opens the anvil GUI
     */
    private void openInventory() {
        AnvilGUI.WRAPPER.handleInventoryCloseEvent(this.player);
        AnvilGUI.WRAPPER.setActiveContainerDefault(this.player);

        Bukkit.getPluginManager().registerEvents(this.listener, this.plugin);

        final Object container = AnvilGUI.WRAPPER.newContainerAnvil(this.player, this.inventoryTitle);

        this.inventory = AnvilGUI.WRAPPER.toBukkitInventory(container);
        this.inventory.setItem(AnvilGUI.Slot.INPUT_LEFT.getSlot(), this.insert);

        this.containerId = AnvilGUI.WRAPPER.getNextContainerId(this.player, container);
        AnvilGUI.WRAPPER.sendPacketOpenWindow(this.player, this.containerId, this.inventoryTitle);
        AnvilGUI.WRAPPER.setActiveContainer(this.player, container);
        AnvilGUI.WRAPPER.setActiveContainerId(container, this.containerId);
        AnvilGUI.WRAPPER.addActiveContainerSlotListener(container, this.player);
        this.open = true;
    }

    /**
     * Class wrapping the magic constants of slot numbers in an anvil GUI
     */
    public enum Slot {

        /**
         * The slot on the far left, where the first input is inserted. An {@link ItemStack} is always inserted
         * here to be renamed
         */
        INPUT_LEFT(0),
        /**
         * Not used, but in a real anvil you are able to put the second item you want to combine here
         */
        INPUT_RIGHT(1),
        /**
         * The output slot, where an item is put when two items are combined from {@link #INPUT_LEFT} and
         * {@link #INPUT_RIGHT} or {@link #INPUT_LEFT} is renamed
         */
        OUTPUT(2);

        private final int slot;

        Slot(final int slot) {
            this.slot = slot;
        }

        public int getSlot() {
            return this.slot;
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
         * An {@link BiFunction} that is called when the anvil output slot has been clicked
         */
        private BiFunction<Player, String, AnvilGUI.Response> completeFunction;

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
        private String itemText = "";

        /**
         * An {@link ItemStack} to be put in the input slot
         */
        private ItemStack item;

        /**
         * Prevents the closing of the anvil GUI by the user
         *
         * @return The {@link AnvilGUI.Builder} instance
         */
        public AnvilGUI.Builder preventClose() {
            this.preventClose = true;
            return this;
        }

        /**
         * Listens for when the inventory is closed
         *
         * @param closeListener An {@link Consumer} that is called when the anvil GUI is closed
         * @return The {@link AnvilGUI.Builder} instance
         * @throws IllegalArgumentException when the closeListener is null
         */
        public AnvilGUI.Builder onClose(final Consumer<Player> closeListener) {
            Validate.notNull(closeListener, "closeListener cannot be null");
            this.closeListener = closeListener;
            return this;
        }

        /**
         * Handles the inventory output slot when it is clicked
         *
         * @param completeFunction An {@link BiFunction} that is called when the user clicks the output slot
         * @return The {@link AnvilGUI.Builder} instance
         * @throws IllegalArgumentException when the completeFunction is null
         */
        public AnvilGUI.Builder onComplete(final BiFunction<Player, String, AnvilGUI.Response> completeFunction) {
            Validate.notNull(completeFunction, "Complete function cannot be null");
            this.completeFunction = completeFunction;
            return this;
        }

        /**
         * Sets the plugin for the {@link AnvilGUI}
         *
         * @param plugin The {@link Plugin} this anvil GUI is associated with
         * @return The {@link AnvilGUI.Builder} instance
         * @throws IllegalArgumentException if the plugin is null
         */
        public AnvilGUI.Builder plugin(final Plugin plugin) {
            Validate.notNull(plugin, "Plugin cannot be null");
            this.plugin = plugin;
            return this;
        }

        /**
         * Sets the inital item-text that is displayed to the user
         *
         * @param text The initial name of the item in the anvil
         * @return The {@link AnvilGUI.Builder} instance
         * @throws IllegalArgumentException if the text is null
         */
        public AnvilGUI.Builder text(final String text) {
            Validate.notNull(text, "Text cannot be null");
            this.itemText = text;
            return this;
        }

        /**
         * Sets the AnvilGUI title that is to be displayed to the user
         *
         * @param title The title that is to be displayed to the user
         * @return The {@link AnvilGUI.Builder} instance
         * @throws IllegalArgumentException if the title is null
         */
        public AnvilGUI.Builder title(final String title) {
            Validate.notNull(title, "title cannot be null");
            this.title = title;
            return this;
        }

        /**
         * Sets the {@link ItemStack} to be put in the first slot
         *
         * @param item The {@link ItemStack} to be put in the first slot
         * @return The {@link AnvilGUI.Builder} instance
         * @throws IllegalArgumentException if the {@link ItemStack} is null
         */
        public AnvilGUI.Builder item(final ItemStack item) {
            Validate.notNull(item, "item cannot be null");
            this.item = item;
            return this;
        }

        /**
         * Creates the anvil GUI and opens it for the player
         *
         * @param player The {@link Player} the anvil GUI should open for
         * @return The {@link AnvilGUI} instance from this builder
         * @throws IllegalArgumentException when the onComplete function, plugin, or player is null
         */
        public AnvilGUI open(final Player player) {
            Validate.notNull(this.plugin, "Plugin cannot be null");
            Validate.notNull(this.completeFunction, "Complete function cannot be null");
            Validate.notNull(player, "Player cannot be null");
            return new AnvilGUI(this.plugin, player, this.title, this.itemText, this.item, this.preventClose, this.closeListener, this.completeFunction);
        }

    }

    /**
     * Represents a response when the player clicks the output item in the anvil GUI
     */
    public static final class Response {

        /**
         * The text that is to be displayed to the user
         */
        private final String text;

        /**
         * Creates a response to the user's input
         *
         * @param text The text that is to be displayed to the user, which can be null to close the inventory
         */
        private Response(final String text) {
            this.text = text;
        }

        /**
         * Returns an {@link AnvilGUI.Response} object for when the anvil GUI is to close
         *
         * @return An {@link AnvilGUI.Response} object for when the anvil GUI is to close
         */
        public static AnvilGUI.Response close() {
            return new AnvilGUI.Response(null);
        }

        /**
         * Returns an {@link AnvilGUI.Response} object for when the anvil GUI is to display text to the user
         *
         * @param text The text that is to be displayed to the user
         * @return An {@link AnvilGUI.Response} object for when the anvil GUI is to display text to the user
         */
        public static AnvilGUI.Response text(final String text) {
            return new AnvilGUI.Response(text);
        }

        /**
         * Gets the text that is to be displayed to the user
         *
         * @return The text that is to be displayed to the user
         */
        public String getText() {
            return this.text;
        }

    }

    /**
     * Simply holds the listeners for the GUI
     */
    private final class ListenUp implements Listener {

        @EventHandler
        public void onInventoryClick(final InventoryClickEvent event) {
            if ((!event.getInventory().equals(AnvilGUI.this.inventory) || event.getRawSlot() >= 3) &&
                !event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) &&
                (event.getRawSlot() >= 3 || !event.getAction().equals(InventoryAction.PLACE_ALL) &&
                    !event.getAction().equals(InventoryAction.PLACE_ONE) &&
                    !event.getAction().equals(InventoryAction.PLACE_SOME) && event.getCursor() == null)) {
                return;
            }
            event.setCancelled(true);
            final Player clicker = (Player) event.getWhoClicked();
            if (event.getRawSlot() != AnvilGUI.Slot.OUTPUT.getSlot()) {
                return;
            }
            final ItemStack clicked = AnvilGUI.this.inventory.getItem(AnvilGUI.Slot.OUTPUT.getSlot());
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }
            final AnvilGUI.Response response = AnvilGUI.this.completeFunction.apply(clicker,
                clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : "");
            if (response.getText() == null) {
                AnvilGUI.this.closeInventory();
                return;
            }
            final ItemMeta meta = clicked.getItemMeta();
            meta.setDisplayName(response.getText());
            clicked.setItemMeta(meta);
            AnvilGUI.this.inventory.setItem(AnvilGUI.Slot.INPUT_LEFT.getSlot(), clicked);
        }

        @EventHandler
        public void onInventoryDrag(final InventoryDragEvent event) {
            if (!event.getInventory().equals(AnvilGUI.this.inventory)) {
                return;
            }
            for (final AnvilGUI.Slot slot : AnvilGUI.Slot.values()) {
                if (event.getRawSlots().contains(slot.getSlot())) {
                    event.setCancelled(true);
                    break;
                }
            }
        }

        @EventHandler
        public void onInventoryClose(final InventoryCloseEvent event) {
            if (!AnvilGUI.this.open || !event.getInventory().equals(AnvilGUI.this.inventory)) {
                return;
            }
            AnvilGUI.this.closeInventory();
            if (AnvilGUI.this.preventClose) {
                Bukkit.getScheduler().runTask(AnvilGUI.this.plugin, AnvilGUI.this::openInventory);
            }
        }

    }

}
