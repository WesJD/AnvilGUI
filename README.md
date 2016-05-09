#AnvilGUI

Easily user anvil guis to get a user's input.

This project was made since their is no easy way to do this with the Spigot / Bukkit APIs. It requires interaction
with NMS and that is a pain in a non-private plugin where users will have lots of different versions of the server
running.

##How to use

###As a dependency

```xml
<dependencies>
    <dependency>
        <groupId>net.buildstatic.util</groupId>
        <artifactId>anvilgui</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    ...
</dependencies>

<repositories>
    <repository>
        <id>buildstatic-repo</id>
        <name>BuildStatic Repository</name>
        <url>http://serv.buildstatic.net/maven-repo</url>
    </repository>
    ...
</repositories>
```
alternatively, you can download a jar [here](http://ci.buildstatic.net/job/AnvilGUI/).

###In your plugin

####Prompting a user for input

```java
new AnvilGUI(myPluginInstance, holder, "What is the meaning of life?", new AnvilGUI.ClickHandler() {
    @Override
    public String onClick(Player player, String reply) {
        if (reply.equalsIgnoreCase("you")) {
            player.sendMessage("You have magical powers!");
            return null;
        }
        return "Incorrect.";
    }
});
```
The AnvilGUI takes in a parameter of your plugin, the player that the GUI should open for, a prompt, and the
`ClickHandler`. The first two parameters are quite obvious, and the third for example would be a question, just like 
what is shown above.

###Handling their answer

```java
@Override
public String onClick(Player player, String reply) {
    if (reply.equalsIgnoreCase("you")) {
        player.sendMessage("You have magical powers!");
        return null;
    }
    return "Incorrect.";
}
```
The above code is what is inside your `ClickHandler`. The parameters of the method are also obvious, the player who answered 
and their reply. The method also has a return value of `String`. This string is to be used if the user is wrong, etc,
and it will show in the dialogue box in the GUI what is supplied. If you return `null`, the inventory will close.

##Compilation

We use Maven to handle our dependencies: `mvn clean install`. Do note that you will need the four spigot jars in use
in this repo to be installed on your local repository.

##License

This project is licensed under the [MIT License](LICENSE).
