# Jar It! v0.1.11+1.19.2

Jar It! version 0.1.11 for Minecraft 1.19.2

Changes:

* Adds a command to lock a jar and prevent it from being opened.
    * `/jar-it lock <id>` - This command locks a jar and prevents it from being opened with a Jar Opener.
    * `/jar-it unlock <id>` - This command unlocks a locked jar and allows it to be opened with a Jar Opener.
* Makes `/jar-it destroy <id>`'s confirm button suggest the command to actually destroy the jar, instead of straight-up
  running it.
* Makes management commands broadcast to ops.
* Fixes some lang errors.
