package net.battlenexus.paintball.game.config.impl;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.game.config.ConfigParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class LocationConfig implements ConfigParser {
    public Location location;

    @Override
    public void parse(NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            String world_name;
            World world = null;
            double x = 0, y = 0, z = 0, yaw = 0, pitch = 0;
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (!(childNodes.item(i) instanceof Element))
                    continue;
                Element item = (Element)childNodes.item(i);
                if (item.getNodeName().equals("x")) {
                    x = Double.parseDouble(item.getFirstChild().getNodeValue());
                } else if (item.getNodeName().equals("y")) {
                    y = Double.parseDouble(item.getFirstChild().getNodeValue());
                } else if (item.getNodeName().equals("z")) {
                    z = Double.parseDouble(item.getFirstChild().getNodeValue());
                } else if (item.getNodeName().equals("yaw")) {
                    yaw = Double.parseDouble(item.getFirstChild().getNodeValue());
                } else if (item.getNodeName().equals("pitch")) {
                    pitch = Double.parseDouble(item.getFirstChild().getNodeValue());
                } else if (item.getNodeName().equals("world")) {
                    world_name = item.getFirstChild().getNodeValue();
                    world = Bukkit.getServer().createWorld(new WorldCreator(world_name));
                    if (world == null) {
                        world = Paintball.INSTANCE.paintball_world;
                    }
                }
            }
            location = new Location(world, x, y, z, (float)yaw, (float)pitch);
        }
    }

    @Override
    public void save(ArrayList<String> lines) {
        lines.add("<x>" + location.getX() + "</x>");
        lines.add("<y>" + location.getY() + "</y>");
        lines.add("<z>" + location.getZ() + "</z>");
        lines.add("<world>" + location.getWorld().getName() + "</world>");
    }
}
