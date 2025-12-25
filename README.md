> [!NOTE]
> The Maven repository for AnvilGUI has moved as of version `1.10.11-SNAPSHOT`.
>
> Replace:
> ```xml
> <repository>
>   <id>codemc-snapshots</id>
>   <url>https://repo.codemc.io/repository/maven-snapshots/</url>
> </repository>
> ```
>
> With:
> ```xml
> <repository>
>   <id>mvn-wesjd-net</id>
>   <url>https://mvn.wesjd.net/</url>
> </repository>
> ```

# AnvilGUI

**AnvilGUI** is a Minecraft Java Edition library that supports capturing user input from an anvil inventory without touching version-specific code.

```java
new AnvilGUI.Builder()
    .onClose(stateSnapshot -> {
        stateSnapshot.getPlayer().sendMessage("You closed the inventory.");
    })
    .onClick((slot, stateSnapshot) -> {
        if(slot != AnvilGUI.Slot.OUTPUT) {
            return Collections.emptyList();
        }

        if(stateSnapshot.getText().equalsIgnoreCase("you")) {
            stateSnapshot.getPlayer().sendMessage("You have magical powers!");
            return Arrays.asList(AnvilGUI.ResponseAction.close());
        } else {
            return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Try again"));
        }
    })
    .preventClose()
    .text("What is the meaning of life?")
    .title("Enter your answer")
    .plugin(plugin)
    .open(player);
```

[More usage information is available in USAGE.md](USAGE)

## Installation

Add the repository and dependency to your POM:

```xml
<repository>
    <id>mvn-wesjd-net</id>
    <url>https://mvn.wesjd.net/</url>
</repository>

<dependency>
  <groupId>net.wesjd</groupId>
  <artifactId>anvilgui</artifactId>
  <version>1.10.11-SNAPSHOT</version>
  <scope>compile</scope> <!-- Include the library in your JAR -->
</dependency>
```

Since AnvilGUI is a library, it must be shaded into your plugin. Follow the example shade config below to ensure that:
1. You avoid classpath conflicts with other plugins
2. You do not break the library when minimizing your JAR

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>${shade.version}</version> <!-- The version must be at least 3.5.0 -->
      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
          <configuration>
           <!--
             If multiple plugins use AnvilGUI but use different AnvilGUI versions,
             one of the plugins functionality may break. To avoid these classpath
             conflicts, relocate AnvilGUI to your plugin's namespace.
           -->              
            <relocations>
              <relocation>
                <pattern>net.wesjd.anvilgui</pattern>
                <shadedPattern>[YOUR_PLUGIN_PACKAGE].anvilgui</shadedPattern>
              </relocation>
            </relocation>
              
            <!-- 
              AnvilGUI works by loading the appropriate anvil implementation at
              runtime via reflection. When minimize JAR is enabled, the shade 
              plugin will see no usages of the anvil implementation classes and 
              omit them from the shaded artifacts. To solve this, add a filter 
              that always includes the entire library.
            -->
            <filters>
              <filter>
                <artifact>*:*</artifact>
                <excludeDefaults>false</excludeDefaults>
                <includes>
                  <include>net/wesjd/anvilgui/**</include>
                </includes>
              </filter>
            </filters>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

## Requirements

- Java 8+
- Bukkit / Spigot / Paper for Minecraft 1.7+
- Spigot mappings


### Ensuring you are using Spigot mappings

AnvilGUI is compiled against Spigot mappings. Mojang mappings are not supported at runtime. You can still develop using Mojang mappings when using Paperweight. See below.

### Bukkit Plugin

Your plugin is a [Bukkit plugin](https://docs.papermc.io/paper/dev/plugin-yml) if it contains a `plugin.yml` and **does not** contain a `paper-plugin.yml`.

Bukkit plugins use Spigot mappings by default.

Note that you can still use the Paper API even if your plugin is a Bukkit plugin. The Paper plugin system provides some additional features on top of the Paper API, but is not needed in order to use the Paper API.

### Paper Plugin

Your plugin is a [Paper plugin](https://docs.papermc.io/paper/dev/getting-started/paper-plugins) if it contains a `paper-plugin.yml` (even if `plugin.yml` is also present).

Paper plugins **do not** use Spigot mappings by default. You must [explicitly enable them via your manifest](https://docs.papermc.io/paper/dev/project-setup#spigot-mappings).

### Paperweight Userdev

If you are using the [Paperweight Userdev](https://docs.papermc.io/paper/dev/userdev) toolchain, you must enable Spigot mappings via [the reobfArtifactConfiguration option](https://docs.papermc.io/paper/dev/userdev#compiling-to-spigot-mappings).
