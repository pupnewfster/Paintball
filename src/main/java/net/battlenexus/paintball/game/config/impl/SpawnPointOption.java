package net.battlenexus.paintball.game.config.impl;

import net.battlenexus.paintball.game.config.ConfigOption;
import net.battlenexus.paintball.game.config.ConfigWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SpawnPointOption implements ConfigOption {
    private LocationOption position;
    private int team;
    private boolean isStartPoint;
    private boolean aiSpawn;

    @Override
    public void parse(NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            int team = 0;
            boolean isStartPoint = false, aiSpawn = false;
            LocationOption location = null;
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (!(childNodes.item(i) instanceof Element))
                    continue;
                Element item = (Element) childNodes.item(i);
                if (item.getNodeName().equals("team"))
                    team = Integer.parseInt(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("startSpawn"))
                    isStartPoint = item.getFirstChild().getNodeValue().equalsIgnoreCase("true");
                else if (item.getNodeName().equals("aiSpawn"))
                    aiSpawn = item.getFirstChild().getNodeValue().equalsIgnoreCase("true");
                else if (item.getNodeName().equals("location"))
                    location = (LocationOption) GenericConfigParse.parseObject(item.getChildNodes());
            }

            this.team = team;
            this.isStartPoint = isStartPoint;
            this.aiSpawn = aiSpawn;
            this.position = location;
        }
    }

    @Override
    public void save(ConfigWriter writer) {
        writer.addConfig("team", team);
        writer.addConfig("startSpawn", isStartPoint);
        writer.addConfig("aiSpawn", aiSpawn);

        writer.beginObject("location");
        GenericConfigParse.saveObject(position, writer);
        writer.endObject();
    }

    public LocationOption getPosition() {
        return position;
    }

    public void setPosition(LocationOption position) {
        this.position = position;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public boolean isStartPoint() {
        return isStartPoint;
    }

    public void setStartPoint(boolean startPoint) {
        isStartPoint = startPoint;
    }

    public boolean isAiSpawn() {
        return aiSpawn;
    }

    public void setAiSpawn(boolean aiSpawn) {
        this.aiSpawn = aiSpawn;
    }
}
