package org.emil.hnrpmc.simpleclans.commands.conditions;

import co.aikar.commands.*;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import org.emil.hnrpmc.simpleclans.Clan;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.commands.ClanInput;
import org.jetbrains.annotations.NotNull;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public abstract class AlliedClanCondition extends AbstractParameterCondition<ClanInput> {

    public AlliedClanCondition(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<ClanInput> getType() {
        return ClanInput.class;
    }

    /**
     * Die Lösung: Nutze Wildcards oder den exakten Typ, den ACF vorgibt.
     * Oft ist das Problem, dass der CEC (CommandExecutionContext) selbst
     * den Issuer als ersten Parameter braucht.
     */
    @Override
    public void validateCondition(ConditionContext<CommandIssuer> context,
                                  CommandExecutionContext execContext, // Nutze Wildcards, um Bound-Fehler zu vermeiden
                                  ClanInput value) throws InvalidCommandArgument {

        CommandIssuer issuer = context.getIssuer();

        // Hole den Clan des Spielers
        Clan clan = plugin.getClanManager().getClanByPlayerUniqueId(issuer.getUniqueId());

        if (clan == null || !clan.isAlly(value.getClan().getTag())) {
            throw new ConditionFailedException(ChatFormatting.RED + lang("your.clans.are.not.allies", (ServerPlayer) issuer));
        }
    }

    @Override
    public @NotNull String getId() {
        return "allied_clan";
    }
}