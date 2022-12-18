package net.wesjd.anvilgui;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.wesjd.anvilgui.version.VersionMatcher;
import net.wesjd.anvilgui.version.VersionWrapper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
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
public class AnvilGUI {

    /**
     * The local {@link VersionWrapper} object for the server's version
     */
    private static final VersionWrapper WRAPPER = new VersionMatcher().match();

    /**
     * The variable containing an item with air. Used when the item would be null.
     * To keep the heap clean, this object only gets iniziaised once
     */
    private static final ItemStack AIR = new ItemStack(Material.AIR);

    /**
     * The {@link Plugin} that this anvil GUI is associated with
     */
    private final Plugin plugin;
    /**
     * The player who has the GUI open
     */
    private final Player player;
    /**
     * The title of the anvil inventory
     */
    private final String inventoryTitle;
    /**
     * The initial contents of the inventory
     */
    private final ItemStack[] initialContents;
    /**
     * A state that decides where the anvil GUI is able to get closed by the user
     */
    private final boolean preventClose;

    /**
     * A set of slot numbers that are permitted to be interacted with by the user. An interactable
     * slot is one that is able to be minipulated by the player, i.e. clicking and picking up an item,
     * placing in a new one, etc.
     */
    private final Set<Integer> interactableSlots;

    /**
     * An {@link Consumer} that is called when the anvil GUI is closed
     */
    private final Consumer<Player> closeListener;
    /**
     * An {@link Function} that is called when the {@link Slot#OUTPUT} slot has been clicked
     */
    private final Function<Completion, List<ResponseAction>> completeFunction;

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
     * @param plugin           A {@link org.bukkit.plugin.java.JavaPlugin} instance
     * @param player           The {@link Player} to open the inventory for
     * @param inventoryTitle   What to have the text already set to
     * @param initialContents  The initial contents of the inventory
     * @param preventClose     Whether to prevent the inventory from closing
     * @param closeListener    A {@link Consumer} when the inventory closes
     * @param completeFunction A {@link BiFunction} that is called when the player clicks the {@link Slot#OUTPUT} slot
     */
    private AnvilGUI(
            Plugin plugin,
            Player player,
            String inventoryTitle,
            ItemStack[] initialContents,
            boolean preventClose,
            Set<Integer> interactableSlots,
            Consumer<Player> closeListener,
            Consumer<Player> inputLeftClickListener,
            Consumer<Player> inputRightClickListener,
            Function<Completion, List<ResponseAction>> completeFunction) {
        this.plugin = plugin;
        this.player = player;
        this.inventoryTitle = inventoryTitle;
        this.initialContents = initialContents;
        this.preventClose = preventClose;
        this.interactableSlots = Collections.unmodifiableSet(interactableSlots);
        this.closeListener = closeListener;
        this.inputLeftClickListener = inputLeftClickListener;
        this.inputRightClickListener = inputRightClickListener;
        this.completeFunction = completeFunction;

        openInventory();
    }

    /**
     * Opens the anvil GUI
     */
    private void openInventory() {
        WRAPPER.handleInventoryCloseEvent(player);
        WRAPPER.setActiveContainerDefault(player);

        Bukkit.getPluginManager().registerEvents(listener, plugin);

        final Object container = WRAPPER.newContainerAnvil(player, inventoryTitle);

        inventory = WRAPPER.toBukkitInventory(container);
        // We need to use setItem instead of setContents because a Minecraft ContainerAnvil
        // contains two separate inventories: the result inventory and the ingredients inventory.
        // The setContents method only updates the ingredients inventory unfortunately,
        // but setItem handles the index going into the result inventory.
        for (int i = 0; i < initialContents.length; i++) {
            inventory.setItem(i, initialContents[i]);
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
     *
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
            if (!event.getInventory().equals(inventory)) {
                return;
            }

            final Player clicker = (Player) event.getWhoClicked();
            // prevent players from merging items from the anvil inventory
            if (event.getClickedInventory().equals(clicker) && event.getClick().equals(ClickType.DOUBLE_CLICK)) {
                event.setCancelled(true);
                return;
            }

            if (event.getRawSlot() < 3 || event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                final int slot = event.getRawSlot();
                event.setCancelled(!interactableSlots.contains(slot));

                if (event.getRawSlot() == Slot.OUTPUT) {
                    final ItemStack clicked = inventory.getItem(Slot.OUTPUT);
                    if (clicked == null || clicked.getType() == Material.AIR) return;

                    final List<ResponseAction> actions = completeFunction.apply(new Completion(
                            notNull(inventory.getItem(Slot.INPUT_LEFT)),
                            notNull(inventory.getItem(Slot.INPUT_RIGHT)),
                            notNull(inventory.getItem(Slot.OUTPUT)),
                            player,
                            clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : ""));
                    for (final ResponseAction action : actions) {
                        action.accept(AnvilGUI.this, clicker);
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
                        event.setCancelled(!interactableSlots.contains(slot));
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
                    Bukkit.getScheduler().runTask(plugin, AnvilGUI.this::openInventory);
                }
            }
        }
    }

    /**
     * If the given ItemStack is null, return an air ItemStack, otherwise return the given ItemStack
     *
     * @param stack The ItemStack to check
     * @return air or the given ItemStack
     */
    private ItemStack notNull(ItemStack stack) {
        return stack == null ? AIR : stack;
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
         * A set of integers containing the slot numbers that should be modifiable by the user.
         */
        private Set<Integer> interactableSlots = Collections.emptySet();

        /**
         * An {@link Consumer} that is called when the {@link Slot#INPUT_LEFT} slot has been clicked
         */
        private Consumer<Player> inputLeftClickListener;
        /**
         * An {@link Consumer} that is called when the {@link Slot#INPUT_RIGHT} slot has been clicked
         */
        private Consumer<Player> inputRightClickListener;
        /**
         * An {@link Function} that is called when the anvil output slot has been clicked
         */
        private Function<Completion, List<ResponseAction>> completeFunction;
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
         * An {@link ItemStack} to be placed in the output slot
         */
        private ItemStack itemOutput;

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
         * Permit the user to modify (take items in and out) the slot numbers provided.
         *
         * @param slots A varags param for the slot numbers. You can avoid relying on magic constants by using
         *              the {@link AnvilGUI.Slot} class.
         * @return The {@link Builder} instance
         */
        public Builder interactableSlots(int... slots) {
            final Set<Integer> newValue = new HashSet<>();
            for (int slot : slots) {
                newValue.add(slot);
            }
            interactableSlots = newValue;
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
         * @deprecated Since 1.6.2, use {@link #onComplete(Function)}
         */
        @Deprecated
        public Builder onComplete(BiFunction<Player, String, List<ResponseAction>> completeFunction) {
            Validate.notNull(completeFunction, "Complete function cannot be null");
            this.completeFunction = completion -> completeFunction.apply(completion.player, completion.text);
            return this;
        }

        /**
         * Handles the inventory output slot when it is clicked
         *
         * @param completeFunction An {@link Function} that is called when the user clicks the output slot
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException when the completeFunction is null
         */
        public Builder onComplete(Function<Completion, List<ResponseAction>> completeFunction) {
            Validate.notNull(completeFunction, "Complete function cannot be null");
            this.completeFunction = completeFunction;
            return this;
        }

        /**
         * Sets the plugin for the {@link AnvilGUI}
         *
         * @param plugin The {@link Plugin} this anvil GUI is associated with
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException if the plugin is null
         */
        public Builder plugin(Plugin plugin) {
            Validate.notNull(plugin, "Plugin cannot be null");
            this.plugin = plugin;
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
         * Sets the {@link ItemStack} to be put in the output slot
         *
         * @param item The {@link ItemStack} to be put in the output slot
         * @return The {@link Builder} instance
         */
        public Builder itemOutput(ItemStack item) {
            this.itemOutput = item;
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

            if (itemText != null) {
                if (itemLeft == null) {
                    itemLeft = new ItemStack(Material.PAPER);
                }

                ItemMeta paperMeta = itemLeft.getItemMeta();
                paperMeta.setDisplayName(itemText);
                itemLeft.setItemMeta(paperMeta);
            }

            return new AnvilGUI(
                    plugin,
                    player,
                    title,
                    new ItemStack[] {itemLeft, itemRight, itemOutput},
                    preventClose,
                    interactableSlots,
                    closeListener,
                    inputLeftClickListener,
                    inputRightClickListener,
                    completeFunction);
        }
    }

    /** An action to run in response to a player clicking the output slot in the GUI. This interface is public
     * and permits you, the developer, to add additional response features easily to your custom AnvilGUIs. */
    public interface ResponseAction extends BiConsumer<AnvilGUI, Player> {

        /**
         * Replace the input text box value with the provided text value
         * @param text The text to write in the input box
         * @return The {@link ResponseAction} to achieve the text replacement
         */
        static ResponseAction replaceInputText(String text) {
            return (anvilgui, player) -> {
                final ItemStack outputSlotItem =
                        anvilgui.getInventory().getItem(Slot.OUTPUT).clone();
                final ItemMeta meta = outputSlotItem.getItemMeta();
                meta.setDisplayName(text);
                outputSlotItem.setItemMeta(meta);
                anvilgui.getInventory().setItem(Slot.INPUT_LEFT, outputSlotItem);
            };
        }

        /**
         * Open another inventory
         * @param otherInventory The inventory to open
         * @return The {@link ResponseAction} to achieve the inventory open
         */
        static ResponseAction openInventory(Inventory otherInventory) {
            return (anvigui, player) -> player.openInventory(otherInventory);
        }

        /**
         * Close the AnvilGUI
         * @return The {@link ResponseAction} to achieve closing the AnvilGUI
         */
        static ResponseAction close() {
            return (anvilgui, player) -> anvilgui.closeInventory();
        }

        /**
         * Close the AnvilGUI, then run the provided runnable.
         * @param runnable The runnable to run after the inventory closes
         * @return The {@link ResponseAction} to achieve closing and then running the runnable
         */
        static ResponseAction closeThenRun(Runnable runnable) {
            return (anvilgui, player) -> {
                anvilgui.closeInventory();
                runnable.run();
            };
        }
    }

    /**
     * Represents a response when the player clicks the output item in the anvil GUI
     * @deprecated Since 1.6.2, use {@link ResponseAction}
     */
    @Deprecated
    public static class Response {
        /**
         * Returns an {@link Response} object for when the anvil GUI is to close
         * @return An {@link Response} object for when the anvil GUI is to display text to the user
         * @deprecated Since 1.6.2, use {@link ResponseAction#close()}
         */
        public static List<ResponseAction> close() {
            return Arrays.asList(ResponseAction.close());
        }

        /**
         * Returns an {@link Response} object for when the anvil GUI is to display text to the user
         *
         * @param text The text that is to be displayed to the user
         * @return A list containing the {@link ResponseAction} for legacy compat
         * @deprecated Since 1.6.2, use {@link ResponseAction#replaceInputText(String)}
         */
        public static List<ResponseAction> text(String text) {
            return Arrays.asList(ResponseAction.replaceInputText(text));
        }

        /**
         * Returns an {@link Response} object for when the GUI should open the provided inventory
         *
         * @param inventory The inventory to open
         * @return A list containing the {@link ResponseAction} for legacy compat
         * @deprecated Since 1.6.2, use {@link ResponseAction#openInventory(Inventory)}
         */
        public static List<ResponseAction> openInventory(Inventory inventory) {
            return Arrays.asList(ResponseAction.openInventory(inventory));
        }
    }

    /**
     * Class wrapping the magic constants of slot numbers in an anvil GUI
     */
    public static class Slot {

        private static final int[] values = new int[] {Slot.INPUT_LEFT, Slot.INPUT_RIGHT, Slot.OUTPUT};

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

    /**
     * Class wrapping the values you receive from the onComplete event
     */
    public static final class Completion {

        /**
         * The {@link ItemStack} in the anvilGui slots
         */
        private final ItemStack leftItem, rightItem, outputItem;

        /**
         * The {@link Player} that clicked the output slot
         */
        private final Player player;

        /**
         * The text the player typed into the field
         */
        private final String text;

        /**
         * The event parameter constructor
         * @param leftItem The left item in the combine slot of the anvilGUI
         * @param rightItem The right item in the combine slot of the anvilGUI
         * @param outputItem The item that would have been outputted, when the items would have been combined
         * @param player The player that clicked the output slot
         * @param text The text the player typed into the rename text field
         */
        public Completion(ItemStack leftItem, ItemStack rightItem, ItemStack outputItem, Player player, String text) {
            this.leftItem = leftItem;
            this.rightItem = rightItem;
            this.outputItem = outputItem;
            this.player = player;
            this.text = text;
        }

        /**
         * It returns the item in the left combine slot of the gui
         *
         * @return The leftItem
         */
        public ItemStack getLeftItem() {
            return leftItem;
        }

        /**
         * It returns the item in the right combine slot of the gui
         *
         * @return The rightItem
         */
        public ItemStack getRightItem() {
            return rightItem;
        }

        /**
         * It returns the output item that would have been the result
         * by combining the left and right one
         *
         * @return The outputItem
         */
        public ItemStack getOutputItem() {
            return outputItem;
        }

        /**
         * It returns the player that clicked onto the output slot
         *
         * @return The player
         */
        public Player getPlayer() {
            return player;
        }

        /**
         * It returns the text the player typed into the rename field
         *
         * @return The text of the rename field
         */
        public String getText() {
            return text;
        }
    }
}
