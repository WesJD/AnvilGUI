# AnvilGUI [![Build Status](https://ci.codemc.io/job/WesJD/job/AnvilGUI/badge/icon)](https://ci.codemc.io/job/WesJD/job/AnvilGUI/)
Easily use anvil guis to get a user's input.

This project was made since there is no way to prompt users with an anvil input with the Spigot / Bukkit API. It requires interaction with NMS and that is a pain in plugins where users have different versions of the server running.

## Requirements
Java 8 and Bukkit / Spigot. Most server versions in the [Spigot Repository](https://hub.spigotmc.org/nexus/) are supported.

### My version isn't supported
If you are a developer, submit a pull request adding a wrapper module for your version. Otherwise, please create an issue
on the issues tab. 

## Usage

### As a dependency

AnvilGUI requires the usage of Maven or a Maven compatible build system. 
```xml
<dependency>
    <groupId>net.wesjd</groupId>
    <artifactId>anvilgui</artifactId>
    <version>1.6.0-SNAPSHOT</version>
</dependency>

<repository>
    <id>codemc-snapshots</id>
    <url>https://repo.codemc.io/repository/maven-snapshots/</url>
</repository>
```

It is best to be a good citizen and relocate the dependency to within your namespace in order 
to prevent conflicts with other plugins. Here is an example of how to relocate the dependency:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>${shade.version}</version> <!-- The version must be at least 3.3.0 -->
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <relocations>
                            <relocation>
                                <pattern>net.wesjd.anvilgui</pattern>
                                <shadedPattern>[YOUR_PLUGIN_PACKAGE].anvilgui</shadedPattern> <!-- Replace [YOUR_PLUGIN_PACKAGE] with your namespace -->
                            </relocation>
                        </relocations>
                        <minimizeJar>false</minimizeJar> <!-- Ensure not set to true -->
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
Please note: A lot of tutorials advise you set `<minimizeJar>` to true, but this will break version matching in the library.
If set to true, the different `VersionWrapper`s won't be shaded into the final artifact and no Minecraft version will be compatible.

### In your plugin

The `AnvilGUI.Builder` class is how you build an AnvilGUI. 
The following methods allow you to modify various parts of the displayed GUI. Javadocs are available [here](http://docs.wesjd.net/AnvilGUI/).

#### `onClose(Consumer<Player>)` 
Takes a `Consumer<Player>` argument that is called when a player closes the anvil gui.
```java                                             
builder.onClose(player -> {                         
    player.sendMessage("You closed the inventory.");
});                                                 
``` 

#### `onComplete(BiFunction<Player, String, AnvilGUI.Response>)`  
Takes a `BiFunction<Player, String, AnvilGUI.Response>` argument. The BiFunction is called when a player clicks the output slot. 
The supplied string is what the player has inputted in the renaming field of the anvil gui. You must return an AnvilGUI.Response,
which can either be `close()`, `text(String)`, or `openInventory(Inventory)`. Returning `close()` will close the inventory; returning 
`text(String)` will keep the inventory open and put the supplied String in the renaming field; returning `openInventory(Inventory)`
will open the provided inventory, which is useful for when a user has finished their input in GUI menus.
```java                                                
builder.onComplete((player, text) -> {                 
    if(text.equalsIgnoreCase("you")) {                 
        player.sendMessage("You have magical powers!");
        return AnvilGUI.Response.close();              
    } else {                                           
        return AnvilGUI.Response.text("Incorrect.");   
    }                                                  
});                                                    
```

#### `onComplete(Function<AnvilGUI.Completion, AnvilGUI.Response>)`
Takes a `Function<AnvilGUI.Completion, AnvilGUI.Response>` as argument. The completion is called when a player clicks the output slot.
The supplied completion contains the player who clicked, the inputted text, the left item, right item and the output. You must return an AnvilGUI.Response,
which can either be `close()`, `text(String)`, or `openInventory(Inventory)`. Returning `close()` will close the inventory; returning 
`text(String)` will keep the inventory open and put the supplied String in the renaming field; returning `openInventory(Inventory)`
will open the provided inventory, which is useful for when a user has finished their input in GUI menus.
Useful for situations when dealing with an interactive anvil gui.
```java
builder.onComplete(completion -> {
    player.sendMessage("Left is a item with the Type of " + completion.getLeft().getType());
    return AnvilGUI.Response.close();
});
```

#### `interactableSlots(int... slots)`
This allows or denies users to take / input items in the anvil slots that are provided. This feature is useful when you try to make a inputting system using an anvil gui.
```java
builder.canBeInteractedWith(Slot.INPUT_LEFT, Slot.INPUT_RIGHT);
```

#### `preventClose()` 
Tells the AnvilGUI to prevent the user from pressing escape to close the inventory.
Useful for situations like password input to play.                                      
```java                     
builder.preventClose();     
```                     
     
#### `text(String)`
Takes a `String` that contains what the initial text in the renaming field should be set to.
```java                                           
builder.text("What is the meaning of life?");     
```  

#### `itemLeft(ItemStack)`
Takes a custom `ItemStack` to be placed in the left input slot.
```java                                              
ItemStack stack = new ItemStack(Material.IRON_SWORD);
ItemMeta meta = stack.getItemMeta();                 
meta.setLore(Arrays.asList("Sharp iron sword"));             
stack.setItemMeta(meta); 
builder.itemLeft(stack);        
```         

#### `onLeftInputClick(Consumer<Player>)`
Takes a `Consumer<Player>` to be executed when the item in the left input slot is clicked.
```java                                              
builder.onLeftInputClick(player -> {
    player.sendMessage("You clicked the left input slot!");
});        
```      

#### `itemRight(ItemStack)`
Takes a custom `ItemStack` to be placed in the right input slot.
```java                                              
ItemStack stack = new ItemStack(Material.IRON_INGOT);
ItemMeta meta = stack.getItemMeta();                 
meta.setLore(Arrays.asList("A piece of metal"));             
stack.setItemMeta(meta); 
builder.itemRight(stack);        
```         

#### `onRightInputClick(Consumer<Player>)`
Takes a `Consumer<Player>` to be executed when the item in the right input slot is clicked.
```java                                              
builder.onRightInputClick(player -> {
    player.sendMessage("You clicked the right input slot!");
});        
```

#### `title(String)`
Takes a `String` that will be used as the inventory title. Only displayed in Minecraft 1.14 and above.
```java                            
builder.title("Enter your answer");
```                                
                 
#### `plugin(Plugin)`
Takes the `Plugin` object that is making this anvil gui. It is needed to register listeners.
```java                                         
builder.plugin(pluginInstance);                 
```                            

#### `open(Player)`
Takes a `Player` that the anvil gui should be opened for. This method can be called multiple times without needing to create
a new `AnvilGUI.Builder` object.                                                                                            
```java              
builder.open(player);
```                  

### A full example combining all methods
```java
new AnvilGUI.Builder()
    .onClose(player -> {                                               //called when the inventory is closing
        player.sendMessage("You closed the inventory.");
    })
    .onComplete((player, text) -> {                                    //called when the inventory output slot is clicked
        if(text.equalsIgnoreCase("you")) {
            player.sendMessage("You have magical powers!");
            return AnvilGUI.Response.close();
        } else {
            return AnvilGUI.Response.text("Incorrect.");
        }
    })
    .preventClose()                                                    //prevents the inventory from being closed
    .interactableSlots(Slot.INPUT_RIGHT)                               //allow player to take out and replace the right input item
    .text("What is the meaning of life?")                              //sets the text the GUI should start with
    .itemLeft(new ItemStack(Material.IRON_SWORD))                      //use a custom item for the first slot
    .itemRight(new ItemStack(Material.IRON_SWORD))                     //use a custom item for the second slot
    .onLeftInputClick(player -> player.sendMessage("first sword"))     //called when the left input slot is clicked
    .onRightInputClick(player -> player.sendMessage("second sword"))   //called when the right input slot is clicked
    .title("Enter your answer.")                                       //set the title of the GUI (only works in 1.14+)
    .plugin(myPluginInstance)                                          //set the plugin instance
    .open(myPlayer);                                                   //opens the GUI for the player provided
```
                                                                                                                                                                                                                                                                              

## Development 
We use Maven to handle our dependencies. Run `mvn clean install` using Java 17 to build the project.

### Spotless
The project utilizes the [Spotless Maven Plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven) to
enforce style guidelines. You will not be able to build the project if your code does not meet the guidelines.
To fix all code formatting issues, simply run `mvn spotless:apply`.

## License
This project is licensed under the [MIT License](LICENSE).
