/*
MIT License

Copyright (c) 2018-2020 PaperMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package net.wesjd.anvilgui.version;


import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

/**
 * Partially copied from <a href="https://github.com/PaperMC/PaperLib/blob/master/src/main/java/io/papermc/lib/environments/Environment.java#L56">here</a>.
 */
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
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid bukkit version: " + bukkitVersion);
        }
        final MatchResult matchResult = matcher.toMatchResult();
        final int major;
        try {
            major = Integer.parseInt(matchResult.group(1), 10);
        } catch (final Exception exception) {
            throw new IllegalArgumentException("Cannot parse major version: " + bukkitVersion, exception);
        }
        final int minor;
        try {
            minor = Integer.parseInt(matchResult.group(2), 10);
        } catch (final Exception exception) {
            throw new IllegalArgumentException("Cannot parse minor version: " + bukkitVersion, exception);
        }
        int patch = 0;
        if (matchResult.groupCount() >= 3) {
            try {
                patch = Integer.parseInt(matchResult.group(3), 10);
            } catch (final Exception exception) {
                throw new IllegalArgumentException("Cannot parse patch version: " + bukkitVersion, exception);
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
