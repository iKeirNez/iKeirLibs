package com.ikeirnez.ikeirlibs.bukkit.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

/**
 * A collection of utilities to aid in the process of Serializing.
 */
public class Serialization {

    private Serialization(){}

    /**
     * Serializes a location to a {@link java.util.Map} including yaw and pitch (if set).
     *
     * @param location the location to serialize
     * @return the serialized map value
     */
    public static Map<String, Object> serializeLocation(Location location){
        HashMap<String, Object> data = new HashMap<>();

        data.put("world", location.getWorld().getName());
        data.put("x", location.getX());
        data.put("y", location.getY());
        data.put("z", location.getZ());

        float yaw = location.getYaw();
        float pitch = location.getPitch();

        if (yaw != 0) data.put("yaw", yaw + "f");
        if (pitch != 0) data.put("pitch", pitch + "f");

        return data;
    }

    /**
     * Deserializes a location from a {@link java.util.Map} including yaw and pitch (if set).
     *
     * @param data the map to deserialize from
     * @return the deserialized location
     */
    public static Location deserializeLocation(Map<String, Object> data){
        World world = Bukkit.getWorld((String) data.get("world"));
        if (world == null) return null;

        double x = (double) data.get("x");
        double y = (double) data.get("y");
        double z = (double) data.get("z");

        float yaw = data.containsKey("yaw") ? Float.parseFloat((String) data.get("yaw")) : 0f;
        float pitch = data.containsKey("pitch") ? Float.parseFloat((String) data.get("pitch")) : 0f;

        return new Location(world, x, y, z, yaw, pitch);
    }

}
