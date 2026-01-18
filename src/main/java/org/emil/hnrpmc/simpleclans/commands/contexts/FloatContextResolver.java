package org.emil.hnrpmc.simpleclans.commands.contexts;

import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class FloatContextResolver extends FloatPrimitiveContextResolver {
    public FloatContextResolver(@NotNull SimpleClans plugin) {
        super(plugin);
    }

    @Override
    public Class<Float> getType() {
        return Float.class;
    }
}
