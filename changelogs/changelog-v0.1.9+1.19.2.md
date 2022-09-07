# Jar It! v0.1.9+1.19.2

Jar It! version 0.1.9 for Minecraft 1.19.2

Changes:

* Adds some commands to allow operators to manage a world's jars.
    * `/jar-it create <size> [<id>]` - Allows you to create a new jar in the jar-dimension without having to build it
      first, giving you a jar item for the jar just created. Optionally, the id of the newly created jar can be
      specified. If a jar with that id already exists, you are still given a jar item for that jar, but a new jar is not
      created and an error message is printed.
    * `/jar-it destroy <id> [force]` - Destroys a jar with the given id, moving all players and any loaded entities to
      overworld spawn. Use `force` to skip the "are you sure" dialog.
    * `/jar-it enter [<target>] id <id>` - Teleports the user or an optionally given `target` into the jar with the
      given id.
    * `/jar-it enter [<target>] at <block-pos>` - Teleports the user or an optionally given `target` into the jar at the
      given block position.
    * `/jar-it give <id>` - Gives the player a Jar item for the specified jar id.
    * `/jar-it list` - Lists all the jars in the world.
