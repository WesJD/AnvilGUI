package net.wesjd.anvilgui.version;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;

/**
 * Matches the server's NMS version to its {@link VersionWrapper}
 *
 * @author Wesley Smith
 * @since 1.2.1
 */
public class VersionMatcher {
    /** Maps a Minecraft version string to the corresponding revision string */
    private static final Map<String, String> VERSION_TO_REVISION = new HashMap<String, String>() {
        {
            this.put("1.20", "1_20_R1");
            this.put("1.20.1", "1_20_R1");
            this.put("1.20.2", "1_20_R2");
            this.put("1.20.3", "1_20_R3");
            this.put("1.20.4", "1_20_R3");
            this.put("1.20.5", "1_20_R4");
            this.put("1.20.6", "1_20_R4");
            this.put("1.21", "1_21_R1");
            this.put("1.21.1", "1_21_R1");
            this.put("1.21.2", "1_21_R2");
            this.put("1.21.3", "1_21_R2");
            this.put("1.21.4", "1_21_R3");
        }
    };
    /* This needs to be updated to reflect the newest available version wrapper */
    private static final String FALLBACK_REVISION = "1_21_R3";

    // Paper 1.21.4+ removes support for legacy color codes when opening the inventory using packets
    public static final boolean requiresMini = needsMiniMessage();

    /**
     * Matches the server version to it's {@link VersionWrapper}
     *
     * @return The {@link VersionWrapper} for this server
     * @throws IllegalStateException If the version wrapper failed to be instantiated or is unable to be found
     */
    public VersionWrapper match() {
        String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();

        String rVersion;
        if (!craftBukkitPackage.contains(".v")) { // cb package not relocated (i.e. paper 1.20.5+)
            final String version = Bukkit.getBukkitVersion().split("-")[0];
            rVersion = VERSION_TO_REVISION.getOrDefault(version, FALLBACK_REVISION);
        } else {
            rVersion = craftBukkitPackage.split("\\.")[3].substring(1);
        }

        try {
            return (VersionWrapper) Class.forName(getClass().getPackage().getName() + ".Wrapper" + rVersion)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("AnvilGUI does not support server version \"" + rVersion + "\"", exception);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to instantiate version wrapper for version " + rVersion, exception);
        }
    }

    /**
     * Checks if the server runs paper 1.21.4+ cause it removes support for legacy color codes in the inventory title
     * so we parse any legacy color codes to MiniMessage format
     *
     * @return true if this server doesn't support legacy color codes, false otherwise
     */
    private static boolean needsMiniMessage() {
        String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
        if (!craftBukkitPackage.contains(".v")) { // cb package not relocated (i.e. paper 1.20.5+)
            try {
                int version = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].replace(".", ""));

                if (version >= 1214) // Check for server version above or equal to 1.21.4
                    return true;
            } catch (NumberFormatException e) {} // Invalid version number
        }

        return false;
    }
}
