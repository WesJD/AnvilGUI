package net.wesjd.anvilgui.version;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Delegates to Spigot or Paper wrapper for Minecraft 1.21 R5 depending on runtime environment.
 */
public final class Wrapper1_21_R5 implements VersionWrapper {

    private final VersionWrapper delegate;

    public Wrapper1_21_R5() {
        if (!isPaper()) {
            this.delegate = new Wrapper1_21_R5_Spigot();
        } else {
            throw new IllegalStateException("Wrapper does not support Paper atm");
        }
    }

    /**
     * Detects if the server is Paper by checking for the presence of Mojang-mapped class names.
     */
    private boolean isPaper() {
        return !Bukkit.getServer().getClass().getPackage().getName().contains(".v");
    }

    @Override
    public int getNextContainerId(Player player, AnvilContainerWrapper container) {
        return delegate.getNextContainerId(player, container);
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        delegate.handleInventoryCloseEvent(player);
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId, Object inventoryTitle) {
        delegate.sendPacketOpenWindow(player, containerId, inventoryTitle);
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        delegate.sendPacketCloseWindow(player, containerId);
    }

    @Override
    public void sendPacketExperienceChange(Player player, int experienceLevel) {
        delegate.sendPacketExperienceChange(player, experienceLevel);
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        delegate.setActiveContainerDefault(player);
    }

    @Override
    public void setActiveContainer(Player player, AnvilContainerWrapper container) {
        delegate.setActiveContainer(player, container);
    }

    @Override
    public void setActiveContainerId(AnvilContainerWrapper container, int containerId) {
        delegate.setActiveContainerId(container, containerId);
    }

    @Override
    public void addActiveContainerSlotListener(AnvilContainerWrapper container, Player player) {
        delegate.addActiveContainerSlotListener(container, player);
    }

    @Override
    public AnvilContainerWrapper newContainerAnvil(Player player, Object title) {
        return delegate.newContainerAnvil(player, title);
    }

    @Override
    public Object literalChatComponent(String content) {
        return delegate.literalChatComponent(content);
    }

    @Override
    public Object jsonChatComponent(String json) {
        return delegate.jsonChatComponent(json);
    }
}
