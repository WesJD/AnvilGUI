package net.wesjd.anvilgui.version;

import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

abstract class EnvironmentBase implements Environment {
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "(?i)\\(MC: (\\d)\\.(\\d+)\\.?(\\d+?)?(?: (Pre-Release|Release Candidate) )?(\\d)?\\)"
    );

    private final int minecraftMajorVersion;
    private final int minecraftMinorVersion;
    private final int minecraftPatchVersion;
    private final int minecraftPreReleaseVersion;
    private final int minecraftReleaseCandidateVersion;

    EnvironmentBase() {
        this(Bukkit.getVersion());
    }

    EnvironmentBase(final String bukkitVersion) {
        final Matcher matcher = EnvironmentBase.VERSION_PATTERN.matcher(bukkitVersion);
        int major = 1;
        int minor = 0;
        int patch = 0;
        int preRelease = -1;
        int releaseCandidate = -1;
        if (matcher.find()) {
            final MatchResult matchResult = matcher.toMatchResult();
            try {
                major = Integer.parseInt(matchResult.group(1), 10);
            } catch (final Exception ignored) {}
            try {
                minor = Integer.parseInt(matchResult.group(2), 10);
            } catch (final Exception ignored) {}
            if (matchResult.groupCount() >= 3) {
                try {
                    patch = Integer.parseInt(matchResult.group(3), 10);
                } catch (final Exception ignored) {}
            }
            if (matchResult.groupCount() >= 5) {
                try {
                    final int ver = Integer.parseInt(matcher.group(5));
                    if (matcher.group(4).toLowerCase(Locale.ENGLISH).contains("pre")) {
                        preRelease = ver;
                    } else {
                        releaseCandidate = ver;
                    }
                } catch (final Exception ignored) {}
            }
        }
        this.minecraftMajorVersion = major;
        this.minecraftMinorVersion = minor;
        this.minecraftPatchVersion = patch;
        this.minecraftPreReleaseVersion = preRelease;
        this.minecraftReleaseCandidateVersion = releaseCandidate;
    }

    @Override
    public int minecraftMajorVersion() {
        return this.minecraftMajorVersion;
    }

    @Override
    public int minecraftMinorVersion() {
        return this.minecraftMinorVersion;
    }

    @Override
    public int minecraftPatchVersion() {
        return this.minecraftPatchVersion;
    }

    @Override
    public int minecraftPreReleaseVersion() {
        return this.minecraftPreReleaseVersion;
    }

    @Override
    public int minecraftReleaseCandidateVersion() {
        return this.minecraftReleaseCandidateVersion;
    }

    @Override
    public boolean isVersion(final int minor) {
        return this.isVersion(minor, 0);
    }

    @Override
    public boolean isVersion(final int minor, final int patch) {
        return (
            this.minecraftMinorVersion > minor ||
            (this.minecraftMinorVersion >= minor && this.minecraftPatchVersion >= patch)
        );
    }

    @Override
    public boolean isSpigot() {
        return false;
    }

    @Override
    public boolean isPaper() {
        return false;
    }
}
