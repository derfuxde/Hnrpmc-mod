package org.emil.hnrpmc.simpleclans.managers;

public class ConditionRule {
    private final String condition;
    private final String message;

    public ConditionRule(String rawRule) {
        if (rawRule.contains(";")) {
            String[] parts = rawRule.split(";", 2);
            this.condition = parts[0];
            this.message = parts[1];
        } else {
            this.condition = "default";
            this.message = rawRule;
        }
    }

    public boolean matches(double value) {
        if (condition.equals("default")) return true;

        try {
            if (condition.startsWith("<")) {
                return value < Double.parseDouble(condition.substring(1));
            } else if (condition.startsWith(">")) {
                return value > Double.parseDouble(condition.substring(1));
            } else if (condition.startsWith("<=")) {
                return value <= Double.parseDouble(condition.substring(2));
            } else if (condition.startsWith(">=")) {
                return value >= Double.parseDouble(condition.substring(2));
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    public String getMessage() {
        return message;
    }
}