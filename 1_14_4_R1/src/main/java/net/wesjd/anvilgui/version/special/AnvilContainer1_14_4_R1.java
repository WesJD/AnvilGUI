package net.wesjd.anvilgui.version.special;


import net.minecraft.server.v1_14_R1.*;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AnvilContainer1_14_4_R1 extends ContainerAnvil {

    public AnvilContainer1_14_4_R1(Player player, int containerId, IChatBaseComponent guiTitle) {
        super(
                containerId,
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
            output.set(this.getSlot(0).getItem().cloneItemStack());
        }

        this.levelCost.set(0);

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
}
