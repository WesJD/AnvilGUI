package net.wesjd.anvilgui.version;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class Wrapper26_R1 implements VersionWrapper {
    private int getRealNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }

    /**
     * Turns a {@link Player} into an NMS one
     *
     * @param player The player to be converted
     * @return the NMS EntityPlayer
     */
    private ServerPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    @Override
    public int getNextContainerId(Player player, AnvilContainerWrapper container) {
        return ((AnvilContainer) container).getContainerId();
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        try {
            // As of 1.21.5, this method only exists on Spigot, not on Paper.
            // It was removed from Paper here:
            // https://github.com/PaperMC/Paper/commit/f00727c57e564f3a8cb875183a54142feb693db7
            CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
        } catch (NoSuchMethodError ignored) {
            // Workaround for Paper servers
            try {
                Class<?> inventoryCloseEventReasonClass =
                        Class.forName("org.bukkit.event.inventory.InventoryCloseEvent$Reason");
                Method handleInventoryCloseEventMethod = CraftEventFactory.class.getMethod(
                        "handleInventoryCloseEvent", ServerPlayer.class, inventoryCloseEventReasonClass);
                handleInventoryCloseEventMethod.invoke(
                        null,
                        toNMS(player),
                        inventoryCloseEventReasonClass.getField("UNKNOWN").get(null));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        toNMS(player).doCloseContainer();
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId, Object inventoryTitle) {
        toNMS(player).connection.send(new ClientboundOpenScreenPacket(containerId, MenuType.ANVIL, (Component)
                inventoryTitle));
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).connection.send(new ClientboundContainerClosePacket(containerId));
    }

    @Override
    public void sendPacketExperienceChange(Player player, int experienceLevel) {
        toNMS(player).connection.send(new ClientboundSetExperiencePacket(0f, 0, experienceLevel));
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).containerMenu = toNMS(player).inventoryMenu;
    }

    @Override
    public void setActiveContainer(Player player, AnvilContainerWrapper container) {
        toNMS(player).containerMenu = (AbstractContainerMenu) container;
    }

    @Override
    public void setActiveContainerId(AnvilContainerWrapper container, int containerId) {}

    @Override
    public void addActiveContainerSlotListener(AnvilContainerWrapper container, Player player) {
        toNMS(player).initMenu((AbstractContainerMenu) container);
    }

    @Override
    public AnvilContainerWrapper newContainerAnvil(Player player, Object title) {
        return new AnvilContainer(player, getRealNextContainerId(player), (Component) title);
    }

    @Override
    public Object literalChatComponent(String content) {
        return Component.literal(content);
    }

    @Override
    public Object jsonChatComponent(String json) {
        return CraftChatMessage.fromJSON(json);
    }

    private static class AnvilContainer extends AnvilMenu implements AnvilContainerWrapper {
        public AnvilContainer(Player player, int containerId, Component guiTitle) {
            super(
                    containerId,
                    ((CraftPlayer) player).getHandle().getInventory(),
                    ContainerLevelAccess.create(((CraftWorld) player.getWorld()).getHandle(), new BlockPos(0, 0, 0)));
            this.checkReachable = false;
            setTitle(guiTitle);
        }

        @Override
        public void createResult() {
            // If the output is empty copy the left input into the output
            Slot output = this.getSlot(2);
            if (!output.hasItem()) {
                output.set(this.getSlot(0).getItem().copy());
            }

            this.cost.set(0);

            // Sync to the client
            this.sendAllDataToRemote();
            this.broadcastChanges();
        }

        @Override
        public void removed(net.minecraft.world.entity.player.Player player) {}

        @Override
        protected void clearContainer(net.minecraft.world.entity.player.Player player, Container container) {}

        public int getContainerId() {
            return this.containerId;
        }

        @Override
        public String getRenameText() {
            return this.itemName;
        }

        @Override
        public void setRenameText(String text) {
            // If an item is present in the left input slot change its hover name to the literal text.
            Slot inputLeft = this.getSlot(0);
            if (inputLeft.hasItem()) {
                inputLeft.getItem().set(DataComponents.CUSTOM_NAME, Component.literal(text));
            }
        }

        @Override
        public Inventory getBukkitInventory() {
            return this.getBukkitView().getTopInventory();
        }
    }
}
