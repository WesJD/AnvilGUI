package net.wesjd.anvilgui.version;

/**
 * Utility methods that assist plugin developers accessing Paper features.
 * Bridges backwards compatability with Spigot and CraftBukkit so your plugin
 * will still work on those platforms, and fall back to less performant methods.
 */
final class PaperLib {
    private static final Environment environment = Internal.initialize();

    /**
     * Detects if the current MC version is at least the following version.
     * <p>
     * Assumes 0 patch version.
     *
     * @param minor Min Minor Version
     * @return Meets the version requested
     */
    static boolean isVersion(final int minor) {
        return PaperLib.environment.isVersion(minor);
    }

    /**
     * Detects if the current MC version is at least the following version.
     *
     * @param minor Min Minor Version
     * @param patch Min Patch Version
     *
     * @return Meets the version requested
     */
    static boolean isVersion(final int minor, final int patch) {
        return PaperLib.environment.isVersion(minor, patch);
    }

    /**
     * Gets the current Minecraft Major version. IE: 1.16.5 returns 1
     *
     * @return The Major Version
     */
    static int minecraftMajorVersion() {
        return PaperLib.environment.minecraftMajorVersion();
    }

    /**
     * Gets the current Minecraft Minor version. IE: 1.13.1 returns 13
     *
     * @return The Minor Version
     */
    static int minecraftMinorVersion() {
        return PaperLib.environment.minecraftMinorVersion();
    }

    /**
     * Gets the current Minecraft Patch version. IE: 1.13.1 returns 1
     *
     * @return The Patch Version
     */
    static int minecraftPatchVersion() {
        return PaperLib.environment.minecraftPatchVersion();
    }

    /**
     * Gets the current Minecraft Pre-Release version if applicable, otherwise -1. IE: "1.14.3 Pre-Release 4" returns 4
     *
     * @return The Pre-Release Version if applicable, otherwise -1
     */
    static int minecraftPreReleaseVersion() {
        return PaperLib.environment.minecraftPreReleaseVersion();
    }

    /**
     * Gets the current Minecraft Release Candidate version if applicable, otherwise -1. IE: "1.18 Release Candidate 3" returns 3
     *
     * @return The Release Candidate Version if applicable, otherwise -1
     */
    static int minecraftReleaseCandidateVersion() {
        return PaperLib.environment.minecraftReleaseCandidateVersion();
    }

    /**
     * Check if the server has access to the Spigot API
     *
     * @return True for Spigot <em>and</em> Paper environments
     */
    static boolean isSpigot() {
        return PaperLib.environment.isSpigot();
    }

    /**
     * Check if the server has access to the Paper API
     *
     * @return True for Paper environments
     */
    static boolean isPaper() {
        return PaperLib.environment.isPaper();
    }

    private PaperLib() throws IllegalAccessException {
        throw new IllegalAccessException("Utility class!");
    }
}
