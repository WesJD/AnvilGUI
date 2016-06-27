package net.wesjd.anvilgui.version;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 BuildStatic
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
public abstract class VersionWrapper {

    public abstract int getNextContainerId(Player player);

    public abstract void handleInventoryCloseEvent(Player player);

    public abstract void sendPacketOpenWindow(Player player, int containerId);

    public abstract void sendPacketCloseWindow(Player player, int containerId);

    public abstract void setActiveContainerDefault(Player player);

    public abstract void setActiveContainer(Player player, Object container);

    public abstract void setActiveContainerId(Object container, int containerId);

    public abstract void addActiveContainerSlotListener(Object container, Player player);

    public abstract Inventory toBukkitInventory(Object container);

    public abstract Object newContainerAnvil(Player player);

}
