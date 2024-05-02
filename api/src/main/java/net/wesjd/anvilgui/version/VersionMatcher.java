package net.wesjd.anvilgui.version;

/**
 * Matches the server's NMS version to its {@link VersionWrapper}
 *
 * @author Wesley Smith
 * @since 1.2.1
 */
public class VersionMatcher {
    private static final String PATTERN_AS_STRING = "Wrapper%s_%s%s";

    /**
     * Matches the server version to it's {@link VersionWrapper}
     *
     * @return The {@link VersionWrapper} for this server
     * @throws IllegalStateException If the version wrapper failed to be instantiated or is unable to be found
     */
    public VersionWrapper match() {
        final int major = PaperLib.minecraftMajorVersion();
        final int minor = PaperLib.minecraftMinorVersion();
        final int patch = PaperLib.minecraftPatchVersion();

        VersionWrapper suitableWrapper = null;

        // Start searching for suitable VersionWrapper
        // starting from the current patch version going downward
        for (int i = patch; i >= 0; --i) {
            final String patchVersion = i == 0 ? "" : "_" + i;
            final String wrapperClassName = String.format(VersionMatcher.PATTERN_AS_STRING, major, minor, patchVersion);

            try {
                final Class<?> wrapperClass =
                        Class.forName(getClass().getPackage().getName() + "." + wrapperClassName);
                suitableWrapper =
                        (VersionWrapper) wrapperClass.getDeclaredConstructor().newInstance();
                break;
            } catch (final ClassNotFoundException exception) {
                // Ignore for this exception to look for previous patch version.
            } catch (final ReflectiveOperationException exception) {
                throw new IllegalStateException(
                        "Failed to instantiate version wrapper for server version " + major + "." + minor + "." + i,
                        exception);
            }
        }

        if (suitableWrapper == null) {
            throw new IllegalStateException(
                    "No compatible version wrapper found for server version " + major + "." + minor);
        } else {
            return suitableWrapper;
        }
    }
}
