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
        }
    };

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
            // separating major and minor versions, example: 1.20.4-R0.1-SNAPSHOT -> major = 20, minor = 4
            final String version = Bukkit.getBukkitVersion().split("-")[0];
            if (!VERSION_TO_REVISION.containsKey(version)) {
                throw new IllegalStateException(
                        "AnvilGUI does not support bukkit server version \"" + Bukkit.getBukkitVersion() + "\"");
            }
            rVersion = VERSION_TO_REVISION.get(version);
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
}
