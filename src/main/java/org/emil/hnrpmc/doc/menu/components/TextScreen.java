package org.emil.hnrpmc.doc.menu.components;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.emil.hnrpmc.Hnrpmc;
import org.emil.hnrpmc.doc.HNDoc;
import org.emil.hnrpmc.doc.menu.MainMenu;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextScreen extends AbstractWidget {
    private final MainMenu parent;
    private final String content;
    private List<FormattedCharSequence> splitLines;

    public TextScreen(int width, int height, int x, int y, MainMenu parent, Component title, String content) {
        super(x, y, width, height, title);
        this.parent = parent;
        this.content = content;
        this.parsedLines = new ArrayList<>();
        parseAndSplit(content);
    }

    public TextScreen(TextScreen lastScreen, String content) {
        super(lastScreen.getX(), lastScreen.getY(), lastScreen.width, lastScreen.getHeight(), lastScreen.getMessage());
        this.parent = lastScreen.parent;
        this.content = content;
        this.parsedLines = new ArrayList<>();
        parseAndSplit(content);
    }

    public static ResourceLocation loadBase64AsTexture(String base64, String name) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);

            NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));

            DynamicTexture dynamicTexture = new DynamicTexture(image);
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Hnrpmc.MODID, "dynamic/" + name);

            Minecraft.getInstance().getTextureManager().register(location, dynamicTexture);

            return location;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getTextureHeight(ResourceLocation location) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);

        if (texture instanceof HttpTexture http) {
            return 0;
        }

        if (texture instanceof DynamicTexture dynamic) {
            NativeImage img = dynamic.getPixels();
            if (img != null) return img.getHeight();
        }

        return 0;
    }

    public int getTextureWidth(ResourceLocation location) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);

        if (texture instanceof HttpTexture http) {
            return 0;
        }

        if (texture instanceof DynamicTexture dynamic) {
            NativeImage img = dynamic.getPixels();
            if (img != null) return img.getWidth();
        }

        return 0;
    }

    private int[] getTextureDimensions(ResourceLocation location) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);

        if (texture instanceof HttpTexture http) {
            return new int[]{0, 0};
        }

        if (texture instanceof DynamicTexture dynamic) {
            NativeImage img = dynamic.getPixels();
            if (img != null) return new int[]{img.getWidth(), img.getHeight()};
        }

        return new int[]{0, 0};
    }

    private double scrollAmount = 0;
    private int totalContentHeight = 0; // Wird beim Parsen berechnet

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scrollAmount = Math.max(0, Math.min(scrollAmount - scrollY * 12, totalContentHeight - this.height + 20));
        return true;
    }

    private void renderScrollBar(GuiGraphics graphics) {
        if (totalContentHeight <= height) return;

        int scrollBarWidth = 4;
        int scrollBarX = getX() + width - scrollBarWidth - 2;

        // Berechne die Höhe und Position des Balkens
        float viewRatio = (float) height / totalContentHeight;
        int barHeight = (int) (height * viewRatio);
        int barTop = getY() + (int) ((scrollAmount / totalContentHeight) * height);

        graphics.fill(scrollBarX, barTop, scrollBarX + scrollBarWidth, barTop + barHeight, 0xFFAAAAAA);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x80000000);

        graphics.enableScissor(getX(), getY(), getX() + width, getY() + height);

        int currentY = (int) (getY() + 10 - scrollAmount);
        int startX = getX() + 10;

        parseAndSplit(content);
        for (FormattedLine line : parsedLines) {
            if (line.isImage()) {
                ResourceLocation rl = loadBase64AsTexture(line.getImage(), "test_image");
                int[] dims = getTextureDimensions(rl);
                int realW = dims[0];
                int realH = dims[1];

                if (realW > 0 && realH > 0) {
                    int renderWidth = this.width - 20;

                    float aspectRatio = (float) realH / realW;
                    int renderHeight = (int) (renderWidth * aspectRatio);

                    graphics.blit(rl, getX() + 10, currentY, 0, 0, renderWidth, renderHeight, renderWidth, renderHeight);

                    currentY += renderHeight + 10;
                } else {
                    graphics.drawString(parent.getMinecraft().font, "Lade Bild...", getX() + 10, currentY, 0x888888);
                    currentY += 12;
                }
            } else if (line.getText() != null) {
                graphics.drawString(parent.getMinecraft().font, line.getText(), startX, currentY, 0xFFFFFF);
                currentY += 12;
            }

            if (currentY > getY() + height - 10) break;
        }

        this.totalContentHeight = (currentY + (int)scrollAmount) - getY();

        graphics.disableScissor();

        renderScrollBar(graphics);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, "Text");
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHoveredOrFocused() && button == 0) {
            int startX = getX() + 10;
            int startY = getY() + 10;
            int relativeY = (int) (mouseY - startY);
            int relativeX = (int) (mouseX - startX);

            int lineIndex = relativeY / 12;

            if (lineIndex >= 0 && lineIndex < parsedLines.size()) {
                FormattedLine clickedLine = parsedLines.get(lineIndex);

                if (clickedLine.isUrl()) {
                    String[] words = formattedCharSequenceToString(clickedLine.getText()).split(" ");

                    int currentX = 0;

                    for (String word : words) {
                        int wordWidth = parent.getMinecraft().font.width(word);
                        int spaceWidth = parent.getMinecraft().font.width(" ");

                        if (relativeX >= currentX && relativeX <= (currentX + wordWidth)) {
                            if (clickedLine.getUrlholder().contains(word)) {
                                System.out.println("Geklicktes Wort/URL: " + word);

                                String url = clickedLine.getUrl();

                                if (url.contains(":") && url.split(":")[0].equals("doc")) {
                                    String path = "/" + url.split(":")[1];
                                    String displayName = Arrays.stream(url.split("/")).toList().getLast();

                                    for (Map.Entry<String, String> fileEntry : HNDoc.getInstance().getLoader().Files.entrySet()) {
                                        String fullPath = fileEntry.getKey();
                                        if (fullPath.replace("src/Doc", "").equals(path)) {
                                            try {
                                                //String fileContent = fileEntry.getValue();

                                                //Gson gson = HNDoc.getInstance().getGson();
                                                //Type listType = new TypeToken<Map<String, ?>>(){}.getType();
                                                //Map<String, ?> item = gson.fromJson(fileContent, listType);

                                                String itemPath = fullPath.replace("src/Doc", "");


                                                String jsonResponse = HNDoc.getInstance().getLoader().Files.get(itemPath);


                                                if (parent.currentText != null) {
                                                    parent.currentText.active = false;
                                                    parent.currentText.visible = false;
                                                }

                                                TextScreen ts = new TextScreen(this, jsonResponse);

                                                parent.render(ts);
                                                parent.currentText = ts;
                                            } catch (Exception e) {
                                                System.out.println("error " + e);
                                                return false;
                                            }


                                            return true;
                                        }
                                    }
                                } else {
                                    Util.getPlatform().openUri(url);
                                }


                                return true;
                            } else {
                                return false;
                            }
                        }

                        currentX += wordWidth + spaceWidth;

                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private List<FormattedLine> parsedLines = new ArrayList<>();

    public String formattedCharSequenceToString(FormattedCharSequence codes) {
        StringBuilder sb = new StringBuilder();
        codes.accept((index, style, codePoint) -> {
            sb.append((char) codePoint);
            return true;
        });
        return sb.toString();
    }


    private void parseAndSplit(String content) {
        this.parsedLines.clear();
        String text = content.replace("<br>", "\n")
                .replaceAll("<bb>(.*?)<\\.bb>", "§l$1§r")
                .replaceAll("<_>(.*?)<\\._>", "§n$1§r")
                .replaceAll("<->(.*?)<\\.->", "§m$1§r");
        text = convertHexTags(text);

        Pattern urlPattern = Pattern.compile("<URL>\\((.*?)\\)(.*?)<\\.URL>|<img>\\((.*?)\\)");
        Pattern imgPattern = Pattern.compile("");

        String[] paragraphs = text.split("\n");

        for (String p : paragraphs) {
            Matcher matcher = urlPattern.matcher(p);
            int lastEnd = 0;


            while (matcher.find()) {
                if (matcher.group(1) != null) {
//                    if (matcher.start() > lastEnd) {
//                        addTextLines(p.substring(lastEnd, matcher.start()), null, null);
//                    }

                    String pretext = matcher.group(1);
                    String link = matcher.group(1);
                    String label = matcher.group(2);
                    addTextLines(p.split("<URL>")[0] + " §9§n" + label + "§r", link, label);

                    lastEnd = matcher.end();
                }
                else if (matcher.group(3) != null) {
                    //if (matcher.start() == 0) {
                        String imgPath = matcher.group(3);
                        parsedLines.add(new FormattedLine(null, convertUrlToBase64(imgPath), null, null));
                        lastEnd = matcher.end();
                    //}
                }
            }

            if (lastEnd < p.length()) {
                addTextLines(p.substring(lastEnd), null, null);
            }
        }
    }

    private void addTextLines(String text, String url, String urlHolder) {
        List<FormattedCharSequence> lines = parent.getMinecraft().font.split(Component.literal(text), this.width - 20);
        for (FormattedCharSequence line : lines) {
            parsedLines.add(new FormattedLine(line, null, url, urlHolder));
        }
    }

    private String convertHexTags(String input) {
        java.util.regex.Pattern hexPattern = java.util.regex.Pattern.compile("<#([A-Fa-f0-9]{6})>(.*?)<\\.#>");
        java.util.regex.Matcher matcher = hexPattern.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            String text = matcher.group(2);
            StringBuilder mcHex = new StringBuilder("§x");
            for (char c : hex.toCharArray()) mcHex.append("§").append(c);
            matcher.appendReplacement(sb, mcHex.toString() + text + "§r");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String convertUrlToBase64(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            try (InputStream is = url.openStream();
                 BufferedInputStream bis = new BufferedInputStream(is)) {

                byte[] bytes = bis.readAllBytes();
                return Base64.getEncoder().encodeToString(bytes);//.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}