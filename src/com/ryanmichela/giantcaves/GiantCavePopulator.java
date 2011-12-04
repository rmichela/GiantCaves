//    Copyright (C) 2011  Ryan Michela
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.ryanmichela.giantcaves;

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

/**
 */
public class GiantCavePopulator extends BlockPopulator{

    public Plugin plugin;
    private Config config;

    public GiantCavePopulator(Plugin plugin, Config config)
    {
        this.plugin = plugin;
        this.config = config;
        blockingCoefficient = amplitude - config.cutoff;
        materialId = (byte)(config.debugMode ? 1 : 0); // Stone in debug, air in release
        fxz = 1.0 / config.sxz;
        fy = 1.0 / config.sy;
    }

    // Frequency
    private final double fxz;
    private final double fy;

    // Density
    private final int amplitude = 100;
    private final int blockingCoefficient;

    // Second pass
    private final double f2xz = 0.25;
    private final double f2y = 0.05;
    private final int amplitude2 = 2;

    // Position
    private final int caveBandBuffer = 16;

    // Material
    private final byte materialId;

    @Override
    public void populate(final World world, final Random random, final Chunk source) {
        boolean chunkHasGiantCave = false;
        byte[] chunkVector = ((CraftChunk)source).getHandle().b;
        final Set<Block> needsPhysics = new HashSet<Block>();

        NoiseGenerator noiseGen = new SimplexNoiseGenerator(world);

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                for(int y = 0; y < 128; y++) {
                    if(y < config.caveBandMin || y > config.caveBandMax) continue;

                    double xx = (source.getX() << 4) | (x & 0xF);
                    double yy = y & 0x7F;
                    double zz = (source.getZ() << 4) | (z & 0xF);
                    if(    noiseGen.noise(xx * fxz, yy * fy, zz * fxz) * amplitude
                         + noiseGen.noise(xx * f2xz, yy * f2y, zz * f2xz) * amplitude2
                         - linearCutoffCoefficient(y) > config.cutoff)
                    {
                        chunkHasGiantCave = true;
                        int loc = blockOffset(x, y, z);

                        if(chunkVector[loc] == 9 || chunkVector[loc] == 11) {
                            needsPhysics.add(source.getBlock(x,y,z));
                        }

                        chunkVector[loc] = materialId;
                        //chunkVector[blockOffset(x,120,z)] = 20;
                    }
                }
            }
        }

        if(needsPhysics.size() > 0) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    for(Block block : needsPhysics) {
                        // Placing a gravel block where lava/water source blocks used to be kills lava/water towers
                        block.setType(Material.GRAVEL);
                    }
                }
            });
        }

        if(chunkHasGiantCave) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    world.refreshChunk(source.getX(), source.getZ());
                }
            });
        }
    }

    private int blockOffset(int x, int y, int z) {
        return x << 11 | z << 7 | y;
    }

    private double linearCutoffCoefficient(int y) {
        // No distortion
        if( y < config.caveBandMin || y > config.caveBandMax) {
            return blockingCoefficient;
        // Bottom layer distortion
        } else if (y >= config.caveBandMin && y <= config.caveBandMin + caveBandBuffer) {
            int yy = y - config.caveBandMin;
            return (-blockingCoefficient / caveBandBuffer) * yy + blockingCoefficient;
        // Top layer distortion
        } else if (y <= config.caveBandMax && y >= config.caveBandMax - caveBandBuffer) {
            int yy = y - config.caveBandMax + caveBandBuffer;
            return (blockingCoefficient / caveBandBuffer) * yy;
        // Out of range
        } else {
            return 0;
        }
    }
}
