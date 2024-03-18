package net.wesjd.anvilgui.version.special;


import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Slot;
import net.wesjd.anvilgui.version.VersionWrapper;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AnvilContainer1_17_1_R1 extends ContainerAnvil implements VersionWrapper.AnvilContainerWrapper {
    public AnvilContainer1_17_1_R1(Player player, int containerId, IChatBaseComponent guiTitle) {
        super(
                containerId,
                ((CraftPlayer) player).getHandle().getInventory(),
                ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
        this.checkReachable = false;
        setTitle(guiTitle);
    }

    @Override
    public void l() {
        // If the output is empty copy the left input into the output
        Slot output = this.getSlot(2);
        if (!output.hasItem()) {
            Slot input = this.getSlot(0);

            if (input.hasItem()) {
                output.set(input.getItem().cloneItemStack());
            }
        }

        this.w.set(0);

        // Sync to the client
        this.updateInventory();
        this.d();
    }

    @Override
    public void b(EntityHuman player) {}

    @Override
    protected void a(EntityHuman player, IInventory container) {}

    public int getContainerId() {
        return this.j;
    }

    @Override
    public String getRenameText() {
        return this.v;
    }

    @Override
    public void setRenameText(String text) {
        // If an item is present in the left input slot change its hover name to the literal text.
        Slot inputLeft = getSlot(0);
        if (inputLeft.hasItem()) {
            inputLeft.getItem().a(new ChatComponentText(text));
        }
    }

    @Override
    public Inventory getBukkitInventory() {
        return getBukkitView().getTopInventory();
    }
}
