package net.battlenexus.paintball.game.config;

import org.w3c.dom.NodeList;

public interface ConfigParser {

    public void parse(Config config, NodeList childNodes);
}
