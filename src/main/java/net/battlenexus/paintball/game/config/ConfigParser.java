package net.battlenexus.paintball.game.config;

import org.w3c.dom.NodeList;

import java.util.ArrayList;

public interface ConfigParser {
    void parse(NodeList childNodes);

    void save(ArrayList<String> lines);
}