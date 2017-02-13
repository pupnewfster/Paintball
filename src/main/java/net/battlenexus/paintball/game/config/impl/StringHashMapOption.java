package net.battlenexus.paintball.game.config.impl;

import net.battlenexus.paintball.game.config.ConfigOption;
import net.battlenexus.paintball.game.config.ConfigWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;

public class StringHashMapOption extends HashMap<String, String> implements ConfigOption {
    @Override
    public void parse(NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (!(childNodes.item(i) instanceof Element))
                    continue;
                Element item = (Element) childNodes.item(i);
                if (item.getNodeName().equals("item")) {
                    NodeList list = item.getChildNodes();
                    if (list != null && list.getLength() > 0) {
                        String key = null;
                        String value;
                        for (int ii = 0; ii < list.getLength(); ii++) {
                            if (!(list.item(ii) instanceof Element))
                                continue;
                            Element item2 = (Element) list.item(ii);
                            if (item2.getNodeName().equals("key"))
                                key = item2.getFirstChild().getNodeValue();
                            else if (item2.getNodeName().equals("value")) {
                                value = item2.getFirstChild().getNodeValue();
                                if (key != null)
                                    put(key, value);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void save(ConfigWriter writer) {
        for (String key : keySet()) {
            String value = get(key);


            writer.beginObject("item");

            writer.addConfig("key", key);
            writer.addConfig("value", value);

            writer.endObject();
        }
    }
}