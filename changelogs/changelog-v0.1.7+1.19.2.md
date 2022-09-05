# Jar It! v0.1.7+1.19.2

Jar It! version 0.1.7 for Minecraft 1.19.2

Changes:

* Fixes issue where mobs in a jar without a player also in it never end up being teleported back when the jar is opened.
    * This is done by having jars that are loaded also load their contents, meaning that machines in a loaded jar will
      continue to run.
