//Copyright (C) 2011  Ryan Michela
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.ryanmichela.giantcaves;

import net.minecraft.server.ChunkSection;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.noise.*;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

/**
 */
public class GiantCavePopulator extends BlockPopulator {

    public Plugin plugin;
    private Config config;
// Note: Smaller frequencies yield slower change (more stretched out)
//   Larger amplitudes yield greater influence on final void
// Frequency
    private final double f1xz;
    private final double f1y;
// Density
    private final int amplitude1 = 100;
    private final double subtractForLessThanCutoff;
// Second pass - small noise
    private final double f2xz = 0.25;
    private final double f2y = 0.05;
    private final int amplitude2 = 2;
// Third pass - vertical noise
    private final double f3xz = 0.025;
    private final double f3y = 0.005;
    private final int amplitude3 = 20;
// Position
    private final int caveBandBuffer;
// Material
    private final byte materialId;

    public GiantCavePopulator(Plugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        subtractForLessThanCutoff = amplitude1 - config.cutoff;
        materialId = (byte)(config.debugMode ? 1 : 0); // Stone in debug, air in release
        f1xz = 1.0 / config.sxz;
        f1y = 1.0 / config.sy;
        if (config.caveBandMax - config.caveBandMin > 128) {
            caveBandBuffer = 32;
        } else {
            caveBandBuffer = 16;
        }
    }

    @Override
    public void populate(final World world, final Random random, final Chunk source) {
        boolean chunkHasGiantCave = false;
        net.minecraft.server.Chunk nmsChunk = ((CraftChunk)source).getHandle();
        ChunkSection[] chunkSections = nmsChunk.i();
        final Set<Block> fixBlocks = new HashSet<Block>();
        boolean flag = false;

        NoiseGenerator noiseGen1 = new SimplexNoiseGenerator(world);
        NoiseGenerator noiseGen2 = new SimplexNoiseGenerator((long)noiseGen1.noise(source.getX(), source.getZ()));
        NoiseGenerator noiseGen3 = new SimplexNoiseGenerator((long)noiseGen1.noise(source.getX(), source.getZ()));

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = config.caveBandMax; y >= config.caveBandMin; y--) {
                    /*
                     * if (y < config.caveBandMin || y > config.caveBandMax) {
                     * continue;
                     * }
                     */ //Wut...

                    double xx = (source.getX() << 4) | (x & 0xF);
                    double yy = y;
                    double zz = (source.getZ() << 4) | (z & 0xF);
                    if ((noiseGen1.noise(xx * f1xz, yy * f1y, zz * f1xz) * amplitude1)
                        + (noiseGen2.noise(xx * f2xz, yy * f2y, zz * f2xz) * amplitude2)
                        - (noiseGen3.noise(xx * f3xz, yy * f3y, zz * f3xz) * amplitude3)
                        - linearCutoffCoefficient(y) > config.cutoff) {
                        chunkHasGiantCave = true;
                        int oldBlockId = nmsChunk.getTypeId(x, y, z);

                        //if (oldBlockId == Material.STATIONARY_WATER.getId() || oldBlockId == Material.STATIONARY_LAVA.getId()) {
                        //if ((y > 0) && (((nmsChunk.getTypeId(x, y - 1, z) != Material.STATIONARY_WATER.getId()) && (nmsChunk.getTypeId(x, y - 1, z) != Material.STATIONARY_LAVA.getId())) || (nmsChunk.getTypeId(x, y - 1, z) == Material.AIR.getId()))) {
                        //    fixBlocks.add(source.getBlock(x, y, z));
                        //}
                        //}

                        // See NMS.Chunk.a() line 368-375
                        ChunkSection cs = chunkSections[y >> 4];
                        if (cs == null) {
                            cs = chunkSections[y >> 4] = new ChunkSection(y >> 4 << 4);
                            flag = true;
                        }
                        // Set the target block to materialId.
                        if (oldBlockId == Material.STATIONARY_WATER.getId() || oldBlockId == Material.STATIONARY_LAVA.getId()) { //Don't do it if it may be an ocean.
                            if ((y > 0) && (((nmsChunk.getTypeId(x, y - 1, z) != Material.STATIONARY_WATER.getId()) && (nmsChunk.getTypeId(x, y - 1, z) != Material.STATIONARY_LAVA.getId())) || (nmsChunk.getTypeId(x, y - 1, z) == Material.AIR.getId()))) {
                                cs.a(x, y & 15, z, materialId);
                            }
                        } else {
                            cs.a(x, y & 15, z, materialId);
                        }
                        //Update blocks
                        if (y > 0) { //Update below
                            nmsChunk.world.a(x, y-1, z, nmsChunk.getTypeId(x, y-1, z), 4); //Update block
                        }
                        nmsChunk.world.a(x, y, z, nmsChunk.getTypeId(x, y, z), 4); //Update block
                        if (y < 254) { //Update above
                            nmsChunk.world.a(x, y+1, z, nmsChunk.getTypeId(x, y+1, z), 4); //Update block
                        }
                        // Strip out any TileEntity that may remain
                        nmsChunk.f(x, y, z);
                    }
                }
            }
        }

        if (flag) {
            nmsChunk.initLighting();
        }

        //if (!fixBlocks.isEmpty()) {
        //plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
        //	public void run() {
        //	    for (Block block : fixBlocks) {
        //	        // Placing a air block where lava/water source blocks used to be kills lava/water towers
        //		    block.setType(Material.AIR);
        //		    block.setData((byte)0);
        //	    }
        //	}
        //}, 10);
        //}
    }

    private double linearCutoffCoefficient(int y) {
        // Out of bounds
        if (y < config.caveBandMin || y > config.caveBandMax) {
            return subtractForLessThanCutoff;
            // Bottom layer distortion
        } else if (y >= config.caveBandMin && y <= config.caveBandMin + caveBandBuffer) {
            double yy = y - config.caveBandMin;
            return (-subtractForLessThanCutoff / (double)caveBandBuffer) * yy + subtractForLessThanCutoff;
            // Top layer distortion
        } else if (y <= config.caveBandMax && y >= config.caveBandMax - caveBandBuffer) {
            double yy = y - config.caveBandMax + caveBandBuffer;
            return (subtractForLessThanCutoff / (double)caveBandBuffer) * yy;
            // In bounds, no distortion
        } else {
            return 0;
        }
    }
}
