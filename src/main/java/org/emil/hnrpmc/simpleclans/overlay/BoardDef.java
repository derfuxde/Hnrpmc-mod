package org.emil.hnrpmc.simpleclans.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record BoardDef(String id, String when, String title, List<String> lines) {

    public static BoardDef from(Object o) {
        Map<?, ?> m = (Map<?, ?>) o;

        Object idObj = m.get("id");
        Object whenObj = m.get("conditions");
        Object titleObj = m.get("title");

        String id = idObj != null ? String.valueOf(idObj) : "unnamed";
        String when = whenObj != null ? String.valueOf(whenObj) : "true";
        String title = titleObj != null ? String.valueOf(titleObj) : " ";

        Object rawLines = m.get("lines");
        List<String> lines = new ArrayList<>();
        if (rawLines instanceof List<?> l) {
            for (Object x : l) lines.add(String.valueOf(x));
        }

        return new BoardDef(id, when, title, lines);
    }
}
