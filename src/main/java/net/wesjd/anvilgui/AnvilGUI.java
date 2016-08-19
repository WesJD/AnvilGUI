package net.wesjd.anvilgui;

import net.wesjd.anvilgui.version.Version;
import net.wesjd.anvilgui.version.VersionWrapper;
import org.apache.commons.lang3.Validate;
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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Wesley Smith
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class AnvilGUI implements Listener {

    private final Player holder;
    private final ItemStack insert;
    private final BiFunction<Player, String, String> biFunction;

    private final VersionWrapper wrapper;
    private final int containerId;
    private final Inventory inventory;

    private boolean open = false;

    @Deprecated
    public AnvilGUI(Plugin plugin, Player holder, String insert, ClickHandler clickHandler) {
        this(plugin, holder, insert, clickHandler::onClick);
    }

    public AnvilGUI(Plugin plugin, Player holder, String insert, BiFunction<Player, String, String> biFunction) {
        this.holder = holder;
        this.biFunction = biFunction;

        final ItemStack paper = new ItemStack(Material.PAPER);
        final ItemMeta paperMeta = paper.getItemMeta();
        paperMeta.setDisplayName(insert);
        paper.setItemMeta(paperMeta);
        this.insert = paper;

        final Version version = Version.of(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
        Validate.notNull(version, "Your server version isn't supported in AnvilGUI!");
        wrapper = version.getWrapper();

        wrapper.handleInventoryCloseEvent(holder);
        wrapper.setActiveContainerDefault(holder);

        Bukkit.getPluginManager().registerEvents(this, plugin);

        final Object container = wrapper.newContainerAnvil(holder);

        inventory = wrapper.toBukkitInventory(container);
        inventory.setItem(Slot.INPUT_LEFT, this.insert);

        containerId = wrapper.getNextContainerId(holder);
        wrapper.sendPacketOpenWindow(holder, containerId);
        wrapper.setActiveContainer(holder, container);
        wrapper.setActiveContainerId(container, containerId);
        wrapper.addActiveContainerSlotListener(container, holder);

        open = true;
    }

    public void closeInventory() {
        Validate.isTrue(open, "You can't close an inventory that isn't open!");
        open = false;

        wrapper.handleInventoryCloseEvent(holder);
        wrapper.setActiveContainerDefault(holder);
        wrapper.sendPacketCloseWindow(holder, containerId);

        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getInventory().equals(inventory)) {
            e.setCancelled(true);
            final Player clicker = (Player) e.getWhoClicked();
            if(e.getRawSlot() == Slot.OUTPUT) {
                final ItemStack clicked = inventory.getItem(e.getRawSlot());
                if(clicked == null || clicked.getType() == Material.AIR) return;
                final String ret = biFunction.apply(clicker, clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : clicked.getType().toString());
                if(ret != null) {
                    final ItemMeta meta = clicked.getItemMeta();
                    meta.setDisplayName(ret);
                    clicked.setItemMeta(meta);
                    inventory.setItem(e.getRawSlot(), clicked);
                } else closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if(open && e.getInventory().equals(inventory)) closeInventory();
    }

    @Deprecated
    public static abstract class ClickHandler {

        public abstract String onClick(Player clicker, String input);

    }

    public static class Slot {

        public static final int INPUT_LEFT = 0;
        public static final int INPUT_RIGHT = 1;
        public static final int OUTPUT = 2;

    }

}
