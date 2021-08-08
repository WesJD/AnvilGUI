package net.wesjd.anvilgui.version;

import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;

/**
 * Matches the server's NMS version to its {@link VersionWrapper}
 *
 * @author Wesley Smith
 * @since 1.2.1
 */
public class VersionMatcher {

    /**
     * The server's version
     */
    private final String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);

    /**
     * Matches the server version to it's {@link VersionWrapper}
     *
     * @return The {@link VersionWrapper} for this server
     * @throws RuntimeException If AnvilGUI doesn't support this server version
     */
    public VersionWrapper match() {
        try {
            return (VersionWrapper) Class.forName(this.getClass().getPackage().getName() + ".Wrapper" + serverVersion).newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Your server version isn't supported in AnvilGUI!");
        }
    }

}
