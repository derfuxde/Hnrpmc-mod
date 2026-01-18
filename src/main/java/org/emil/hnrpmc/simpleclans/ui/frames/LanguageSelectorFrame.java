package org.emil.hnrpmc.simpleclans.ui.frames;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import org.emil.hnrpmc.simpleclans.ClanPlayer;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.emil.hnrpmc.simpleclans.language.LanguageResource;
import org.emil.hnrpmc.simpleclans.ui.InventoryDrawer;
import org.emil.hnrpmc.simpleclans.ui.SCComponent;
import org.emil.hnrpmc.simpleclans.ui.SCComponentImpl;
import org.emil.hnrpmc.simpleclans.ui.SCFrame;
import org.emil.hnrpmc.simpleclans.utils.Paginator;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.emil.hnrpmc.simpleclans.SimpleClans.lang;

public class LanguageSelectorFrame extends SCFrame {

    private final ClanPlayer clanPlayer;
    private final SimpleClans plugin;
    private final Paginator paginator;
    private final List<Locale> languages;

    public LanguageSelectorFrame(@Nullable SCFrame parent, @NotNull Player viewer) {
        super(parent, (ServerPlayer) viewer);
        plugin = SimpleClans.getInstance();
        clanPlayer = plugin.getClanManager().getCreateClanPlayer(viewer.getUUID());
        languages = LanguageResource.getAvailableLocales();
        paginator = new Paginator(getSize() - 9, languages);
    }

    @Override
    public @NotNull String getTitle() {
        Locale locale = clanPlayer.getLocale();
        if (locale == null) {
            locale = plugin.getSettingsManager().getLanguage();
        }
        return lang("gui.languageselector.title", getViewer(), locale.toLanguageTag());
    }

    @Override
    public int getSize() {
        return 3 * 9;
    }

    @Override
    public void createComponents() {
        addHeader();
        int slot = 9;
        for (int i = paginator.getMinIndex(); paginator.isValidIndex(i); i++) {
            Locale locale = languages.get(i);
            addLanguage(slot, locale);
            slot++;
        }
    }

    private void addLanguage(int slot, Locale locale) {
        SCComponent c = new SCComponentImpl.Builder(Items.PAPER)
                .withDisplayName(lang("gui.languageselector.language.title", getViewer(), locale.toLanguageTag().replace("-", "_")))
                .withSlot(slot).withLore(
                        Arrays.asList(lang("gui.languageselector.language.lore.left.click", getViewer()),
                                lang("gui.languageselector.language.lore.translation.status", getViewer(),
                                        LanguageResource.getTranslationStatus(locale)),
                                !locale.equals(Locale.ENGLISH) ?
                                        lang("gui.languageselector.language.lore.right.click", getViewer()) : ""))
                .build();
        c.setListener(ClickType.PICKUP, ClickAction.PRIMARY, () -> {
            clanPlayer.setLocale(locale);
            plugin.getStorageManager().updateClanPlayer(clanPlayer);
            InventoryDrawer.open(getViewer(), this);
        });
        if (!locale.equals(Locale.ENGLISH)) {
            c.setListener(ClickType.PICKUP, ClickAction.SECONDARY, () -> {
                getViewer().sendSystemMessage(Component.literal(lang("click.to.help.translating", getViewer(), getCrowdinLink(locale))));
                getViewer().closeContainer();
            });
        }
        add(c);
    }

    private String getCrowdinLink(@NotNull Locale locale) {
        String base = "https://crowdin.com/project/simpleclans/";
        //only known exception
        if (locale.equals(new Locale("uk", "UA"))) {
            return base + "uk";
        }
        if (locale.getLanguage().equalsIgnoreCase(locale.getCountry())) {
            return base + locale.getLanguage();
        }

        return base + locale.toLanguageTag();
    }

    public void addHeader() {
        for (int slot = 0; slot < 9; slot++) {
            if (slot == 2 || slot == 6 || slot == 7)
                continue;
            add(Components.getPanelComponent(slot));
        }
        add(Components.getBackComponent(getParent(), 2, getViewer()));

        add(Components.getPreviousPageComponent(6, this::previousPage, paginator, getViewer()));
        add(Components.getNextPageComponent(7, this::nextPage, paginator, getViewer()));
    }

    private void previousPage() {
        if (paginator.previousPage()) {
            updateFrame();
        }
    }

    private void nextPage() {
        if (paginator.nextPage()) {
            updateFrame();
        }
    }

    private void updateFrame() {
        InventoryDrawer.open(getViewer(), this);
    }
}
