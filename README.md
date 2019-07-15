# AnvilGUI
Easily use anvil guis to get a user's input.

This project was made since their is no easy way to do this with the Spigot / Bukkit APIs. It requires interaction
with NMS and that is a pain in a non-private plugin where users will have lots of different versions of the server
running.

## Requirements
Java 8 and Bukkit / Spigot. Most server versions in the [Spigot Repository](https://hub.spigotmc.org/nexus/) are supported.

### My version isn't supported
If you are a developer, submit a pull request adding a wrapper class for your version. Otherwise, please create an issue
on the issues tab. 

## How to use
### As a dependency
```xml
<dependency>
    <groupId>com.github.WesJD</groupId>
    <artifactId>AnvilGUI</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### In your plugin
```java
new AnvilGUI.Builder()
    .onClose(player -> {                   //called when the inventory is closing
        player.sendMessage("You closed the inventory.");
    })
    .onComplete((player, text) -> {        //called when the inventory output slot is clicked
        if(text.equalsIgnoreCase("you")) {
            player.sendMessage("You have magical powers!");
            return AnvilGUI.Response.close();
        } else {
            return AnvilGUI.Response.text("Incorrect.");
        }
    })
    .preventClose()                        //prevents the inventory from being closed
    .text("What is the meaning of life?")  //sets the text the GUI should start with
    .plugin(myPluginInstance)              //set the plugin instance
    .open(myPlayer);                       //opens the GUI for the player provided
```

### [Javadocs](http://docs.wesjd.net/AnvilGUI/)

## Compilation
Build with `mvn clean install`.

## License
This project is licensed under the [MIT License](LICENSE).
