# AnvilGUI
Easily use anvil guis to get a user's input.

This project was made since there is no way to prompt users with an anvil input with the Spigot / Bukkit API. It requires interaction with NMS and that is a pain in plugins where users have different versions of the server running.

## Requirements
Java 8 and Bukkit / Spigot. Most server versions in the [Spigot Repository](https://hub.spigotmc.org/nexus/) are supported.

### My version isn't supported
If you are a developer, submit a pull request adding a wrapper class for your version. Otherwise, please create an issue
on the issues tab. 

## How to use
### [As a dependency](https://jitpack.io/#WesJD/AnvilGUI)
### In your plugin
```java
new AnvilGUI.Builder()
    .onClose(player -> {                      //called when the inventory is closing
        player.sendMessage("You closed the inventory.");
    })
    .onComplete((player, text) -> {           //called when the inventory output slot is clicked
        if(text.equalsIgnoreCase("you")) {
            player.sendMessage("You have magical powers!");
            return AnvilGUI.Response.close();
        } else {
            return AnvilGUI.Response.text("Incorrect.");
        }
    })
    .preventClose()                           //prevents the inventory from being closed
    .text("What is the meaning of life?")     //sets the text the GUI should start with
    .item(new ItemStack(Material.GOLD_BLOCK)) //use a custom item for the first slot
    .title("Enter your answer.")              //set the title of the GUI (only works in 1.14+)
    .plugin(myPluginInstance)                 //set the plugin instance
    .open(myPlayer);                          //opens the GUI for the player provided
```

### [Javadocs](http://docs.wesjd.net/AnvilGUI/)

## Compilation
Build with `mvn clean install`.

## License
This project is licensed under the [MIT License](LICENSE).
