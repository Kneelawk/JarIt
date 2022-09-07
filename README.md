# Jar It!

You can put things in jars.

## ModFest

![ModFest Singularity Icon](https://cdn.discordapp.com/attachments/1008539448016916717/1011047045735395448/ModFest_Singularity_Banner.png)

This mod is a submission for the 2022 ModFest: Singularity.

## Usage

1. Surround whatever you want to jar in a cube of Jar Glass.
2. Use the Jar Cork on one of the top blocks of the jar glass. This block must not be on an edge or a corner.
3. The jar's contents are collapsed into a single Jar block that can be picked up and brought wherever.
4. Use the Jar Opener on a placed jar to expand it again back into the cube of Jar Glass with contents preserved.

### Commands

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

## Known Issues

* Jars do not render their insides and being inside a jar does not render what is outside the jar.
* Jars do not track if their item or block gets destroyed, meaning that jars can be orphaned. Commands can be used to
  obtain items for these jars.
* Jars that are not loaded when they're destroyed via command do not transport their entities to overworld spawn.
