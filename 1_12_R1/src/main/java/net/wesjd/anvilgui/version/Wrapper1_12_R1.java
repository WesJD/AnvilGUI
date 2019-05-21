package net.wesjd.anvilgui.version;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.inventory.Inventory;

public class Wrapper1_12_R1 implements VersionWrapper {

    private final EntityPlayer nms;
    private final AnvilContainer container;
    private int containerId = 0;

    public Wrapper1_12_R1(CraftPlayer craftPlayer) {
        nms = craftPlayer.getHandle();
        container = new AnvilContainer(nms);
    }

    @Override
    public Inventory create() {
        CraftEventFactory.handleInventoryCloseEvent(nms);
        nms.activeContainer = nms.defaultContainer;

        return container.getBukkitView().getTopInventory();
    }

    @Override
    public void open() {
        containerId = nms.nextContainerCounter();
        packet(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name")));
        nms.activeContainer = container;
        container.windowId = containerId;
        container.addSlotListener(nms);
    }

    @Override
    public void close() {
        CraftEventFactory.handleInventoryCloseEvent(nms);
        nms.activeContainer = nms.defaultContainer;
        packet(new PacketPlayOutCloseWindow(containerId));
    }

    private void packet(Packet packet) {
        nms.playerConnection.sendPacket(packet);
    }

    private class AnvilContainer extends ContainerAnvil {

        AnvilContainer(EntityPlayer entityPlayer) {
            super(entityPlayer.inventory, entityPlayer.world, BlockPosition.ZERO, entityPlayer);

            checkReachable = false;
        }

    }

}