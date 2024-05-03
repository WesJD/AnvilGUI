package net.wesjd.anvilgui.version;

/**
 * Matches the server's NMS version to its {@link VersionWrapper}
 *
 * @author Wesley Smith
 * @since 1.2.1
 */
public class VersionMatcher {
    private final VersionProvider versionProvider = new VersionProvider();

    /**
     * Matches the server version to it's {@link VersionWrapper}
     *
     * @return The {@link VersionWrapper} for this server
     * @throws IllegalStateException If the version wrapper failed to be instantiated or is unable to be found
     */
    public VersionWrapper match() {
        final String classPattern = "Wrapper%s_%s%s";
        final String packageName = getClass().getPackage().getName();

        final int major = this.versionProvider.major();
        final int minor = this.versionProvider.minor();
        final int patch = this.versionProvider.patch();

        // Start searching for suitable VersionWrapper
        // starting from the current patch version going downward
        VersionWrapper suitableWrapper = null;
        for (int i = patch; i >= 0; --i) {
            final String patchVersion = i == 0 ? "" : "_" + i;
            final String wrapperClassName = String.format(classPattern, major, minor, patchVersion);

            try {
                final Class<?> wrapperClass = Class.forName(packageName + "." + wrapperClassName);
                suitableWrapper =
                        (VersionWrapper) wrapperClass.getDeclaredConstructor().newInstance();
                break;
            } catch (final ClassNotFoundException exception) {
                // Ignore for this exception to look for previous patch version.
            } catch (final ReflectiveOperationException exception) {
                throw new IllegalStateException(
                        "Failed to instantiate version wrapper for server version v" + major + "." + minor + "."
                                + patch,
                        exception);
            }
        }

        if (suitableWrapper == null) {
            throw new IllegalStateException(
                    "No compatible version wrapper found for server version v" + major + "." + minor + "." + patch);
        } else {
            return suitableWrapper;
        }
    }
}
