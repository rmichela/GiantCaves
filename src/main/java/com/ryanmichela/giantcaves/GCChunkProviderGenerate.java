package com.ryanmichela.giantcaves;

import net.minecraft.server.v1_6_R3.*;

import java.util.Random;

/**
 * Copyright 2013 Ryan Michela
 */
public class GCChunkProviderGenerate extends ChunkProviderGenerate {
    public GCChunkProviderGenerate(World world, long l, boolean b) {
        super(world, l, b);
    }

    @Override
    public Chunk getChunkAt(int i, int j) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Chunk getOrCreateChunk(int i, int j) {
        throw new UnsupportedOperationException();
    }

    public byte[] getChunkSectionsAt(int i, int j) {
        Random k = ReflectionUtil.getProtectedValue(this, "k");
        World p = ReflectionUtil.getProtectedValue(this, "p");
        Boolean q = ReflectionUtil.getProtectedValue(this, "q");
        WorldGenBase t = ReflectionUtil.getProtectedValue(this, "t");
        WorldGenStronghold u = ReflectionUtil.getProtectedValue(this, "u");
        WorldGenVillage v = ReflectionUtil.getProtectedValue(this, "v");
        WorldGenMineshaft w = ReflectionUtil.getProtectedValue(this, "w");
        WorldGenLargeFeature x = ReflectionUtil.getProtectedValue(this, "x");
        WorldGenBase y = ReflectionUtil.getProtectedValue(this, "y");
        BiomeBase[] z = ReflectionUtil.getProtectedValue(this, "z");

        k.setSeed((long) i * 341873128712L + (long) j * 132897987541L);
        byte[] abyte = new byte['\u8000'];

        this.a(i, j, abyte);
        z = p.getWorldChunkManager().getBiomeBlock(z, i * 16, j * 16, 16, 16);
        this.a(i, j, abyte, z);
        t.a(this, p, i, j, abyte);
        y.a(this, p, i, j, abyte);
        if (q) {
            w.a(this, p, i, j, abyte);
            v.a(this, p, i, j, abyte);
            u.a(this, p, i, j, abyte);
            x.a(this, p, i, j, abyte);
        }

        return abyte;
    }
}
