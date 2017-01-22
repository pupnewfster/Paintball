package net.battlenexus.paintball.game.config.impl;

import net.battlenexus.paintball.game.config.ConfigParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class ArrayListConfig<T extends ConfigParser> extends ArrayList<T> implements ConfigParser {
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
    public void save(ArrayList<String> lines) {
        for (T item : this) {
            lines.add("<item>");
            GenericConfigParse.saveObject(item, lines);
            lines.add("</item>");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayListConfig<T> clone() {
        ArrayListConfig<T> new_list = (ArrayListConfig<T>) super.clone();
        new_list.addAll(this);
        return new_list;
    }
}