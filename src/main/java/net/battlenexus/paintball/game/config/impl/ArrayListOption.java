package net.battlenexus.paintball.game.config.impl;

import net.battlenexus.paintball.game.config.ConfigOption;
import net.battlenexus.paintball.game.config.ConfigWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class ArrayListOption<T extends ConfigOption> extends ArrayList<T> implements ConfigOption {
    @SuppressWarnings("unchecked")
    @Override
    public void parse(NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (!(childNodes.item(i) instanceof Element))
                    continue;
                Element item = (Element) childNodes.item(i);
                if (item.getNodeName().equals("item")) {
                    NodeList list = item.getChildNodes();
                    T object = (T) GenericConfigParse.parseObject(list);
                    add(object);
                }
            }
        }
    }

    @Override
    public void save(ConfigWriter configWriter) {
        for (T item : this) {
            configWriter.beginObject("item");
            GenericConfigParse.saveObject(item, configWriter);
            configWriter.endObject();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayListOption<T> clone() {
        ArrayListOption<T> new_list = (ArrayListOption<T>) super.clone();
        new_list.addAll(this);
        return new_list;
    }
}