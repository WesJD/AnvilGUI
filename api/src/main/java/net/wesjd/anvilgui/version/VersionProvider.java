package net.wesjd.anvilgui.version;


import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

final class VersionProvider {
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("(?i)\\(MC: (\\d)\\.(\\d+)\\.?(\\d+?)?(?: (Pre-Release|Release Candidate) )?(\\d)?\\)");

    private final int major;
    private final int minor;
    private final int patch;

    VersionProvider() {
        this(Bukkit.getVersion());
    }

    VersionProvider(final String bukkitVersion) {
        final Matcher matcher = VersionProvider.VERSION_PATTERN.matcher(bukkitVersion);
        int major = 1;
        int minor = 0;
        int patch = 0;
        if (matcher.find()) {
            final MatchResult matchResult = matcher.toMatchResult();
            try {
                major = Integer.parseInt(matchResult.group(1), 10);
            } catch (final Exception ignored) {
            }
            try {
                minor = Integer.parseInt(matchResult.group(2), 10);
            } catch (final Exception ignored) {
            }
            if (matchResult.groupCount() >= 3) {
                try {
                    patch = Integer.parseInt(matchResult.group(3), 10);
                } catch (final Exception ignored) {
                }
            }
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    int major() {
        return this.major;
    }

    int minor() {
        return this.minor;
    }

    int patch() {
        return this.patch;
    }
}
