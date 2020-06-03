package net.wesjd.anvilgui.version.special;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.ContainerAccess;
import net.minecraft.server.v1_14_R1.ContainerAnvil;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftHumanEntity;
import org.bukkit.entity.Player;

public final class AnvilContainer1_14_4_R1 extends ContainerAnvil {

    public AnvilContainer1_14_4_R1(final Player player, final int containerId, final String guiTitle) {
        super(containerId, ((CraftHumanEntity) player).getHandle().inventory,
            ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
        this.checkReachable = false;
        this.setTitle(new ChatMessage(guiTitle));
    }

    @Override
    public void e() {
        super.e();
        this.levelCost.set(0);
    }

    public int getContainerId() {
        return this.windowId;
    }

}
