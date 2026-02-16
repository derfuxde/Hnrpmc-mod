package org.emil.hnrpmc.doc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.Hnrpmod;
import org.emil.hnrpmc.doc.managers.Loader;
import org.emil.hnrpmc.hnclaim.HNClaims;
import org.slf4j.Logger;

public class HNDoc extends Hnrpmod {

    private static HNDoc instance;
    public static final Logger LOGGER = LogUtils.getLogger();

    private final Loader loader;

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();


    public HNDoc(IEventBus modEventBus) {
        instance = this;

        this.loader = new Loader();
    }

    public static HNDoc getInstance() {
        return instance;
    }

    public Loader getLoader() {
        return loader;
    }

    public Gson getGson() {
        return gson;
    }
}
