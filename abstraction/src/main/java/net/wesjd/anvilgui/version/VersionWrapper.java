package net.wesjd.anvilgui.version;

import org.bukkit.inventory.Inventory;

public interface VersionWrapper {

    Inventory create();

    void open();

    void close();

}
