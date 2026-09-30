package net.wesjd.anvilgui.version;

import java.util.function.Function;
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
import net.minecraft.world.inventory.Container;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class Wrapper1_21_R3 implements VersionWrapper {
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
        toNMS(player).q(); // q -> doCloseContainer
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId, Object inventoryTitle) {
        toNMS(player).f.b(new PacketPlayOutOpenWindow(containerId, Containers.i, (IChatBaseComponent) inventoryTitle));
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).f.b(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void sendPacketExperienceChange(Player player, int experienceLevel) {
        toNMS(player).f.b(new PacketPlayOutExperience(0f, 0, experienceLevel));
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

        private Function<String, ItemStack> renameVisitor;

        public AnvilContainer(Player player, int containerId, IChatBaseComponent guiTitle) {
            super(
                    containerId,
                    ((CraftPlayer) player).getHandle().gi(),
                    ContainerAccess.a(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(guiTitle);
        }

        @Override
        public boolean isExtendedApiSupported() {
            return true;
        }

        @Override
        public void setLeftItem(ItemStack item) {
            this.b(0).f(CraftItemStack.asNMSCopy(item));
        }

        @Override
        public void setMiddleItem(ItemStack item) {
            this.b(1).f(CraftItemStack.asNMSCopy(item));
        }

        @Override
        public void setRightItem(ItemStack item) {
            this.b(2).f(CraftItemStack.asNMSCopy(item));
        }

        @Override
        public void l() {
            // If the output is empty copy the left input into the output
            Slot output = this.b(2); // b -> getSlot
            if (!output.h()) { // h -> hasItem
                Slot input = this.b(0);
                if (input.h()) {
                    output.f(input.g().v()); // f -> set, g -> getItem, v -> copy
                }
            }

            this.y.a(0); // y -> cost, a -> set

            // Sync to the client
            this.b(); // b -> sendAllDataToRemote
            this.d(); // d -> broadcastChanges
        }

        @Override
        public void a(EntityHuman player) {}

        @Override
        protected void a(EntityHuman player, IInventory container) {}

        public int getContainerId() {
            return this.l;
        }

        @Override
        public String getRenameText() {
            return this.x;
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
        public void setRenameVisitor(Function<String, ItemStack> renameVisitor) {
            this.renameVisitor = renameVisitor;
        }

        @Override
        public Inventory getBukkitInventory() {
            // NOTE: We need to call Container#getBukkitView() instead of ContainerAnvil#getBukkitView()
            // because ContainerAnvil#getBukkitView() had an ABI breakage in the middle of the Minecraft 1.21
            // development cycle for Spigot. For more info, see: https://github.com/WesJD/AnvilGUI/issues/342
            return ((Container) this).getBukkitView().getTopInventory();
        }

        @Override
        public boolean a(String s) {
            if (renameVisitor == null) {
                return super.a(s);
            }

            // The visitor reads the rename text through StateSnapshot#getText, so it has to be current
            String previousText = this.x;
            this.x = s;
            ItemStack item = renameVisitor.apply(s);
            if (item == null) {
                this.x = previousText; // the vanilla method ignores a name equal to the current one
                return super.a(s);
            }

            this.b(2).f(CraftItemStack.asNMSCopy(item));
            l();
            return true;
        }
    }
}
