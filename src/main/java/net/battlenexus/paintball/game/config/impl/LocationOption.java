package net.battlenexus.paintball.game.config.impl;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.game.config.ConfigOption;
import net.battlenexus.paintball.game.config.ConfigWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LocationOption implements ConfigOption {
    private Location location;

    public LocationOption(Location location) {
        this.location = location;
    }

    @Override
    public void parse(NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            String world_name;
            World world = null;
            double x = 0, y = 0, z = 0, yaw = 0, pitch = 0;
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (!(childNodes.item(i) instanceof Element))
                    continue;
                Element item = (Element) childNodes.item(i);
                if (item.getNodeName().equals("x"))
                    x = Double.parseDouble(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("y"))
                    y = Double.parseDouble(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("z"))
                    z = Double.parseDouble(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("yaw"))
                    yaw = Double.parseDouble(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("pitch"))
                    pitch = Double.parseDouble(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("world")) {
                    world_name = item.getFirstChild().getNodeValue();
                    world = Bukkit.getWorld(world_name);
                    if (world == null)
                        world = Bukkit.createWorld(new WorldCreator(world_name));
                    if (world == null)
                        world = Paintball.INSTANCE.paintball_world;
                }
            }
            location = new Location(world, x, y, z, (float) yaw, (float) pitch);
        }
    }

    @Override
    public void save(ConfigWriter writer) {
        writer.addConfig("x", location.getX());
        writer.addConfig("y", location.getY());
        writer.addConfig("z", location.getZ());
        writer.addConfig("world", location.getWorld().getName());
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}