# Jar It! v0.1.6+1.19.2

Jar It! version 0.1.6 for Minecraft 1.19.2

Changes:

* Fixes de-sync issues with jars capturing players which also allows jars to capture all entities.
* Fixes jars not actually enforcing their size cap.
* Fixes blocks that get replaced by expanding jars not dropping anything.
* Fixes jars not checking for gaps in their walls.
* Adds helpful messages telling you what is wrong with your jar.

Known Issues:

* Mobs in a jar are not returned when a jar is opened unless a player is also in the jar.
    * In order to fix this, I'll have to setup a jar-ticking chunk-loading system.
