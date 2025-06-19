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
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R5.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class Wrapper1_21_R5_Spigot implements VersionWrapper {

    private EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private int getRealNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }

    @Override
    public int getNextContainerId(Player player, AnvilContainerWrapper container) {
        return ((AnvilContainer) container).getContainerId();
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
        toNMS(player).p(); // p -> doCloseContainer
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId, Object inventoryTitle) {
        toNMS(player).g.b(new PacketPlayOutOpenWindow(containerId, Containers.i, (IChatBaseComponent) inventoryTitle));
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).g.b(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void sendPacketExperienceChange(Player player, int experienceLevel) {
        toNMS(player).g.b(new PacketPlayOutExperience(0f, 0, experienceLevel));
    }

    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).cn = toNMS(player).cm;
    }

    @Override
    public void setActiveContainer(Player player, AnvilContainerWrapper container) {
        toNMS(player).cn = (Container) container;
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
        return IChatBaseComponent.b(content);
    }

    @Override
    public Object jsonChatComponent(String json) {
        return IChatBaseComponent.a(json, IRegistryCustom.b);
    }

    private static class AnvilContainer extends ContainerAnvil implements AnvilContainerWrapper {
        public AnvilContainer(Player player, int containerId, IChatBaseComponent guiTitle) {
            super(
                    containerId,
                    ((CraftPlayer) player).getHandle().gs(),
                    ContainerAccess.a(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(guiTitle);
        }

        @Override
        public void l() {
            Slot output = this.b(2);
            if (!output.h()) {
                output.f(this.b(0).g().v());
            }
            this.y.a(0);
            this.b();
            this.d();
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
            Slot inputLeft = b(0);
            if (inputLeft.h()) {
                inputLeft.g().b(DataComponents.g, IChatBaseComponent.b(text));
            }
        }

        @Override
        public Inventory getBukkitInventory() {
            return ((Container) this).getBukkitView().getTopInventory();
        }
    }
}
