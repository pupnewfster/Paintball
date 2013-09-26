package net.battlenexus.paintball.game.config.impl;

import net.battlenexus.paintball.game.config.ConfigParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class ArrayListConfig<T extends ConfigParser> extends ArrayList<T> implements ConfigParser {
    @Override
    public void parse(NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (!(childNodes.item(i) instanceof Element))
                    continue;
                Element item = (Element)childNodes.item(i);
                if (item.getNodeName().equals("item")) {
                    NodeList list = item.getChildNodes();
                    if (list != null && list.getLength() > 0) {
                        String class_ = null;
                        for (int ii = 0; ii < list.getLength(); ii++) {
                            if (!(list.item(ii) instanceof Element))
                                continue;
                            Element item2 = (Element)list.item(ii);
                            if (item2.getNodeName().equals("class")) {
                                class_ = item2.getFirstChild().getNodeValue();
                            } else if (item2.getNodeName().equals("parse")) {
                                if (class_ == null)
                                    break;
                                try {
                                    Class<? extends ConfigParser> class_item = (Class<? extends ConfigParser>) Class.forName(class_); //weeeeaaak typing yum
                                    ConfigParser object = class_item.newInstance();
                                    NodeList children = item2.getChildNodes();
                                    object.parse(children);
                                    add((T) object);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void save(ArrayList <String> lines) {
        for (T item : this) {
            lines.add("<item>");
            lines.add("<class>" + item.getClass().getCanonicalName() + "</class>");
            lines.add("<parse>");
            item.save(lines);
            lines.add("</parse>");
            lines.add("</item>");
        }
    }
}
