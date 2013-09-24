package net.battlenexus.paintball.game.config;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.Team;
import org.bukkit.Location;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Formatter;

public class Config {
    public static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    @ConfigItem
    private Team blue_team;
    @ConfigItem
    private Team red_team;
    @ConfigItem
    private String map_name;
    @ConfigItem
    private int playerMax = 16;

    public Config() {
        blue_team = new Team();
        red_team = new Team();
    }

    public Config(Config toClone) {
        if (toClone == null)
            throw new InvalidParameterException("toClone cannot be null!");
        this.map_name = toClone.map_name;
        //this.map_config = new Map(toClone.map_config);
        this.blue_team = new Team(toClone.blue_team);
        this.red_team = new Team(toClone.red_team);
    }

    public void setMapName(String mapName) {
        this.map_name = mapName;
    }

    public void setTeamName(int team, String name) {
        if (team == 0) {
            blue_team.setTeamName(name);
        } else if (team == 1) {
            red_team.setTeamName(name);
        }
    }

    public void setTeamSpawn(int team, Location spawn) {
        if (team == 0) {
            blue_team.setSpawn(spawn);
        } else if (team == 1) {
            red_team.setSpawn(spawn);
        }
    }

    public Team getRedTeam() {
        return red_team;
    }

    public Team getBlueTeam() {
        return blue_team;
    }

    public String getMapName() {
        return map_name;
    }


    public void parseFile(File file) throws IOException {
        if (!file.exists())
            throw new IOException("File not found!");

        Field[] fields = getClass().getDeclaredFields();

        FileInputStream fin = new FileInputStream(file);
        try {
            DocumentBuilder db = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            Document dom = db.parse(fin);
            Element elm = dom.getDocumentElement();

            NodeList list = elm.getChildNodes();
            if (list != null && list.getLength() > 0) {
                for (int i = 0; i < list.getLength(); i++) {
                    if (!(list.item(i) instanceof Element))
                        continue;
                    Element item = (Element) list.item(i);
                    String item_name = item.getNodeName();

                    for (Field f : fields) {
                        if (f.getName().equals(item_name)) {
                            if (isConfigItem(f)) {
                                if (ConfigParser.class.isAssignableFrom(f.getType())) {
                                    ConfigParser parser = (ConfigParser) f.getType().getConstructor().newInstance();
                                    parser.parse(this, item.getChildNodes());
                                    f.set(this, parser);
                                } else {
                                    if (String.class.isAssignableFrom(f.getType())) {
                                        f.set(this, item.getFirstChild().getNodeValue());
                                    } else if (Integer.class.isAssignableFrom(f.getType())) {
                                        f.set(this, Integer.parseInt(item.getFirstChild().getNodeName()));
                                    } else if (Boolean.class.isAssignableFrom(f.getType())) {
                                        f.set(this, item.getFirstChild().getNodeValue().toLowerCase().contains("y"));
                                    } else {
                                        Paintball.INSTANCE.error("Cannot assign value for item \"" + item_name + "\"");
                                    }
                                }
                            } else {
                                Paintball.INSTANCE.error("Unknown config item \"" + item_name + "\"");
                            }
                            break;
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            throw new IOException("Error reading config!", e);
        } catch (SAXException e) {
            throw new IOException("Error parsing file!", e);
        } catch (IllegalAccessException e) {
            throw new IOException("Error setting value for config!", e);
        } catch (NoSuchMethodException e) {
            throw new IOException("Field did not have a default constructor!", e);
        } catch (InstantiationException e) {
            throw new IOException("Error creating new field!", e);
        } catch (InvocationTargetException e) {
            throw new IOException("Error invoking method to parse item!", e);
        }
    }

    public String[] save() {
        ArrayList<String> lines = new ArrayList<String>();
        Field[] fields = getClass().getDeclaredFields();
        lines.add("<config>");
        for (Field f : fields) {
            if (isConfigItem(f)) {
                Object obj;
                try {
                    obj = f.get(this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
                if (obj instanceof ConfigParser) {
                    lines.add("<" + f.getName() + ">");
                    ConfigParser parser = (ConfigParser)obj;
                    parser.save(lines);
                    lines.add("</" + f.getName() + ">");
                } else {
                    String item_name = f.getName();
                    if (!Modifier.isTransient(f.getModifiers())) {
                        lines.add("<" + item_name + ">" + obj.toString() + "</" + item_name +">");
                    }
                }
            }
        }
        lines.add("</config>");

        return lines.toArray(new String[lines.size()]);
    }

    public void saveToFile(String map_name) throws IOException {
        String[] lines = save();
        File dir = Paintball.INSTANCE.getDataFolder();
        if (!dir.exists()) {
            boolean result = dir.mkdir();
            if (!result)
                throw new IOException("Error creating maps directory!");
        }
        Formatter formatter = new Formatter(new FileWriter(new File(dir, map_name + ".xml"), true));
        for (String line : lines) {
            formatter.out().append(line).append("\n");
        }
        formatter.close();
    }

    private boolean isConfigItem(Field field) {
        return field.getAnnotation(ConfigItem.class) != null;
    }

    public int getPlayerMax() {
        return playerMax;
    }

    public void setPlayerMax(int playerMax) {
        this.playerMax = playerMax;
    }
}
