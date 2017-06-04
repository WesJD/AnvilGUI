package net.wesjd.anvilgui.version;

import net.wesjd.anvilgui.version.impl.FallbackWrapper;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Version {
    private static final String VERSIONS_PACKET = "net.wesjd.anvilgui.version.impl";
    private static final String GITHUB_LINK = "https://github.com/upperlevel/AnvilGUI";

    /**
     * The used VersionWrapper
     */
    private static final VersionWrapper wrapper;
    private static final boolean isFallback;

   static {
       final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
       VersionWrapper w = null;
       try {
           Class<?> wrapperClazz = Version.class.getClassLoader().loadClass(VERSIONS_PACKET + "Wrapper" + version);
           w = (VersionWrapper) wrapperClazz.newInstance();
       } catch (ClassNotFoundException e) {
           Bukkit.getLogger().warning("[AnvilGUI] Cannot load hard-coded Wrapper, please ask the developers to implement \"" + version + "\" too (" + GITHUB_LINK + ")");
       } catch (Exception e) {
           Bukkit.getLogger().log(Level.SEVERE, "[AnvilGui] Unknown error while loading hard-coded Wrapper", e);
       }
       if(w == null) {
           Bukkit.getLogger().warning("[AnvilGui] Using fallback wrapper, it's NOT fully supported and should only be used for testing purposes");
           wrapper = new FallbackWrapper();
           isFallback = true;
       } else  {
           Bukkit.getLogger().info("[AnvilGui] Using hard-coded Wrapper for \"" + version + "\"");
           wrapper = w;
           isFallback = false;
       }
   }

   public static VersionWrapper getWrapper() {
       if(isFallback)
           //We want the user to know that this version is NOT fully supported (so we're gonna spam it!)
           Bukkit.getLogger().warning("[AnvilGui] Using fallback wrapper, it's NOT fully supported nor fast, this should ONLY be used for testing purposes");
       return wrapper;
   }

   public static boolean isFallback() {
       return isFallback;
   }
}
