package net.battlenexus.paintball.game.config;

import org.w3c.dom.NodeList;

public interface ConfigOption {
    void parse(NodeList childNodes);

    void save(ConfigWriter writer);
}