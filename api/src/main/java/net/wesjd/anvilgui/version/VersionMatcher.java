package net.wesjd.anvilgui.version;


import org.bukkit.Bukkit;

/**
 * Matches the server's NMS version to its {@link VersionWrapper}
 *
 * @author Wesley Smith
 * @since 1.2.1
 */
public final class VersionMatcher {

    private VersionMatcher() {}

    /**
     * Matches the server version to it's {@link VersionWrapper}
     *
     * @return The {@link VersionWrapper} for this server
     * @throws IllegalStateException If the version wrapper failed to be instantiated or is unable to be found
     */
    public static VersionWrapper match() {
        final String serverVersion = Bukkit.getServer()
                .getClass()
                .getPackage()
                .getName()
                .split("\\.")[3]
                .substring(1);
        try {
            return (VersionWrapper)
                    Class.forName(VersionMatcher.class.getPackage().getName() + ".Wrapper" + serverVersion)
                            .getDeclaredConstructor()
                            .newInstance();
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(
                    "AnvilGUI does not support server version \"" + serverVersion + "\"", exception);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Failed to instantiate version wrapper for version " + serverVersion, exception);
        }
    }
}
