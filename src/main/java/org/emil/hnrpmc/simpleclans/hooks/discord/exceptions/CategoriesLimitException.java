package org.emil.hnrpmc.simpleclans.hooks.discord.exceptions;

public class CategoriesLimitException extends DiscordHookException {
  
    public CategoriesLimitException(String debugMessage, String messageKey) {
        super(debugMessage, messageKey);
    }
}
