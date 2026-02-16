package org.emil.hnrpmc.doc.menu.components;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.*;

public class FormattedLine {
    private final FormattedCharSequence text;
    private final String image;
    private final String url;
    private final String urlholder;

    public FormattedLine(FormattedCharSequence text, String image, String url, String urlholder) {
        this.text = text;
        this.image = image;
        this.url = url;
        this.urlholder = urlholder;
    }

    public boolean isImage() { return image != null; }
    public boolean isUrl() { return url != null; }
    public FormattedCharSequence getText() { return text; }
    public String getImage() { return image; }
    public String getUrl() { return url; }
    public String getUrlholder() {
        return urlholder;
    }
}