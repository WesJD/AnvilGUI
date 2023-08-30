package net.wesjd.anvilgui.version.special;


import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Slot;
import net.wesjd.anvilgui.version.VersionWrapper;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AnvilContainer1_19_1_R1 extends ContainerAnvil implements VersionWrapper.AnvilContainerWrapper {
    public AnvilContainer1_19_1_R1(Player player, int containerId, IChatBaseComponent guiTitle) {
        super(
                containerId,
                ((CraftPlayer) player).getHandle().fA(),
                ContainerAccess.a(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
        this.checkReachable = false;
        setTitle(guiTitle);
    }

    @Override
    public void l() {
        // If the output is empty copy the left input into the output
        Slot output = this.b(2);
        if (!output.f()) {
            output.e(this.b(0).e().o());
        }

        this.w.a(0);
        // Sync to the client
        this.b();
        this.d();
    }

    @Override
    public void b(EntityHuman player) {}

    @Override
    protected void a(EntityHuman player, IInventory container) {}

    @Override
    public String getRenameText() {
        return this.v;
    }

    @Override
    public void setRenameText(String text) {
        // If an item is present in the left input slot change its hover name to the literal text.
        Slot inputLeft = b(0);
        if (inputLeft.f()) {
            inputLeft.e().a(IChatBaseComponent.b(text));
        }
    }

    public int getContainerId() {
        return this.j;
    }

    @Override
    public Inventory getBukkitInventory() {
        return getBukkitView().getTopInventory();
    }
}
