package net.wesjd.anvilgui.version.special;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AnvilContainer1_17_1_R1 extends ContainerAnvil {
    public AnvilContainer1_17_1_R1(Player player, int containerId ,String guiTitle) {
        super(containerId, ((CraftPlayer)player).getHandle().getInventory(),
                ContainerAccess.at(((CraftWorld)player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
        this.checkReachable = false;
        setTitle(new ChatMessage(guiTitle));
    }

    @Override
    public void l() {
        super.l();
        this.w.set(0);
    }

    @Override
    public void b(EntityHuman player) {}

    @Override
    protected void a(EntityHuman player, IInventory container) {}

    public int getContainerId() {
        return this.j;
    }
}
