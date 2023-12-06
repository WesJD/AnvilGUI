package net.wesjd.anvilgui;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import net.md_5.bungee.api.chat.BaseComponent;
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
     * If the given ItemStack is null, return an air ItemStack, otherwise return the given ItemStack
     *
     * @param stack The ItemStack to check
     * @return air or the given ItemStack
     */
    private static ItemStack itemNotNull(ItemStack stack) {
        return stack == null ? AIR : stack;
    }

    /**
     * The {@link Plugin} that this anvil GUI is associated with
     */
    private final Plugin plugin;
    /**
     * The player who has the GUI open
     */
    private final Player player;
    /**
     * An {@link Executor} that executes tasks on the main server thread
     */
    private final Executor mainThreadExecutor;
    /**
     * The title of the anvil inventory
     */
    private final Object titleComponent;
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

    /** An {@link Consumer} that is called when the anvil GUI is close */
    private final Consumer<StateSnapshot> closeListener;
    /** A flag that decides whether the async click handler can be run concurrently */
    private final boolean concurrentClickHandlerExecution;
    /** An {@link BiFunction} that is called when a slot is clicked */
    private final ClickHandler clickHandler;

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
     * The actual container backing the Anvil GUI
     */
    private VersionWrapper.AnvilContainerWrapper container;

    /**
     * Create an AnvilGUI
     *
     * @param plugin           A {@link org.bukkit.plugin.java.JavaPlugin} instance
     * @param player           The {@link Player} to open the inventory for
     * @param mainThreadExecutor An {@link Executor} that executes on the main server thread
     * @param titleComponent   What to have the text already set to
     * @param initialContents  The initial contents of the inventory
     * @param preventClose     Whether to prevent the inventory from closing
     * @param closeListener    A {@link Consumer} when the inventory closes
     * @param concurrentClickHandlerExecution Flag to allow concurrent execution of the click handler
     * @param clickHandler     A {@link ClickHandler} that is called when the player clicks a slot
     */
    private AnvilGUI(
            Plugin plugin,
            Player player,
            Executor mainThreadExecutor,
            Object titleComponent,
            ItemStack[] initialContents,
            boolean preventClose,
            Set<Integer> interactableSlots,
            Consumer<StateSnapshot> closeListener,
            boolean concurrentClickHandlerExecution,
            ClickHandler clickHandler) {
        this.plugin = plugin;
        this.player = player;
        this.mainThreadExecutor = mainThreadExecutor;
        this.titleComponent = titleComponent;
        this.initialContents = initialContents;
        this.preventClose = preventClose;
        this.interactableSlots = Collections.unmodifiableSet(interactableSlots);
        this.closeListener = closeListener;
        this.concurrentClickHandlerExecution = concurrentClickHandlerExecution;
        this.clickHandler = clickHandler;
    }

    /**
     * Opens the anvil GUI
     */
    private void openInventory() {
        Bukkit.getPluginManager().registerEvents(listener, plugin);

        container = WRAPPER.newContainerAnvil(player, titleComponent);

        inventory = container.getBukkitInventory();
        // We need to use setItem instead of setContents because a Minecraft ContainerAnvil
        // contains two separate inventories: the result inventory and the ingredients inventory.
        // The setContents method only updates the ingredients inventory unfortunately,
        // but setItem handles the index going into the result inventory.
        for (int i = 0; i < initialContents.length; i++) {
            inventory.setItem(i, initialContents[i]);
        }

        containerId = WRAPPER.getNextContainerId(player, container);
        WRAPPER.handleInventoryCloseEvent(player);
        WRAPPER.sendPacketOpenWindow(player, containerId, titleComponent);
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
            closeListener.accept(StateSnapshot.fromAnvilGUI(this));
        }
    }

    /**
     * Updates the title of the AnvilGUI to the new one.
     *
     * @param literalTitle The title to use as literal text
     * @param preserveRenameText Whether to preserve the entered rename text
     * @throws IllegalArgumentException when literalTitle is null
     * @see Builder#title(String)
     */
    public void setTitle(String literalTitle, boolean preserveRenameText) {
        Validate.notNull(literalTitle, "literalTitle cannot be null");
        setTitle(WRAPPER.literalChatComponent(literalTitle), preserveRenameText);
    }

    /**
     * Updates the title of the AnvilGUI to the new one.
     *
     * @param json The json used to parse into a rich chat component
     * @param preserveRenameText Whether to preserve the entered rename text
     * @throws IllegalArgumentException when json is null
     * @see Builder#jsonTitle(String)
     */
    public void setJsonTitle(String json, boolean preserveRenameText) {
        Validate.notNull(json, "json cannot be null");
        setTitle(WRAPPER.jsonChatComponent(json), preserveRenameText);
    }

    /**
     * Updates the title of the AnvilGUI to the new one.
     *
     * @param title The title as a NMS ChatComponent
     * @param preserveRenameText Whether to preserve the entered rename text
     */
    private void setTitle(Object title, boolean preserveRenameText) {
        if (!WRAPPER.isCustomTitleSupported()) {
            return;
        }
        String renameText = container.getRenameText();
        WRAPPER.sendPacketOpenWindow(player, containerId, title);
        if (preserveRenameText) {
            // The renameText field is marked as @Nullable in newer versions
            container.setRenameText(renameText == null ? "" : renameText);
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

        /**
         * Boolean storing the running status of the latest click handler to prevent double execution.
         * All accesses to this boolean will be from the main server thread, except for the rare event
         * that the plugin is disabled and the mainThreadExecutor throws an exception
         */
        private boolean clickHandlerRunning = false;

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getInventory().equals(inventory)) {
                return;
            }

            final int rawSlot = event.getRawSlot();
            // ignore items dropped outside the window
            if (rawSlot == -999) return;

            final Player clicker = (Player) event.getWhoClicked();
            final Inventory clickedInventory = event.getClickedInventory();

            if (clickedInventory != null) {
                if (clickedInventory.equals(clicker.getInventory())) {
                    // prevent players from merging items from the anvil inventory
                    if (event.getClick().equals(ClickType.DOUBLE_CLICK)) {
                        event.setCancelled(true);
                        return;
                    }
                    // prevent shift moving items from players inv to the anvil inventory
                    if (event.isShiftClick()) {
                        event.setCancelled(true);
                        return;
                    }
                }
                // prevent players from swapping items in the anvil gui
                if ((event.getCursor() != null && event.getCursor().getType() != Material.AIR)
                        && !interactableSlots.contains(rawSlot)
                        && event.getClickedInventory().equals(inventory)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (rawSlot < 3 && rawSlot >= 0 || event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                event.setCancelled(!interactableSlots.contains(rawSlot));
                if (clickHandlerRunning && !concurrentClickHandlerExecution) {
                    // A click handler is running, don't launch another one
                    return;
                }

                final CompletableFuture<List<ResponseAction>> actionsFuture =
                        clickHandler.apply(rawSlot, StateSnapshot.fromAnvilGUI(AnvilGUI.this));

                final Consumer<List<ResponseAction>> actionsConsumer = actions -> {
                    for (final ResponseAction action : actions) {
                        action.accept(AnvilGUI.this, clicker);
                    }
                };

                if (actionsFuture.isDone()) {
                    // Fast-path without scheduling if clickHandler is performed in sync
                    // Because the future is already completed, .join() will not block the server thread
                    actionsFuture.thenAccept(actionsConsumer).join();
                } else {
                    clickHandlerRunning = true;
                    // If the plugin is disabled and the Executor throws an exception, the exception will be passed to
                    // the .handle method
                    actionsFuture
                            .thenAcceptAsync(actionsConsumer, mainThreadExecutor)
                            .handle((results, exception) -> {
                                if (exception != null) {
                                    plugin.getLogger()
                                            .log(
                                                    Level.SEVERE,
                                                    "An exception occurred in the AnvilGUI clickHandler",
                                                    exception);
                                }
                                // Whether an exception occurred or not, set running to false
                                clickHandlerRunning = false;
                                return null;
                            });
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
                    mainThreadExecutor.execute(AnvilGUI.this::openInventory);
                }
            }
        }
    }

    /** A builder class for an {@link AnvilGUI} object */
    public static class Builder {

        /** An {@link Executor} that executes tasks on the main server thread */
        private Executor mainThreadExecutor;
        /** An {@link Consumer} that is called when the anvil GUI is close */
        private Consumer<StateSnapshot> closeListener;
        /** A flag that decides whether the async click handler can be run concurrently */
        private boolean concurrentClickHandlerExecution = false;
        /** An {@link Function} that is called when a slot in the inventory has been clicked */
        private ClickHandler clickHandler;
        /** A state that decides where the anvil GUI is able to be closed by the user */
        private boolean preventClose = false;
        /** A set of integers containing the slot numbers that should be modifiable by the user. */
        private Set<Integer> interactableSlots = Collections.emptySet();
        /** The {@link Plugin} that this anvil GUI is associated with */
        private Plugin plugin;
        /** The text that will be displayed to the user */
        private Object titleComponent = WRAPPER.literalChatComponent("Repair & Name");
        /** The starting text on the item */
        private String itemText;
        /** An {@link ItemStack} to be put in the left input slot */
        private ItemStack itemLeft;
        /** An {@link ItemStack} to be put in the right input slot */
        private ItemStack itemRight;
        /** An {@link ItemStack} to be placed in the output slot */
        private ItemStack itemOutput;

        /**
         * Set a custom main server thread executor. Useful for plugins targeting Folia.
         *
         * @param executor The executor to run tasks on
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException when the executor is null
         */
        public Builder mainThreadExecutor(Executor executor) {
            Validate.notNull(executor, "Executor cannot be null");
            this.mainThreadExecutor = executor;
            return this;
        }

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
        public Builder onClose(Consumer<StateSnapshot> closeListener) {
            Validate.notNull(closeListener, "closeListener cannot be null");
            this.closeListener = closeListener;
            return this;
        }

        /**
         * Do an action when a slot is clicked in the inventory
         * <p>
         * The ClickHandler is only called when the previous execution of the ClickHandler has finished.
         * To alter this behaviour use {@link #allowConcurrentClickHandlerExecution()}
         *
         * @param clickHandler A {@link ClickHandler} that is called when the user clicks a slot. The
         *                     {@link Integer} is the slot number corresponding to {@link Slot}, the
         *                     {@link StateSnapshot} contains information about the current state of the anvil,
         *                     and the response is a {@link CompletableFuture} that will eventually return a
         *                     list of {@link ResponseAction} to execute in the order that they are supplied.
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException when the function supplied is null
         */
        public Builder onClickAsync(ClickHandler clickHandler) {
            Validate.notNull(clickHandler, "click function cannot be null");
            this.clickHandler = clickHandler;
            return this;
        }

        /**
         * By default, the {@link #onClickAsync(ClickHandler) async click handler} will not run concurrently
         * and instead wait for the previous {@link CompletableFuture} to finish before executing it again.
         * <p>
         * If this trait is desired, it can be enabled by calling this method but may lead to inconsistent
         * behaviour if not handled properly.
         *
         * @return The {@link Builder} instance
         */
        public Builder allowConcurrentClickHandlerExecution() {
            this.concurrentClickHandlerExecution = true;
            return this;
        }

        /**
         * Do an action when a slot is clicked in the inventory
         *
         * @param clickHandler A {@link BiFunction} that is called when the user clicks a slot. The
         *                     {@link Integer} is the slot number corresponding to {@link Slot}, the
         *                     {@link StateSnapshot} contains information about the current state of the anvil,
         *                     and the response is a list of {@link ResponseAction} to execute in the order
         *                     that they are supplied.
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException when the function supplied is null
         */
        public Builder onClick(BiFunction<Integer, StateSnapshot, List<ResponseAction>> clickHandler) {
            Validate.notNull(clickHandler, "click function cannot be null");
            this.clickHandler =
                    (slot, stateSnapshot) -> CompletableFuture.completedFuture(clickHandler.apply(slot, stateSnapshot));
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
         * Sets the initial item-text that is displayed to the user.
         * <br><br>
         * If the usage of Adventure Components is desired, you must create an item, set the displayname of it
         * and put it into the AnvilGUI via {@link #itemLeft(ItemStack)} manually.
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
         * Sets the AnvilGUI title that is to be displayed to the user.
         * <br>
         * The provided title will be treated as literal text.
         *
         * @param title The title that is to be displayed to the user
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException if the title is null
         */
        public Builder title(String title) {
            Validate.notNull(title, "title cannot be null");
            this.titleComponent = WRAPPER.literalChatComponent(title);
            return this;
        }

        /**
         * Sets the AnvilGUI title that is to be displayed to the user.
         * <br>
         * The provided json will be parsed into rich chat components.
         *
         * @param json The title that is to be displayed to the user
         * @return The {@link Builder} instance
         * @throws IllegalArgumentException if the title is null
         * @see net.md_5.bungee.chat.ComponentSerializer#toString(BaseComponent)
         */
        public Builder jsonTitle(String json) {
            Validate.notNull(json, "json cannot be null");
            this.titleComponent = WRAPPER.jsonChatComponent(json);
            return this;
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
         * @throws IllegalArgumentException when the onClick function, plugin, or player is null
         */
        public AnvilGUI open(Player player) {
            Validate.notNull(plugin, "Plugin cannot be null");
            Validate.notNull(clickHandler, "click handler cannot be null");
            Validate.notNull(player, "Player cannot be null");

            if (itemText != null) {
                if (itemLeft == null) {
                    itemLeft = new ItemStack(Material.PAPER);
                }

                ItemMeta paperMeta = itemLeft.getItemMeta();
                paperMeta.setDisplayName(itemText);
                itemLeft.setItemMeta(paperMeta);
            }

            // If no executor is specified, execute all tasks with the BukkitScheduler
            if (mainThreadExecutor == null) {
                mainThreadExecutor = task -> Bukkit.getScheduler().runTask(plugin, task);
            }

            final AnvilGUI anvilGUI = new AnvilGUI(
                    plugin,
                    player,
                    mainThreadExecutor,
                    titleComponent,
                    new ItemStack[] {itemLeft, itemRight, itemOutput},
                    preventClose,
                    interactableSlots,
                    closeListener,
                    concurrentClickHandlerExecution,
                    clickHandler);
            anvilGUI.openInventory();
            return anvilGUI;
        }
    }

    /**
     * A handler that is called when the user clicks a slot. The
     * {@link Integer} is the slot number corresponding to {@link Slot}, the
     * {@link StateSnapshot} contains information about the current state of the anvil,
     * and the response is a {@link CompletableFuture} that will eventually return a
     * list of {@link ResponseAction} to execute in the order that they are supplied.
     */
    @FunctionalInterface
    public interface ClickHandler extends BiFunction<Integer, StateSnapshot, CompletableFuture<List<ResponseAction>>> {}

    /** An action to run in response to a player clicking the output slot in the GUI. This interface is public
     * and permits you, the developer, to add additional response features easily to your custom AnvilGUIs. */
    @FunctionalInterface
    public interface ResponseAction extends BiConsumer<AnvilGUI, Player> {

        /**
         * Replace the input text box value with the provided text value.
         *
         * Before using this method, it must be verified by the caller that items are either in
         * {@link Slot#INPUT_LEFT} or {@link Slot#OUTPUT} present.
         *
         * @param text The text to write in the input box
         * @return The {@link ResponseAction} to achieve the text replacement
         * @throws IllegalArgumentException when the text is null
         * @throws IllegalStateException when the slots {@link Slot#INPUT_LEFT} and {@link Slot#OUTPUT} are <code>null</code>
         */
        static ResponseAction replaceInputText(String text) {
            Validate.notNull(text, "text cannot be null");
            return (anvilgui, player) -> {
                ItemStack item = anvilgui.getInventory().getItem(Slot.OUTPUT);
                if (item == null) {
                    // Fallback on left input slot if player hasn't typed anything yet
                    item = anvilgui.getInventory().getItem(Slot.INPUT_LEFT);
                }
                if (item == null) {
                    throw new IllegalStateException(
                            "replaceInputText can only be used if slots OUTPUT or INPUT_LEFT are not empty");
                }

                final ItemStack cloned = item.clone();
                final ItemMeta meta = cloned.getItemMeta();
                meta.setDisplayName(text);
                cloned.setItemMeta(meta);
                anvilgui.getInventory().setItem(Slot.INPUT_LEFT, cloned);
            };
        }

        /**
         * Updates the title of the AnvilGUI to the new one.
         *
         * @param literalTitle The title to use as literal text
         * @param preserveRenameText Whether to preserve the entered rename text
         * @throws IllegalArgumentException when literalTitle is null
         * @see Builder#title(String)
         */
        static ResponseAction updateTitle(String literalTitle, boolean preserveRenameText) {
            Validate.notNull(literalTitle, "literalTitle cannot be null");
            return (anvilGUI, player) -> anvilGUI.setTitle(literalTitle, preserveRenameText);
        }

        /**
         * Updates the title of the AnvilGUI to the new one.
         *
         * @param json The json used to parse into a rich chat component
         * @param preserveRenameText Whether to preserve the entered rename text
         * @throws IllegalArgumentException when json is null
         * @see Builder#jsonTitle(String)
         */
        static ResponseAction updateJsonTitle(String json, boolean preserveRenameText) {
            Validate.notNull(json, "json cannot be null");
            return (anvilGUI, player) -> anvilGUI.setJsonTitle(json, preserveRenameText);
        }

        /**
         * Open another inventory
         * @param otherInventory The inventory to open
         * @return The {@link ResponseAction} to achieve the inventory open
         * @throws IllegalArgumentException when the otherInventory is null
         */
        static ResponseAction openInventory(Inventory otherInventory) {
            Validate.notNull(otherInventory, "otherInventory cannot be null");
            return (anvilgui, player) -> player.openInventory(otherInventory);
        }

        /**
         * Close the AnvilGUI
         * @return The {@link ResponseAction} to achieve closing the AnvilGUI
         */
        static ResponseAction close() {
            return (anvilgui, player) -> anvilgui.closeInventory();
        }

        /**
         * Run the provided runnable
         * @param runnable The runnable to run
         * @return The {@link ResponseAction} to achieve running the runnable
         * @throws IllegalArgumentException when the runnable is null
         */
        static ResponseAction run(Runnable runnable) {
            Validate.notNull(runnable, "runnable cannot be null");
            return (anvilgui, player) -> runnable.run();
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

    /** Represents a snapshot of the state of an AnvilGUI */
    public static final class StateSnapshot {

        /**
         * Create an {@link StateSnapshot} from the current state of an {@link AnvilGUI}
         * @param anvilGUI The instance to take the snapshot of
         * @return The snapshot
         */
        private static StateSnapshot fromAnvilGUI(AnvilGUI anvilGUI) {
            final Inventory inventory = anvilGUI.getInventory();
            return new StateSnapshot(
                    itemNotNull(inventory.getItem(Slot.INPUT_LEFT)).clone(),
                    itemNotNull(inventory.getItem(Slot.INPUT_RIGHT)).clone(),
                    itemNotNull(inventory.getItem(Slot.OUTPUT)).clone(),
                    anvilGUI.player);
        }

        /**
         * The {@link ItemStack} in the anvilGui slots
         */
        private final ItemStack leftItem, rightItem, outputItem;

        /**
         * The {@link Player} that clicked the output slot
         */
        private final Player player;

        /**
         * The event parameter constructor
         * @param leftItem The left item in the combine slot of the anvilGUI
         * @param rightItem The right item in the combine slot of the anvilGUI
         * @param outputItem The item that would have been outputted, when the items would have been combined
         * @param player The player that clicked the output slot
         */
        public StateSnapshot(ItemStack leftItem, ItemStack rightItem, ItemStack outputItem, Player player) {
            this.leftItem = leftItem;
            this.rightItem = rightItem;
            this.outputItem = outputItem;
            this.player = player;
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
            return outputItem.hasItemMeta() ? outputItem.getItemMeta().getDisplayName() : "";
        }
    }
}
