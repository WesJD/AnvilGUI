package net.wesjd.anvilgui.version;

import net.minecraft.server.v1_14_R1.*;
import net.wesjd.anvilgui.version.special.AnvilContainer1_14_4_R1;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Wrapper1_14_R1 implements VersionWrapper {

    private final boolean IS_ONE_FOURTEEN = Bukkit.getBukkitVersion().contains("1.14.4");

    private int getRealNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNextContainerId(Player player, AnvilContainerWrapper container) {
        if (IS_ONE_FOURTEEN) {
            return ((AnvilContainer1_14_4_R1) container).getContainerId();
        } else {
            return ((AnvilContainer) container).getContainerId();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
        toNMS(player).m(); // m -> doCloseContainer
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketOpenWindow(Player player, int containerId, Object guiTitle) {
        toNMS(player)
                .playerConnection
                .sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, (IChatBaseComponent) guiTitle));
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
    public void setActiveContainer(Player player, AnvilContainerWrapper container) {
        toNMS(player).activeContainer = (Container) container;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerId(AnvilContainerWrapper container, int containerId) {
        // noop
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addActiveContainerSlotListener(AnvilContainerWrapper container, Player player) {
        ((Container) container).addSlotListener(toNMS(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnvilContainerWrapper newContainerAnvil(Player player, Object guiTitle) {
        if (IS_ONE_FOURTEEN) {
            return new AnvilContainer1_14_4_R1(player, getRealNextContainerId(player), (IChatBaseComponent) guiTitle);
        } else {
            return new Wrapper1_14_R1.AnvilContainer(player, (IChatBaseComponent) guiTitle);
        }
    }

    @Override
    public Object literalChatComponent(String content) {
        return new ChatComponentText(content);
    }

    @Override
    public Object jsonChatComponent(String json) {
        return IChatBaseComponent.ChatSerializer.a(json);
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
    private class AnvilContainer extends ContainerAnvil implements AnvilContainerWrapper {

        public AnvilContainer(Player player, IChatBaseComponent guiTitle) {
            super(
                    getRealNextContainerId(player),
                    ((CraftPlayer) player).getHandle().inventory,
                    ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(guiTitle);
        }

        @Override
        public void e() {
            // If the output is empty copy the left input into the output
            Slot output = this.getSlot(2);
            if (!output.hasItem()) {
                Slot input = this.getSlot(0);

                if (input.hasItem()) {
                    output.set(input.getItem().cloneItemStack());
                }
            }

            this.levelCost.a(0);

            // Sync to the client
            this.c();
        }

        @Override
        public void b(EntityHuman entityhuman) {}

        @Override
        protected void a(EntityHuman entityhuman, World world, IInventory iinventory) {}

        public int getContainerId() {
            return windowId;
        }

        @Override
        public String getRenameText() {
            return this.renameText;
        }

        @Override
        public void setRenameText(String text) {
            // If an item is present in the left input slot change its hover name to the literal text.
            Slot inputLeft = getSlot(0);
            if (inputLeft.hasItem()) {
                inputLeft.getItem().a(new ChatComponentText(text));
            }
        }

        @Override
        public Inventory getBukkitInventory() {
            return getBukkitView().getTopInventory();
        }
    }
}
