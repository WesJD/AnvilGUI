package net.wesjd.version;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.ContainerAccess;
import net.minecraft.server.v1_14_R1.ContainerAnvil;
import net.wesjd.anvilgui.version.Wrapper1_14_R1;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Wrapper1_14_4 extends Wrapper1_14_R1 {

    @Override
    public Object newContainerAnvil(Player player) {
        return new Wrapper1_14_4.AnvilContainer(player);
    }

    private class AnvilContainer extends ContainerAnvil {

        public AnvilContainer(Player player) {
            super(Wrapper1_14_4.this.getNextContainerId(player), ((CraftPlayer) player).getHandle().inventory,
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

}
