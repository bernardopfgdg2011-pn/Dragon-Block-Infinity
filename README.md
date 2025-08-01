# Branch Information

This branch specifically tackles the PedestalBlockRenderer desync issues and its boilerplate by making
use of [Cardinal Components API](https://ladysnake.org/wiki/cardinal-components-api/).

The [Implemented Inventory Interface](./src/main/java/net/kaupenjoe/tutorialmod/block/entity/ImplementedInventory.java)
has been switched out with a direct `Sided Inventory` implementation in
the [BlockEntity](./src/main/java/net/kaupenjoe/tutorialmod/block/entity/custom/PedestalBlockEntity.java) to support
component syncing. If necessary, this can get cleaned up more.

The [Pedestal Component](./src/main/java/net/kaupenjoe/tutorialmod/cca/component/SyncedPedestalComponent.java) also
takes
care of the rotation for the client side rendering now.

For more information check out [Cardinal Component API's wiki](https://ladysnake.org/wiki/cardinal-components-api/).

---

<a href="https://www.youtube.com/playlist?list=PLKGarocXCE1H_HxOYihQMq0mlpqiUJj4L" target="_blank">
<p align="center">
<img src="https://kaupenjoe.net/files/General/Minecraft/Modding/Tutorials/fabric-tutorial-image-1.png" alt="Logo" width="1000"/> 
</p></a>

# Fabric Modding Tutorials For Minecraft 1.21.X

This is the GitHub Repository for Kaupenjoe's Fabric Modding Tutorials For Minecraft 1.21.X

The Individual Tutorials are seperated into Branches for ease of access.

Watch the Tutorials
here: <a href="https://www.youtube.com/playlist?list=PLKGarocXCE1H_HxOYihQMq0mlpqiUJj4L" target="_blank">YouTube
Playlist</a>
