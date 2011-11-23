package com.ryanmichela.giantcaves;

import org.bukkit.event.Event;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 */
public class GCPlugin extends JavaPlugin {

    private static final String worldName = "world";

    public void onEnable() {
        getServer().getLogger().info("[Giant Caves] Started.");
        getServer().getPluginManager().registerEvent(Event.Type.WORLD_INIT, new GCWorldListener(), Event.Priority.Normal, this);
//        getServer().getPluginManager().registerEvent(Event.Type.CHUNK_POPULATED, new GCWorldListener(), Event.Priority.Normal, this);
    }

    public void onDisable() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private class GCWorldListener extends WorldListener {
        @Override
        public void onWorldInit(WorldInitEvent event) {
            if(event.getWorld().getName().equals(worldName)) {
                getServer().getLogger().info("[Giant Caves] Attaching cave populator to world \"" + event.getWorld().getName() + "\"");
                event.getWorld().getPopulators().add(new GiantCavePopulator());
            }
        }

        @Override
        public void onChunkPopulate(ChunkPopulateEvent event) {
            event.getWorld().refreshChunk(event.getChunk().getX(), event.getChunk().getZ());
        }
    }
}
