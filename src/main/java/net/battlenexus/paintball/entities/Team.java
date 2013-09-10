package net.battlenexus.paintball.entities;

import net.battlenexus.paintball.game.config.Config;
import net.battlenexus.paintball.game.config.ConfigParser;
import org.bukkit.Location;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Team implements ConfigParser {
    private String team_name;
    private Location spawn;


    @Override
    public void parse(Config config, NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element item = (Element)childNodes.item(0);

            }
        }
    }
}
