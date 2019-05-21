package net.wesjd.anvilgui.version;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class VersionMatcher {

    private final String serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
    private final List<Class<? extends VersionWrapper>> versions;
    private final int subString;

    @SafeVarargs
    public VersionMatcher(int subString, Class<? extends VersionWrapper>... versions) {
        this.subString = subString;
        this.versions = Arrays.asList(versions);
    }

    public VersionWrapper nms(Object... args) {
        return newInstance(match(), args);
    }

    private Class<? extends VersionWrapper> match() {
        return versions.stream()
            .filter(version -> version.getSimpleName().substring(subString).equals(serverVersion))
            .findFirst()
            .orElseThrow(() -> {
                throw new IllegalAccessError("Unsupported version for AnvilGUI");
            });
    }

    private VersionWrapper newInstance(Class<? extends VersionWrapper> clazz, Object... args) {
        Class[] argTypes = new Class[args.length];
        int ix = 0;
        for (Object arg : args) {
            argTypes[ix++] = arg != null ? arg.getClass() : null;
        }
        return newInstance(clazz, argTypes, args);
    }

    @SuppressWarnings("unchecked")
    private VersionWrapper newInstance(Class<? extends VersionWrapper> clazz, Class<?>[] argTypes, Object... args) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(argTypes);
            return (VersionWrapper) constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}