package net.wesjd.version.special;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.ContainerAccess;
import net.minecraft.server.v1_14_R1.ContainerAnvil;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AnvilContainer1_14_4_R1 extends ContainerAnvil {

    public AnvilContainer1_14_4_R1(Player player, int containerId) {
        super(containerId, ((CraftPlayer) player).getHandle().inventory,
                ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
        this.checkReachable = false;
        setTitle(new ChatMessage("Repair & Name"));
    }

    @Override
    public void e() {
        super.e();
        this.levelCost.set(0);
    }

}
