package com.lyra.addon.utils;

public class Coordinate {
    public int x;
    public int z;

    public Coordinate(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }
}
