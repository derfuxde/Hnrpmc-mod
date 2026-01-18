package org.emil.hnrpmc.simpleclans.commands.contexts;

import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class DoubleContextResolver extends DoublePrimitiveContextResolver {
    public DoubleContextResolver(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<Double> getType() {
        return Double.class;
    }
}
