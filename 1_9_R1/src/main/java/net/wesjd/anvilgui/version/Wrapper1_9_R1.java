package net.wesjd.anvilgui.version;

import net.minecraft.server.v1_9_R1.*;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * {@link VersionWrapper} implemented for NMS version 1_9_R1
 *
 * @author Wesley Smith
 * @since 1.0
 */
public final class Wrapper1_9_R1 implements VersionWrapper {

    /**
     * Turns a {@link Player} into an NMS one
     *
     * @param player The player to be converted
     * @return the NMS EntityPlayer
     */
    private static EntityPlayer toNMS(final Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    @Override
    public int getNextContainerId(final Player player, final Object container) {
        return Wrapper1_9_R1.toNMS(player).nextContainerCounter();
    }

    @Override
    public void handleInventoryCloseEvent(final Player player) {
        CraftEventFactory.handleInventoryCloseEvent(Wrapper1_9_R1.toNMS(player));
    }

    @Override
    public void sendPacketOpenWindow(final Player player, final int containerId, final String guiTitle) {
        Wrapper1_9_R1.toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name")));
    }

    @Override
    public void sendPacketCloseWindow(final Player player, final int containerId) {
        Wrapper1_9_R1.toNMS(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void setActiveContainerDefault(final Player player) {
        Wrapper1_9_R1.toNMS(player).activeContainer = Wrapper1_9_R1.toNMS(player).defaultContainer;
    }

    @Override
    public void setActiveContainer(final Player player, final Object container) {
        Wrapper1_9_R1.toNMS(player).activeContainer = (Container) container;
    }

    @Override
    public void setActiveContainerId(final Object container, final int containerId) {
        ((Container) container).windowId = containerId;
    }

    @Override
    public void addActiveContainerSlotListener(final Object container, final Player player) {
        ((Container) container).addSlotListener(Wrapper1_9_R1.toNMS(player));
    }

    @Override
    public Inventory toBukkitInventory(final Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    @Override
    public Object newContainerAnvil(final Player player, final String guiTitle) {
        return new Wrapper1_9_R1.AnvilContainer(Wrapper1_9_R1.toNMS(player));
    }

    /**
     * Modifications to ContainerAnvil that makes it so you don't have to have xp to use this anvil
     */
    private static final class AnvilContainer extends ContainerAnvil {

        private AnvilContainer(final EntityHuman entityhuman) {
            super(entityhuman.inventory, entityhuman.world, new BlockPosition(0, 0, 0), entityhuman);
        }

        @Override
        public boolean a(final EntityHuman entityhuman) {
            return true;
        }

    }

}
