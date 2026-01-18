package org.emil.hnrpmc.simpleclans.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import org.apache.logging.log4j.Level;
import org.emil.hnrpmc.simpleclans.proxy.BungeeManager;
import org.emil.hnrpmc.simpleclans.utils.ObjectUtils;
import org.jetbrains.annotations.Nullable;

import static java.util.logging.Level.INFO;
import static org.emil.hnrpmc.simpleclans.SimpleClans.debug;

public abstract class Update<T> extends MessageListener {

    public Update(BungeeManager bungee) {
        super(bungee);
    }

    protected abstract Class<T> getType();

    protected abstract @Nullable T getCurrent(T t);

    protected abstract void insert(T t);

    @Override
    public final void accept(ByteArrayDataInput data) {
        T t = bungee.getGson().fromJson(data.readUTF(), getType());
        T current = getCurrent(t);
        if (current == null) {
            insert(t);
            debug(String.format("Inserted %s", t));
            return;
        }
        try {
            ObjectUtils.updateFields(t, current);
        } catch (IllegalAccessException e) {
            bungee.getPlugin().getLogger().info( String.format("An error happened while updating %s", t), e);
        }
        debug(String.format("Updated %s", t));
    }
}
