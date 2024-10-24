package net.wesjd.anvilgui.version;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutExperience;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.*;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class Wrapper1_21_R1 implements VersionWrapper {
    private int getRealNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
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

    @Override
    public int getNextContainerId(Player player, AnvilContainerWrapper container) {
        return ((AnvilContainer) container).getContainerId();
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
        toNMS(player).t(); // t -> doCloseContainer
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId, Object inventoryTitle) {
        toNMS(player).c.b(new PacketPlayOutOpenWindow(containerId, Containers.i, (IChatBaseComponent) inventoryTitle));
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).c.b(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void sendPacketExperienceChange(Player player, int experienceLevel) {
        toNMS(player).c.b(new PacketPlayOutExperience(0f, 0, experienceLevel));
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).cd = toNMS(player).cc; // cd -> containerMenu, cc -> inventoryMenu
    }

    @Override
    public void setActiveContainer(Player player, AnvilContainerWrapper container) {
        toNMS(player).cd = (Container) container;
    }

    @Override
    public void setActiveContainerId(AnvilContainerWrapper container, int containerId) {}

    @Override
    public void addActiveContainerSlotListener(AnvilContainerWrapper container, Player player) {
        toNMS(player).a((Container) container);
    }

    @Override
    public AnvilContainerWrapper newContainerAnvil(Player player, Object title) {
        return new AnvilContainer(player, getRealNextContainerId(player), (IChatBaseComponent) title);
    }

    @Override
    public Object literalChatComponent(String content) {
        return IChatBaseComponent.b(content); // IChatBaseComponent.b -> Component.literal
    }

    @Override
    public Object jsonChatComponent(String json) {
        return IChatBaseComponent.ChatSerializer.a(json, IRegistryCustom.b);
    }

    private static class AnvilContainer extends ContainerAnvil implements AnvilContainerWrapper {

        private int finalLevelCost = 0;

        public AnvilContainer(Player player, int containerId, IChatBaseComponent guiTitle) {
            super(
                    containerId,
                    ((CraftPlayer) player).getHandle().fY(),
                    ContainerAccess.a(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(guiTitle);
        }

        @Override
        public void m() {
            // If the output is empty copy the left input into the output
            Slot output = this.b(2); // b -> getSlot
            if (!output.h()) { // h -> hasItem
                output.f(this.b(0).g().s()); // f -> set, g -> getItem, s -> copy
            }

            this.w.a(finalLevelCost); // w -> cost, a -> set

            // Sync to the client
            this.b(); // b -> sendAllDataToRemote
            this.d(); // d -> broadcastChanges
        }

        @Override
        public void a(EntityHuman player) {}

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
            Slot inputLeft = b(0);
            if (inputLeft.h()) {
                inputLeft
                        .g()
                        .b(
                                DataComponents.g,
                                IChatBaseComponent.b(text)); // DataComponents.g -> DataComponents.CUSTOM_NAME
            }
        }

        @Override
        public void setLevelCost(int levelCost) {
            this.finalLevelCost = levelCost;
        }

        @Override
        public Inventory getBukkitInventory() {
            // NOTE: We need to call Container#getBukkitView() instead of ContainerAnvil#getBukkitView()
            // because ContainerAnvil#getBukkitView() had an ABI breakage in the middle of the Minecraft 1.21
            // development cycle for Spigot. For more info, see: https://github.com/WesJD/AnvilGUI/issues/342
            return ((Container) this).getBukkitView().getTopInventory();
        }
    }
}
