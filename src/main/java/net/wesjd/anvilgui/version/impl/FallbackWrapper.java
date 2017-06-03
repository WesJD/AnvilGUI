package net.wesjd.anvilgui.version.impl;

import net.wesjd.anvilgui.version.VersionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FallbackWrapper implements VersionWrapper {
    private static final String version;

    private static final Class<?> playerClass;
    private static final Method playerGetHandle;

    private static final Method playerNextContainerCounter;

    private static final Class<?> craftEventFactory;
    private static final Method eventFactoryHandleInventoryCloseEvent;

    private static final Field playerPlayerConnection;
    private static final Class<?> playerConnectionClass;
    private static final Class<?> packetClass;
    private static final Method playerConnectionSendPacket;
    private static final Constructor<?> packetPlayOutOpenWindowConstructor;
    private static final Class<?> chatMessageClass;
    private static final Constructor<?> chatMessageConstructor;
    private static final String blockAnvilA;

    private static final Constructor<?> packetPlayOutCloseWindowConstructor;

    private static final Field playerActiveContainer, playerDefaultContainer;

    private static final Field containerWindowId;

    private static final Method containerAddSlotListener;

    private static final Method containerGetBukkitView;

    private static final Field playerInventory, playerWorld;
    private static final Object blockPositionZero;
    private static final Constructor<?> containerAnvilConstructor;
    private static final Field containerCheckReachable;

    static {
        version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            playerClass = getNMSClass("EntityPlayer");

            playerGetHandle = getCraftClass("entity.CraftPlayer").getMethod("getHandle");

            playerNextContainerCounter = playerClass.getMethod("nextContainerCounter");

            craftEventFactory = getCraftClass("event.CraftEventFactory");
            eventFactoryHandleInventoryCloseEvent = craftEventFactory.getMethod("handleInventoryCloseEvent", getNMSClass("EntityHuman"));

            playerPlayerConnection = playerClass.getField("playerConnection");
            playerConnectionClass = getNMSClass("PlayerConnection");
            packetClass = getNMSClass("Packet");
            playerConnectionSendPacket = playerConnectionClass.getMethod("sendPacket", packetClass);
            final Class<?> packetPlayOutOpenWindowClass = getNMSClass("PacketPlayOutOpenWindow");
            packetPlayOutOpenWindowConstructor = packetPlayOutOpenWindowClass.getConstructor(Integer.TYPE, String.class, getNMSClass("IChatBaseComponent"));
            chatMessageClass = getNMSClass("ChatMessage");
            chatMessageConstructor = chatMessageClass.getConstructor(String.class, Object[].class);
            final Class<?> blockClass = getNMSClass("Block");
            final Method blockA = blockClass.getMethod("a");
            final Object blocksAnvil = getNMSClass("Blocks").getField("ANVIL").get(null);
            blockAnvilA = (String) blockA.invoke(blocksAnvil);

            final Class<?> packetPlayOutCloseWindowClass = getNMSClass("PacketPlayOutCloseWindow");
            packetPlayOutCloseWindowConstructor = packetPlayOutCloseWindowClass.getConstructor(Integer.TYPE);

            playerActiveContainer = playerClass.getField("activeContainer");
            playerDefaultContainer = playerClass.getField("defaultContainer");

            final Class<?> containerClass = getNMSClass("Container");
            containerWindowId = containerClass.getField("windowId");

            containerAddSlotListener = containerClass.getMethod("addSlotListener", getNMSClass("ICrafting"));

            containerGetBukkitView = containerClass.getMethod("getBukkitView");

            playerInventory = playerClass.getField("inventory");
            playerWorld = playerClass.getField("world");
            final Class<?> blockPositionClass = getNMSClass("BlockPosition");
            blockPositionZero = blockPositionClass.getField("ZERO").get(null);

            containerAnvilConstructor = getNMSClass("ContainerAnvil").getConstructor(
                    getNMSClass("PlayerInventory"),
                    getNMSClass("World"),
                    blockPositionClass,
                    getNMSClass("EntityHuman")
            );
            containerCheckReachable = getNMSClass("Container").getField("checkReachable");
        } catch (Exception e) {
            throw new UnsupportedVersionException(version, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNextContainerId(Player player) {
        //return nms(player).nextContainerCounter();
        try {
            return (int) playerNextContainerCounter.invoke(toNMS(player));//nms(player).nextContainerCounter();
        } catch (Exception e) {
            handleException(e);
            return -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleInventoryCloseEvent(Player player) {
        //CraftEventFactory.handleInventoryCloseEvent(nms(player));
        try {
            eventFactoryHandleInventoryCloseEvent.invoke(craftEventFactory, toNMS(player));
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketOpenWindow(Player player, int containerId) {
        //toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name")));
        try {
            playerConnectionSendPacket.invoke(//.sendPacket(
                    playerPlayerConnection.get(toNMS(player)),//nms(player).playerConnection
                    packetPlayOutOpenWindowConstructor.newInstance(//new PlayOutOpenWindow(
                            containerId,
                            "minecraft:anvil",
                            chatMessageConstructor.newInstance(blockAnvilA + ".name", new Object[]{})//Blocks.ANVIL.a() + ".name"
                    )//)
            );//);
        } catch (Exception e) {
           handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        // nms(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
        try {
            playerConnectionSendPacket.invoke(//.sendPacket(
                    playerPlayerConnection.get(toNMS(player)),//nms(player).playerConnection
                    packetPlayOutCloseWindowConstructor.newInstance(containerId)//new PacketPlayOutCloseWindow(containerId)
            );//);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerDefault(Player player) {
        try {
            Object nmsPlayer = toNMS(player);
            playerActiveContainer.set(nmsPlayer, playerDefaultContainer.get(nmsPlayer));
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainer(Player player, Object container) {
        try {
            playerActiveContainer.set(toNMS(player), container);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerId(Object container, int containerId) {
        try {
            containerWindowId.set(container, containerId);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        try {
            containerAddSlotListener.invoke(container, toNMS(player));
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Inventory toBukkitInventory(Object container) {
        try {
            return ((InventoryView)containerGetBukkitView.invoke(container)).getTopInventory();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object newContainerAnvil(Player player) {
        //TODO try to make containerAnvil.a() return always true
        try {
            final Object nms = toNMS(player);
            Object obj = containerAnvilConstructor.newInstance(
                    playerInventory.get(nms),
                    playerWorld.get(nms),
                    blockPositionZero,
                    nms
            );
            containerCheckReachable.set(obj, false);
            return obj;
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     * Turns a {@link Player} into an NMS one
     * @param player The player to be converted
     * @return the NMS EntityPlayer
     */
    private Object toNMS(Player player) throws InvocationTargetException, IllegalAccessException {
        return playerGetHandle.invoke(player);
    }

    protected void handleException(Exception e) {
        throw new UnsupportedVersionException(version, e);
    }


    private static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getCraftClass(String path) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + path);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class UnsupportedVersionException extends RuntimeException {
        private final String version;

        public UnsupportedVersionException(String version, Exception e) {
            super("Unsupported version \"" + version + "\", report this to the developers", e);
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }
}
