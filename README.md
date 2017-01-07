#AnvilGUI
Easily use anvil guis to get a user's input.

This project was made since their is no easy way to do this with the Spigot / Bukkit APIs. It requires interaction
with NMS and that is a pain in a non-private plugin where users will have lots of different versions of the server
running.

##Requirements
Java 8 and Bukkit / Spigot. Most server versions in the [Spigot Repository](https://hub.spigotmc.org/nexus/) are supported.

###My version isn't supported
If you are a developer, submit a pull request adding a wrapper class for your version. Otherwise, please create an issue
on the issues tab. 

##How to use

###As a dependency

```xml
<dependencies>
    <dependency>
        <groupId>net.wesjd</groupId>
        <artifactId>anvilgui</artifactId>
        <version>1.1.1-SNAPSHOT</version>
    </dependency>
    ...
</dependencies>

<repositories>
    <repository>
        <id>wesjd-repo</id>
        <url>https://nexus.wesjd.net/repository/thirdparty/</url>
    </repository>
    ...
</repositories>
```

###In your plugin

####Prompting a user for input

```java
new AnvilGUI(myPluginInstance, myPlayer, "What is the meaning of life?", (player, reply) -> {
    if (reply.equalsIgnoreCase("you")) {
        player.sendMessage("You have magical powers!");
        return null;
    }
    return "Incorrect.";
});
```
The AnvilGUI takes in a parameter of your plugin, the player that the GUI should open for, a prompt, and the
`BiFunction`. The first two parameters are quite obvious, and the third for example would be a question, just like
what is shown above.

####Handling their answer

```java
(player, reply) -> {
    if (reply.equalsIgnoreCase("you")) {
        player.sendMessage("You have magical powers!");
        return null;
    }
    return "Incorrect.";
}
```
The above code is what is inside your `BiFunction`. The parameters of the function are also obvious, the player who answered
and their reply. The function also returns a `String`. This string is to be used if the user is wrong, etc,
and it will show in the dialogue box in the GUI what is supplied. If you return `null`, the inventory will close.

###[Javadocs](http://docs.wesjd.net/AnvilGUI/)

##Compilation

Build with `mvn clean install`. Do note that you will need the spigot jars in this repo to be installed on your
local repository. To make this easier, you can use [this shell script](https://gist.github.com/WesJD/39b8f0c88f74bc952e27a737d3a67234).

##License

This project is licensed under the [MIT License](LICENSE).
