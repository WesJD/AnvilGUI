package net.wesjd.anvilgui.version;

final class Internal {

    static Environment initialize() {
        if (Internal.hasClass("com.destroystokyo.paper.PaperConfig")
                || Internal.hasClass("io.papermc.paper.configuration.Configuration")) {
            return new EnvironmentPaper();
        } else if (Internal.hasClass("org.spigotmc.SpigotConfig")) {
            return new EnvironmentSpigot();
        } else {
            return new EnvironmentCraftBukkit();
        }
    }

    private static boolean hasClass(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    private Internal() throws IllegalAccessException {
        throw new IllegalAccessException("Utility class!");
    }
}
