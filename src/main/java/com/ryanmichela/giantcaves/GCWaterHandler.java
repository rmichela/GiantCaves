package com.ryanmichela.giantcaves;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import java.util.WeakHashMap;

/**
 * Copyright 2013 Ryan Michela
 */
public class GCWaterHandler implements Listener {
    // Keep a map of previous random generators, one per chunk.
    // WeakHashMap is used to ensure that chunk unloading is not inhibited. This map won't
    // prevent a chunk from being unloaded and garbage collected.
    private final WeakHashMap<Chunk, GCRandom> randoms = new WeakHashMap<>();

    private final Config config;

    public GCWaterHandler(Config config) {
        this.config = config;
    }

    @EventHandler
    public void FromToHandler(BlockFromToEvent event) {
        // During chunk generation, nms.World.d is set to true. While true, liquids
        // flow continuously instead tick-by-tick. See nms.WorldGenLiquids line 59.
        Block b = event.getBlock();
        if (b.getType() == Material.STATIONARY_WATER || b.getType() == Material.STATIONARY_LAVA) {
            boolean continuousFlowMode = ((CraftWorld)event.getBlock().getWorld()).getHandle().d;
            if (continuousFlowMode) {
                CraftChunk c = (CraftChunk)b.getChunk();
                if (!randoms.containsKey(c)) {
                    randoms.put(c, new GCRandom(c, config));
                }
                GCRandom r = randoms.get(c);

                if (r.isInGiantCave(b.getX(), b.getY(), b.getZ())) {
                    if (b.getData() == 0) { // data value of 0 means source block
                            event.setCancelled(true);
                        }
                }
            }
        }
    }
}
