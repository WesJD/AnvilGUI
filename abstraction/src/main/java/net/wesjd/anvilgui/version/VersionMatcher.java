package net.wesjd.anvilgui.version;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

public class VersionMatcher {

    public static VersionWrapper match() {
        try {
            String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
            String className = new ClassGraph()
                    .enableClassInfo()
                    .whitelistPackages("net.wesjd.anvilgui.version")
                    .scan()
                    .getAllClasses()
                    .stream()
                    .filter(info -> info.getName().contains(serverVersion))
                    .map(ClassInfo::getName)
                    .findFirst().orElse(null);
            Validate.notNull(className, "Your server version isn't supported in AnvilGUI!");
            return (VersionWrapper) Class.forName(className).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

}
