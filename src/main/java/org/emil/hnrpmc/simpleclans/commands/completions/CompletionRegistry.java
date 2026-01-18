package org.emil.hnrpmc.simpleclans.commands.completions;

import org.emil.hnrpmc.simpleclans.SimpleClans;

import java.util.HashMap;
import java.util.Map;

public final class CompletionRegistry {

    private final Map<String, SCCompletion> byId = new HashMap<>();

    public CompletionRegistry(SimpleClans plugin) {
        register(new ClansCompletion(plugin));
        register(new OtherClansCompletion(plugin));
        register(new VerifiedClansCompletion(plugin));
        register(new UnverifiedClansCompletion(plugin));
        register(new AlliedClansCompletion(plugin));
        register(new WarringClansCompletion(plugin));
        register(new ClanMembersCompletion(plugin));
        register(new OnlineClanMembersCompletion(plugin));
        register(new ClanLeadersCompletion(plugin));
        register(new ClanRanksCompletion(plugin));
        register(new RankPermissionsCompletion(plugin));
        register(new PlayersCompletion(plugin));
        register(new OnlinePlayersCompletion(plugin));
        register(new BannedPlayersCompletion(plugin));
        register(new DisallowedTagsCompletion(plugin));
        register(new LocalesCompletion(plugin));
        register(new LandIdsCompletion(plugin));
    }

    private void register(SCCompletion completion) {
        byId.put(completion.getId(), completion);
    }

    public SCCompletion get(String id) {
        return byId.get(id);
    }
}
