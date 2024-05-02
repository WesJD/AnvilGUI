package net.wesjd.anvilgui.version;

interface Environment {
    int minecraftMajorVersion();

    int minecraftMinorVersion();

    int minecraftPatchVersion();

    int minecraftPreReleaseVersion();

    int minecraftReleaseCandidateVersion();

    boolean isVersion(int minor);

    boolean isVersion(int minor, int patch);

    boolean isSpigot();

    boolean isPaper();

    String name();
}
