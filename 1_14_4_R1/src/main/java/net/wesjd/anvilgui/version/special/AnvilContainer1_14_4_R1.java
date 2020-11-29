package net.wesjd.anvilgui.version.special;

import net.minecraft.server.v1_14_R1.*;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AnvilContainer1_14_4_R1 extends ContainerAnvil {

    public AnvilContainer1_14_4_R1(Player player, int containerId, String guiTitle) {
        super(containerId, ((CraftPlayer) player).getHandle().inventory,
                ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
        this.checkReachable = false;
        setTitle(new ChatMessage(guiTitle));
    }

    @Override
    public void e() {
        super.e();
        this.levelCost.set(0);
    }

    @Override
    public void b(EntityHuman entityhuman) {
    }

    @Override
    protected void a(EntityHuman entityhuman, World world, IInventory iinventory) {
    }

    public int getContainerId() {
        return windowId;
    }

}
