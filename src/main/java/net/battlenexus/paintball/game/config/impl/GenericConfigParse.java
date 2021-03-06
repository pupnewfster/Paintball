package net.battlenexus.paintball.game.config.impl;

import net.battlenexus.paintball.game.config.ConfigOption;
import net.battlenexus.paintball.game.config.ConfigWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.Objects;

public class GenericConfigParse {
    private static LinkedList<String> pluginPackages = new LinkedList<>();

    static {
        pluginPackages.add("net.battlenexus.paintball.game.config.impl");
    }

    public static void addConfigPackage(String packageName) {
        pluginPackages.add(packageName);
    }

    public static ConfigOption findConfig(String name) {
        return pluginPackages.stream().map(packagePath -> tryGetParser(packagePath + "." + name)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    private static ConfigOption tryGetParser(String class_) {
        Class<? extends ConfigOption> configParserClass; //weeeeaaak typing yum
        try {
            configParserClass = (Class<? extends ConfigOption>) Class.forName(class_);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        ConfigOption object;
        try {
            object = configParserClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        return object;
    }

    @SuppressWarnings("unchecked")
    public static ConfigOption parseObject(NodeList list) {
        if (list != null && list.getLength() > 0) {
            String class_ = null;
            for (int ii = 0; ii < list.getLength(); ii++) {
                if (!(list.item(ii) instanceof Element))
                    continue;
                Element item2 = (Element) list.item(ii);
                if (item2.getNodeName().equals("type"))
                    class_ = item2.getFirstChild().getNodeValue();
                else if (item2.getNodeName().equals("options")) {
                    if (class_ == null)
                        break;

                    ConfigOption parser = findConfig(class_);
                    if (parser == null)
                        break;

                    NodeList children = item2.getChildNodes();
                    parser.parse(children);
                    return parser;
                }
            }
        }
        return null;
    }

    public static void saveObject(ConfigOption object, ConfigWriter currentConfig) {
        currentConfig.addConfig("type", object.getClass().getSimpleName());

        currentConfig.beginObject("options");
        object.save(currentConfig);
        currentConfig.endObject();
    }
}