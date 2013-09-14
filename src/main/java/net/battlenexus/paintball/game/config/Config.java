package net.battlenexus.paintball.game.config;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.Team;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class Config {
    public static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    @ConfigItem
    private Team blue_team;
    @ConfigItem
    private Team red_team;
    @ConfigItem
    private String map_name;
    @ConfigItem
    private Map map_config;

    public Team getRedTeam() {
        return red_team;
    }

    public Team getBlueTeam() {
        return blue_team;
    }

    public String getMapName() {
        return map_name;
    }

    public Map getMapConfig() {
        return map_config;
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
                    Element item = (Element) list.item(i);
                    String item_name = item.getNodeName();

                    for (Field f : fields) {
                        if (f.getName().equals(item_name)) {
                            if (isConfigItem(f)) {
                                if (f.getDeclaringClass().isAssignableFrom(ConfigParser.class)) {
                                    ConfigParser parser = (ConfigParser) f.getDeclaringClass().getConstructor().newInstance();
                                    parser.parse(this, item.getChildNodes());
                                    f.set(this, parser);
                                } else {
                                    if (f.getDeclaringClass().isAssignableFrom(String.class)) {
                                        f.set(this, item.getFirstChild().getNodeValue());
                                    } else if (f.getDeclaringClass().isAssignableFrom(Integer.class)) {
                                        f.set(this, Integer.parseInt(item.getFirstChild().getNodeName()));
                                    } else if (f.getDeclaringClass().isAssignableFrom(Boolean.class)) {
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
                if (f.getDeclaringClass().isAssignableFrom(ConfigParser.class)) {
                    lines.add("<" + f.getName() + ">");
                    try {
                        ConfigParser parser = (ConfigParser) f.get(this);
                        if (parser != null) {
                            parser.save(lines);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    lines.add("</" + f.getName() + ">");
                } else {
                    String item_name = f.getName();
                    if (!Modifier.isTransient(f.getModifiers())) {
                        try {
                            lines.add("<" + item_name + ">" + f.get(this).toString() + "</" + item_name +">");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        lines.add("</config>");

        return lines.toArray(new String[lines.size()]);
    }

    private boolean isConfigItem(Field field) {
        return field.getAnnotation(ConfigItem.class) != null;
    }
}
