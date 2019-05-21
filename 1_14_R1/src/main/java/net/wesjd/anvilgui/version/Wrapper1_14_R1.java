package net.wesjd.anvilgui.version;

import net.minecraft.server.v1_14_R1.*;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Field;

public class Wrapper1_14_R1 implements VersionWrapper {

    private final EntityPlayer nms;
    private final AnvilContainer container;
    private int containerId = 0;

    public Wrapper1_14_R1(CraftPlayer craftPlayer) {
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
        packet(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, new ChatMessage("Repair & Name")));
        nms.activeContainer = container;
        setWindowId();
        container.addSlotListener(nms);
    }

    @Override
    public void close() {
        CraftEventFactory.handleInventoryCloseEvent(nms);
        nms.activeContainer = nms.defaultContainer;
        packet(new PacketPlayOutCloseWindow(containerId));
    }

    private void setWindowId() {
        Field field = null;

        try {
            field = Container.class.getField("windowId");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        FieldUtils.removeFinalModifier(field);

        try {
            FieldUtils.writeField(field, container, containerId);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void packet(Packet packet) {
        nms.playerConnection.sendPacket(packet);
    }

    private class AnvilContainer extends ContainerAnvil {

        AnvilContainer(EntityPlayer entityPlayer) {
            super(entityPlayer.nextContainerCounter(), entityPlayer.inventory, ContainerAccess.at(
                entityPlayer.world, BlockPosition.ZERO
            ));

            checkReachable = false;
        }

    }

}