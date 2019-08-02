package net.wesjd.anvilgui.version;

import net.minecraft.server.v1_14_R1.*;
import net.wesjd.version.special.AnvilContainer1_14_4_R1;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Wrapper1_14_R1 implements VersionWrapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketOpenWindow(Player player, int containerId) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, new ChatMessage("Repair & Name")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).activeContainer = toNMS(player).defaultContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainer(Player player, Object container) {
        toNMS(player).activeContainer = (Container) container;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerId(Object container, int containerId) {
        try {
            final Field field = Container.class.getField("windowId");

            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(container, containerId);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        ((Container) container).addSlotListener(toNMS(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Inventory toBukkitInventory(Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object newContainerAnvil(Player player) {
        if (Bukkit.getBukkitVersion().contains("1.14.4")) {
            return new AnvilContainer1_14_4_R1(player, getNextContainerId(player));
        }
        return new Wrapper1_14_R1.AnvilContainer(player);
    }

    /**
     * Turns a {@link Player} into an NMS one
     *
     * @param player The player to be converted
     * @return the NMS EntityPlayer
     */
    private EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * Modifications to ContainerAnvil that makes it so you don't have to have xp to use this anvil
     */
    private class AnvilContainer extends ContainerAnvil {

        public AnvilContainer(Player player) {
            super(Wrapper1_14_R1.this.getNextContainerId(player), ((CraftPlayer) player).getHandle().inventory,
                    ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(new ChatMessage("Repair & Name"));
        }

        @Override
        public void e() {
            super.e();
            this.levelCost.a(0);
        }

    }

}
