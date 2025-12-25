# Using AnvilGUI
The `AnvilGUI.Builder` class is how you configure the anvil inventory and open it for a player.

The following methods allow you to modify various parts of the displayed GUI. 

Javadocs are available [here](http://docs.wesjd.net/AnvilGUI/).

#### `onClose(Consumer<StateSnapshot>)`

Takes a `Consumer<StateSnapshot>` argument that is called when a player closes the anvil gui.

```java
builder.onClose(stateSnapshot -> {
    stateSnapshot.getPlayer().sendMessage("You closed the inventory.");
});
```

#### `onClick(BiFunction<Integer, AnvilGUI.StateSnapshot, AnvilGUI.ResponseAction>)`

Takes a `BiFunction` with the slot that was clicked and a snapshot of the current gui state.
The function is called when a player clicks any slots in the inventory.
You must return a `List<AnvilGUI.ResponseAction>`, which could include:
- Closing the inventory (`AnvilGUI.ResponseAction.close()`)
- Replacing the input text (`AnvilGUI.ResponseAction.replaceInputText(String)`)
- Updating the title of the inventory (`AnvilGUI.ResponseAction.updateTitle(String, boolean)`)
- Opening another inventory (`AnvilGUI.ResponseAction.openInventory(Inventory)`)
- Running generic code (`AnvilGUI.ResponseAction.run(Runnable)`)
- Nothing! (`Collections.emptyList()`)

The list of actions are ran in the order they are supplied.

```java
builder.onClick((slot, stateSnapshot) -> {
    if (slot != AnvilGUI.Slot.OUTPUT) {
        return Collections.emptyList();
    }

    if (stateSnapshot.getText().equalsIgnoreCase("you")) {
        stateSnapshot.getPlayer().sendMessage("You have magical powers!");
        return Arrays.asList(
            AnvilGUI.ResponseAction.close(),
            AnvilGUI.ResponseAction.run(() -> myCode(stateSnapshot.getPlayer()))
        );
    } else {
        return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Try again"));
    }
});
```

#### `onClickAsync(ClickHandler)`

Takes a `ClickHandler`, a shorthand for `BiFunction<Integer, AnvilGui.StateSnapshot, CompletableFuture<AnvilGUI.ResponseAction>>`,
that behaves exactly like `onClick()` with the difference that it returns a `CompletableFuture` and therefore allows for
asynchronous calculation of the `ResponseAction`s.

```java
builder.onClickAsync((slot, stateSnapshot) -> CompletedFuture.supplyAsync(() -> {
    // this code is now running async
    if (slot != AnvilGUI.Slot.OUTPUT) {
        return Collections.emptyList();
    }

    if (database.isMagical(stateSnapshot.getText())) {
        // the `ResponseAction`s will run on the main server thread
        return Arrays.asList(
            AnvilGUI.ResponseAction.close(),
            AnvilGUI.ResponseAction.run(() -> myCode(stateSnapshot.getPlayer()))
        );
    } else {
        return Arrays.asList(AnvilGUI.ResponseAction.replaceInputText("Try again"));
    }
}));
```

#### `allowConcurrentClickHandlerExecution()`

Tells the AnvilGUI to disable the mechanism that is put into place to prevent concurrent execution of the
click handler set by `onClickAsync(ClickHandler)`.

```java
builder.allowConcurrentClickHandlerExecution();
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

#### `geyserCompat()`

This toggles compatibility with Geyser software, specifically being able to use AnvilGUI with 0 experience level on Bedrock.
Enabled by default.

```java
builder.geyserCompat();
```

#### `text(String)`

Takes a `String` that contains what the initial text in the renaming field should be set to.
If `itemLeft` is provided, then the display name is set to the provided text. If no `itemLeft`
is set, then a piece of paper will be used.

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

#### `itemRight(ItemStack)`

Takes a custom `ItemStack` to be placed in the right input slot.

```java
ItemStack stack = new ItemStack(Material.IRON_INGOT);
ItemMeta meta = stack.getItemMeta();
meta.setLore(Arrays.asList("A piece of metal"));
stack.setItemMeta(meta);
builder.itemRight(stack);
```

#### `title(String)`

Takes a `String` that will be used literally as the inventory title. Only displayed in Minecraft 1.14 and above.

```java
builder.title("Enter your answer");
```

#### `jsonTitle(String)`

Takes a `String` which contains rich text components serialized as JSON.
Useful for settings titles with hex color codes or Adventure Component interop.
Only displayed in Minecraft 1.14 and above.

```java
builder.jsonTitle("{\"text\":\"Enter your answer\",\"color\":\"green\"}")
```

#### `plugin(Plugin)`

Takes the `Plugin` object that is making this anvil gui. It is needed to register listeners.

```java
builder.plugin(pluginInstance);
```

#### `mainThreadExecutor(Executor)`

Takes an `Executor` that must execute on the main server thread.
If the main server thread is not accessible via the Bukkit scheduler, like on Folia servers, it can be swapped for a
Folia aware executor.

```java
builder.mainThreadExecutor(executor);
```

#### `open(Player)`

Takes a `Player` that the anvil gui should be opened for. This method can be called multiple times without needing to create
a new `AnvilGUI.Builder` object.

```java
builder.open(player);
```

## Example

```java
new AnvilGUI.Builder()
    .onClose(stateSnapshot -> {
        stateSnapshot.getPlayer().sendMessage("You closed the inventory.");
    })
    .onClick((slot, stateSnapshot) -> { // Either use sync or async variant, not both
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
    .preventClose()                                                    //prevents the inventory from being closed
    .text("What is the meaning of life?")                              //sets the text the GUI should start with
    .title("Enter your answer.")                                       //set the title of the GUI (only works in 1.14+)
    .plugin(myPluginInstance)                                          //set the plugin instance
    .open(myPlayer);                                                   //opens the GUI for the player provided
```
