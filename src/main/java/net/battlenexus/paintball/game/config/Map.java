package net.battlenexus.paintball.game.config;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class Map implements ConfigParser {
    private int spawnx;
    private int spawny;
    private int spawnz;
    private int rot;
    private int yaw;

    public int getSpawnX() {
        return spawnx;
    }

    public int getSpawnY() {
        return spawny;
    }

    public int getSpawnZ() {
        return spawnz;
    }

    public int getRotation() {
        return rot;
    }

    public int getYaw() {
        return yaw;
    }

    public void setSpawnX(int spawnx) {
        this.spawnx = spawnx;
    }

    public void setSpawnY(int spawny) {
        this.spawny = spawny;
    }

    public void setSpawnZ(int spawnz) {
        this.spawnz = spawnz;
    }

    public void setRotation(int rot) {
        this.rot = rot;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }


    @Override
    public void parse(Config config, NodeList childNodes) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Element elm = (Element)childNodes.item(i);
            if (elm.getNodeName().equals("spawnx")) {
                spawnx = Integer.parseInt(elm.getFirstChild().getNodeValue());
            } else if (elm.getNodeName().equals("spawny")) {
                spawny = Integer.parseInt(elm.getFirstChild().getNodeValue());
            } else if (elm.getNodeName().equals("spawnz")) {
                spawny = Integer.parseInt(elm.getFirstChild().getNodeValue());
            } else if (elm.getNodeName().equals("rot")) {
                spawny = Integer.parseInt(elm.getFirstChild().getNodeValue());
            } else if (elm.getNodeName().equals("yaw")) {
                spawny = Integer.parseInt(elm.getFirstChild().getNodeValue());
            }
        }
    }

    @Override
    public void save(ArrayList<String> lines) {
        lines.add("<spawnx>" + spawnx + "</spawnx>");
        lines.add("<spawny>" + spawny + "</spawny>");
        lines.add("<spawnz>" + spawnz + "</spawnz>");
        lines.add("<rot>" + rot + "</rot>");
        lines.add("<yaw>" + yaw + "</yaw>");
    }
}
