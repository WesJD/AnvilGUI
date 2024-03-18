package net.wesjd.anvilgui.version;


import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.*;
import net.wesjd.anvilgui.version.special.AnvilContainer1_17_1_R1;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Wrapper1_17_R1 implements VersionWrapper {

    private final boolean IS_ONE_SEVENTEEN_ONE = Bukkit.getBukkitVersion().contains("1.17.1");

    private int getRealNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNextContainerId(Player player, AnvilContainerWrapper container) {
        if (IS_ONE_SEVENTEEN_ONE) {
            return ((AnvilContainer1_17_1_R1) container).getContainerId();
        }
        return ((AnvilContainer) container).getContainerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
        toNMS(player).o(); // o -> doCloseContainer
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketOpenWindow(Player player, int containerId, Object guiTitle) {
        toNMS(player).b.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.h, (IChatBaseComponent)
                guiTitle));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).b.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerDefault(Player player) {
        (toNMS(player)).bV = (Container) (toNMS(player)).bU;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainer(Player player, AnvilContainerWrapper container) {
        (toNMS(player)).bV = (Container) container;
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
        toNMS(player).initMenu((Container) container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnvilContainerWrapper newContainerAnvil(Player player, Object guiTitle) {
        if (IS_ONE_SEVENTEEN_ONE) {
            return new AnvilContainer1_17_1_R1(player, getRealNextContainerId(player), (IChatBaseComponent) guiTitle);
        }
        return new AnvilContainer(player, (IChatBaseComponent) guiTitle);
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
                    Wrapper1_17_R1.this.getRealNextContainerId(player),
                    ((CraftPlayer) player).getHandle().getInventory(),
                    ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(guiTitle);
        }

        @Override
        public void i() {
            // If the output is empty copy the left input into the output
            Slot output = this.getSlot(2);
            if (!output.hasItem()) {
                Slot input = this.getSlot(0);

                if (input.hasItem()) {
                    output.set(input.getItem().cloneItemStack());
                }
            }

            this.w.set(0);

            // Sync to the client
            // This call has been added in 1.17.1, to fix
            // https://hub.spigotmc.org/jira/projects/SPIGOT/issues/SPIGOT-6686
            // but we can backport it here to 1.17
            this.updateInventory();
            this.d();
        }

        @Override
        public void b(EntityHuman player) {}

        @Override
        protected void a(EntityHuman player, IInventory container) {}

        public int getContainerId() {
            return this.j;
        }

        @Override
        public String getRenameText() {
            return this.v;
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
