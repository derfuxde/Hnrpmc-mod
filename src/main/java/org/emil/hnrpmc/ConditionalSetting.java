package org.emil.hnrpmc;

import java.util.List;

public class ConditionalSetting {
    private final String name;
    private final String placeholder;
    private final List<String> rules;

    public ConditionalSetting(String name, String placeholder, List<String> rules) {
        this.name = name;
        this.placeholder = placeholder;
        this.rules = rules;
    }

    // Getter
    public String getName() { return name; }
    public String getPlaceholder() { return placeholder; }
    public List<String> getRules() { return rules; }
}