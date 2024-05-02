package net.wesjd.anvilgui.version;

class EnvironmentSpigot extends EnvironmentCraftBukkit {

    @Override
    public String name() {
        return "Spigot";
    }

    @Override
    public boolean isSpigot() {
        return true;
    }
}
