package net.wesjd.anvilgui;

import net.wesjd.anvilgui.version.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.function.BiFunction;

public class AnvilGUI {

    private static final VersionMatcher versions = new VersionMatcher(
        7,
        Wrapper1_8_R1.class,
        Wrapper1_8_R2.class,
        Wrapper1_8_R3.class,
        Wrapper1_9_R1.class,
        Wrapper1_9_R2.class,
        Wrapper1_10_R1.class,
        Wrapper1_11_R1.class,
        Wrapper1_12_R1.class,
        Wrapper1_13_R1.class,
        Wrapper1_13_R2.class,
        Wrapper1_14_R1.class
    );

    private final Listener listener = new AnvilListener();
    private final VersionWrapper anvil;
    private final Inventory inventory;
    private final BiFunction<Player, String, String> function;
    private Boolean isOpen;

    public AnvilGUI(Plugin plugin, Player player, String insert, BiFunction<Player, String, String> function) {
        this.function = function;
        anvil = versions.nms(player);

        Bukkit.getPluginManager().registerEvents(listener, plugin);

        inventory = anvil.create();

        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(insert);
        paper.setItemMeta(meta);

        inventory.setItem(SlotType.INPUT_LEFT.id, paper);

        Bukkit.getScheduler().runTask(plugin, anvil::open);

        isOpen = true;
    }

    public void close() {
        Validate.isTrue(isOpen, "You can't close an inventory that isn't open!");

        isOpen = false;

        anvil.close();

        HandlerList.unregisterAll(listener);
    }

    private class AnvilListener implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (inventory == event.getInventory()) {
                event.setCancelled(true);

                if (event.getClick() != ClickType.LEFT)
                    return;

                if (event.getRawSlot() == SlotType.OUTPUT.id) {
                    ItemStack clicked = inventory.getItem(event.getRawSlot());
                    final ItemMeta meta = clicked.getItemMeta();

                    if (clicked.getType() == Material.AIR)
                        return;

                    final String ret = function.apply(
                        (Player) event.getWhoClicked(),
                        clicked.hasItemMeta() ? meta.getDisplayName() : clicked.getType().toString()
                    );

                    if (ret == null) {
                        event.getWhoClicked().closeInventory();
                        return;
                    }

                    meta.setDisplayName(ret);
                    clicked.setItemMeta(meta);
                    inventory.setItem(event.getRawSlot(), clicked);
                }
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if (isOpen && inventory == event.getInventory())
                close();
        }

    }

    enum SlotType {
        INPUT_LEFT(0),
        INPUT_RIGHT(1),
        OUTPUT(2);

        private int id;

        SlotType(int id) {
            this.id = id;
        }
    }

}
