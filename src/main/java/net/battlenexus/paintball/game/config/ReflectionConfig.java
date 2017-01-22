package net.battlenexus.paintball.game.config;

import net.battlenexus.paintball.Paintball;
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
import java.util.ArrayList;
import java.util.Formatter;

public abstract class ReflectionConfig {
    public static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

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
                                    parser.parse(item.getChildNodes());
                                    f.setAccessible(true);
                                    f.set(this, parser);
                                } else {
                                    if (String.class.isAssignableFrom(f.getType())) {
                                        f.setAccessible(true);
                                        f.set(this, item.getFirstChild().getNodeValue());
                                    } else if (Integer.class.isAssignableFrom(f.getType())) {
                                        f.setAccessible(true);
                                        f.set(this, Integer.parseInt(item.getFirstChild().getNodeValue()));
                                    } else if (Boolean.class.isAssignableFrom(f.getType())) {
                                        f.setAccessible(true);
                                        f.set(this, item.getFirstChild().getNodeValue().toLowerCase().contains("y"));
                                    } else if (Float.class.isAssignableFrom(f.getType())) {
                                        f.setAccessible(true);
                                        f.set(this, Float.parseFloat(item.getFirstChild().getNodeValue()));
                                    } else if (Double.class.isAssignableFrom(f.getType())) {
                                        f.setAccessible(true);
                                        f.set(this, Double.parseDouble(item.getFirstChild().getNodeValue()));
                                    } else if (Long.class.isAssignableFrom(f.getType())) {
                                        f.setAccessible(true);
                                        f.set(this, Long.parseLong(item.getFirstChild().getNodeValue()));
                                    } else if (Float.class.isAssignableFrom(f.getType())) {
                                        f.setAccessible(true);
                                        f.set(this, Float.parseFloat(item.getFirstChild().getNodeValue()));
                                    } else
                                        Paintball.INSTANCE.error("Cannot assign value for item \"" + item_name + "\"");
                                }
                            } else
                                Paintball.INSTANCE.error("Unknown mapConfig item \"" + item_name + "\"");
                            break;
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            throw new IOException("Error reading mapConfig!", e);
        } catch (SAXException e) {
            throw new IOException("Error parsing file!", e);
        } catch (IllegalAccessException e) {
            throw new IOException("Error setting value for mapConfig!", e);
        } catch (NoSuchMethodException e) {
            throw new IOException("Field did not have a default constructor!", e);
        } catch (InstantiationException e) {
            throw new IOException("Error creating new field!", e);
        } catch (InvocationTargetException e) {
            throw new IOException("Error invoking method to parse item!", e);
        }
    }

    public String[] save() {
        ArrayList<String> lines = new ArrayList<>();
        Field[] fields = getClass().getDeclaredFields();
        lines.add("<mapConfig>");
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
                    ConfigParser parser = (ConfigParser) obj;
                    parser.save(lines);
                    lines.add("</" + f.getName() + ">");
                } else {
                    String item_name = f.getName();
                    if (!Modifier.isTransient(f.getModifiers()))
                        lines.add("<" + item_name + ">" + obj.toString() + "</" + item_name + ">");
                }
            }
        }
        lines.add("</mapConfig>");
        return lines.toArray(new String[lines.size()]);
    }

    public void saveToFile(String map_name) throws IOException {
        String[] lines = save();
        File dir = Paintball.INSTANCE.getDataFolder();
        if (!dir.exists() && !dir.mkdir())
            throw new IOException("Error creating maps directory!");
        Formatter formatter = new Formatter(new FileWriter(new File(dir, map_name), true));
        for (String line : lines)
            formatter.out().append(line).append("\n");
        formatter.close();
    }

    private static boolean isConfigItem(Field field) {
        return field.getAnnotation(ConfigItem.class) != null;
    }
}