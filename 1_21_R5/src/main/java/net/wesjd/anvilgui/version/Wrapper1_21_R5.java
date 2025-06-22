package net.wesjd.anvilgui.version;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutExperience;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.*;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R5.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_21_R5.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class Wrapper1_21_R5 implements VersionWrapper {
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
                        "handleInventoryCloseEvent", EntityHuman.class, inventoryCloseEventReasonClass);
                handleInventoryCloseEventMethod.invoke(
                        null,
                        toNMS(player),
                        inventoryCloseEventReasonClass.getField("UNKNOWN").get(null));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        toNMS(player).q(); // q -> doCloseContainer
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId, Object inventoryTitle) {
        toNMS(player).g.b(new PacketPlayOutOpenWindow(containerId, Containers.i, (IChatBaseComponent)
                inventoryTitle)); // g -> connection, b -> send
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).g.b(new PacketPlayOutCloseWindow(containerId)); // g -> connection, b -> send
    }

    @Override
    public void sendPacketExperienceChange(Player player, int experienceLevel) {
        toNMS(player).g.b(new PacketPlayOutExperience(0f, 0, experienceLevel)); // g -> connection, b -> send
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).cn = toNMS(player).cm; // cn -> containerMenu, cm -> inventoryMenu
    }

    @Override
    public void setActiveContainer(Player player, AnvilContainerWrapper container) {
        toNMS(player).cn = (Container) container; // cn -> containerMenu
    }

    @Override
    public void setActiveContainerId(AnvilContainerWrapper container, int containerId) {}

    @Override
    public void addActiveContainerSlotListener(AnvilContainerWrapper container, Player player) {
        toNMS(player).a((Container) container); // a -> initMenu
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
        return CraftChatMessage.fromJSON(json);
    }

    private static class AnvilContainer extends ContainerAnvil implements AnvilContainerWrapper {
        public AnvilContainer(Player player, int containerId, IChatBaseComponent guiTitle) {
            super(
                    containerId,
                    ((CraftPlayer) player).getHandle().gs(), // gs -> getInventory
                    ContainerAccess.a( // a -> create
                            ((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(guiTitle);
        }

        @Override
        public void l() {
            // If the output is empty copy the left input into the output
            Slot output = this.b(2); // b -> getSlot
            if (!output.h()) { // h -> hasItem
                output.f(this.b(0).g().v()); // f -> set, g -> getItem, v -> copy
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
            return this.l; // l -> containerId
        }

        @Override
        public String getRenameText() {
            return this.x; // x -> itemName
        }

        @Override
        public void setRenameText(String text) {
            // If an item is present in the left input slot change its hover name to the literal text.
            Slot inputLeft = b(0);
            if (inputLeft.h()) {
                inputLeft
                        .g()
                        .b( // b -> set
                                DataComponents.g,
                                IChatBaseComponent.b(text)); // DataComponents.g -> DataComponents.CUSTOM_NAME
            }
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
