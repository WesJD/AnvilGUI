package net.wesjd.anvilgui.version;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.*;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public final class MojangWrapper1_21_R4 implements VersionWrapper {
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
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player), InventoryCloseEvent.Reason.UNKNOWN);
        toNMS(player).doCloseContainer(); // p -> doCloseContainer
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
        toNMS(player).containerMenu = toNMS(player).inventoryMenu; // bR -> containerMenu, bQ -> inventoryMenu
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
        return Component.literal(content); // IChatBaseComponent.b -> Component.literal
    }

    @Override
    public Object jsonChatComponent(String json) {
        return Component.Serializer.fromJson(json, RegistryAccess.EMPTY);
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
            Slot output = this.getSlot(2); // b -> getSlot
            if (!output.hasItem()) { // h -> hasItem
                output.set(this.getSlot(0).getItem().copy()); // f -> set, g -> getItem, v -> copy
            }

            this.cost.set(0); // y -> cost, a -> set

            // Sync to the client
            this.sendAllDataToRemote(); // b -> sendAllDataToRemote
            this.broadcastChanges(); // d -> broadcastChanges
        }

        @Override
        public void removed(net.minecraft.world.entity.player.@NotNull Player player) {}

        @Override
        protected void clearContainer(
                net.minecraft.world.entity.player.@NotNull Player player, @NotNull Container container) {}

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
            Slot inputLeft = getSlot(0);
            if (inputLeft.hasItem()) {
                inputLeft
                        .getItem()
                        .set(
                                DataComponents.CUSTOM_NAME,
                                Component.literal(text)); // DataComponents.g -> DataComponents.CUSTOM_NAME
            }
        }

        @Override
        public Inventory getBukkitInventory() {
            // NOTE: We need to call Container#getBukkitView() instead of ContainerAnvil#getBukkitView()
            // because ContainerAnvil#getBukkitView() had an ABI breakage in the middle of the Minecraft 1.21
            // development cycle for Spigot. For more info, see: https://github.com/WesJD/AnvilGUI/issues/342
            return ((AbstractContainerMenu) this).getBukkitView().getTopInventory();
        }
    }
}
