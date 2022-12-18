# AnvilGUI [![Build Status](https://ci.codemc.io/job/WesJD/job/AnvilGUI/badge/icon)](https://ci.codemc.io/job/WesJD/job/AnvilGUI/)
AnvilGUI is a library to capture user input in Minecraft through an anvil inventory. Anvil inventories within the realm
of the Minecraft / Bukkit / Spigot / Paper API are extremely finnicky and ultimately don't support the ability to use them fully for
the task of user input. As a result, the only way to achieve user input with an anvil inventory requires interaction with obfuscated,
decompiled code. AnvilGUI provides a straightforward, versatile, and easy-to-use solution without having your project
depend on version specific code. 

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
    <version>1.6.2-SNAPSHOT</version>
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
                        <filters>
                            <filter>
                                <artifact>*:*</artifact>
                                <excludeDefaults>false</excludeDefaults>
                                <includes>
                                    <include>[YOUR_PLUGIN_PACKAGE].anvilgui</include>
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
Note: In order to solve `<minimizeJar>` removing AnvilGUI `VerionWrapper`s from the final jar and making the library unusable,
ensure that your `<filters>` section contains the example `<filter>` as seen above.

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

#### `onComplete(Function<AnvilGUI.Completion, AnvilGUI.Response>)`
Takes a `Function<AnvilGUI.Completion, List<AnvilGUI.ResponseAction>>` as argument. The function is called when a player clicks the output slot.
The supplied `Completion` contains the player who clicked, the inputted text, the left item, right item and the output. You must return a `List<AnvilGUI.ResponseAction>`,
which could include:
- Closing the inventory (`AnvilGUI.ResponseAction.close()`)
- Replacing the input text (`AnvilGUI.ResponseAction.replaceInputText(String)`)
- Opening another inventory (`AnvilGUI.ResponseAction.openInventory(Inventory)`)
- Running generic code (`AnvilGUI.ResponseAction.close(Runnable)`)

The list of actions are ran in the order they are supplied.
```java                                                
builder.onComplete((completion) -> {                 
    if(completion.getText().equalsIgnoreCase("you")) {
        completion.getPlayer().sendMessage("You have magical powers!");
        return Arrays.asList(AnvilGUI.ResponseAction.close());              
    } else {                                           
        return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Try again"));   
    }                                                  
});                                                    
```

#### `interactableSlots(int... slots)`
This allows or denies users to take / input items in the anvil slots that are provided. This feature is useful when you try to make a inputting system using an anvil gui.
```java
builder.interactableSlots(Slot.INPUT_LEFT, Slot.INPUT_RIGHT);
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
    .onComplete((completion) -> {                                    //called when the inventory output slot is clicked
        if(completion.getText().equalsIgnoreCase("you")) {
            completion.getPlayer().sendMessage("You have magical powers!");
            return Arrays.asList(AnvilGUI.ResponseAction.close());
        } else {
            return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Try again"));
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
