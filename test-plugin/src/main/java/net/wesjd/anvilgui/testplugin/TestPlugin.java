package net.wesjd.anvilgui.testplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("anvilgui").setExecutor(new AnvilGUICommand(this));
    }
}
