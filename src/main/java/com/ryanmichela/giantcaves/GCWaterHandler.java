package com.ryanmichela.giantcaves;

import net.minecraft.server.v1_6_R3.ChunkSection;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_6_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
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
        boolean continuousFlowMode = ((CraftWorld)event.getBlock().getWorld()).getHandle().d;
        if (continuousFlowMode) {
            Block b = event.getBlock();
            Block b2 = event.getToBlock();
            CraftChunk c = (CraftChunk)b.getChunk();
            if (!randoms.containsKey(c)) {
                randoms.put(c, new GCRandom(c, config));
            }
            GCRandom r = randoms.get(c);

            if (r.isInGiantCave(b.getX(), b.getY(), b.getZ())) {
                if (b2.getRelative(BlockFace.DOWN, 1).getType() == Material.AIR &&
                    b2.getRelative(BlockFace.DOWN, 2).getType() == Material.AIR) {
                    // Convert global coordinates to chunk offsets
                    int xx = b.getX() % 16;
                    int zz = b.getZ() % 16;
                    if (c.getX() < 0) xx = (16 - xx) & 0xF;
                    if (c.getZ() < 0) zz = (16 - zz) & 0xF;

                    if (xx == 0 || xx == 15 || zz == 0 || zz == 15) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
