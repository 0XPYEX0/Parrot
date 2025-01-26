package me.xpyex.plugin.parrot.mirai.module.skripthub;

import cn.hutool.json.JSONObject;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import lombok.Getter;

/**
 * @author msg_dw
 * 我爱你
 */
public class AwtSkriptDocBuilder {

    private static final Color BACKGROUND_COLOR = new Color(227, 227, 227);
    private static final Color DESC_BACKGROUND_COLOR = new Color(249, 249, 249);
    private static final Color DESC_TEXT_COLOR = new Color(112, 112, 112);
    private static final Color CODE_BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color CODE_TEXT_COLOR = new Color(220, 220, 220);
    private static final Font TITLE_FONT = new Font("微软雅黑", Font.PLAIN, 32);
    private static final Font CODE_FONT = new Font("微软雅黑", Font.PLAIN, 14);

    private final List<JSONObject> syntaxList = new ArrayList<>();

    public AwtSkriptDocBuilder syntax(JSONObject jsonObject) {
        syntaxList.add(jsonObject);
        return this;
    }

    public BufferedImage build() {
        Map<Card, Integer> cards = createCard();
        int height = 20;
        for (Integer value : cards.values()) {
            height += value + 20;
        }
        BufferedImage bufferedImage = new BufferedImage(800, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setBackground(BACKGROUND_COLOR);
        graphics.clearRect(0, 0, 800, height);
        int i = 20;
        for (Map.Entry<Card, Integer> entry : cards.entrySet()) {
            Integer cardHeight = entry.getValue();
            Graphics2D cardGraphics = (Graphics2D) graphics.create(20, i, 760, cardHeight);
            entry.getKey().draw(cardGraphics, 760, cardHeight);
            i += cardHeight + 20;
        }
        graphics.dispose();
        return bufferedImage;
    }

    private Map<Card, Integer> createCard() {
        BufferedImage bufferedImage = new BufferedImage(1, 1, 1);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setFont(TITLE_FONT);
        FontMetrics titleFontMetrics = graphics.getFontMetrics();
        graphics.setFont(CODE_FONT);
        FontMetrics codeFontMetrics = graphics.getFontMetrics();
        HashMap<Card, Integer> result = new LinkedHashMap<>();
        for (JSONObject jsonObject : syntaxList) {
            Card card = new Card(codeFontMetrics, jsonObject);
            result.put(card, card.calcHeight(titleFontMetrics, codeFontMetrics));
        }
        return result;
    }

    private static List<String> splitLine(FontMetrics fontMetrics, String text, int width) {
        List<String> result = new ArrayList<>();
        Scanner scanner = new Scanner(text);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (fontMetrics.stringWidth(line) <= width) {
                result.add(line);
                continue;
            }
            List<String> temp = new ArrayList<>();
            StringBuilder stringBuilder = new StringBuilder();
            int index = 0, lastSpace = 0;
            char[] charArray = line.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                if (charArray[i] == ' ') {
                    lastSpace = i;
                }
                stringBuilder.append(charArray[i]);
                if (fontMetrics.stringWidth(stringBuilder.toString()) > width) {
                    temp.add(line.substring(index, lastSpace).trim());
                    stringBuilder = new StringBuilder();
                    index = lastSpace;
                }
            }
            if (index != line.length() - 1) {
                temp.add(line.substring(index).trim());
            }
            // 换行结尾加上 ¬ 符号 表示换行
            for (int i = 0; i < temp.size() - 1; i++) {
                temp.set(i, temp.get(i) + "¬");
            }
            result.addAll(temp);
        }
        return result;
    }

    public static AwtSkriptDocBuilder builder() {
        return new AwtSkriptDocBuilder();
    }

    @Getter
    public enum SyntaxType {
        EVENT(Color.decode("#a763ff")),
        CONDITION(Color.decode("#ff3d3d")),
        EFFECT(Color.decode("#0178ff")),
        EXPRESSION(Color.decode("#0de505")),
        TYPE(Color.decode("#f39c12")),
        FUNCTION(Color.decode("#b4b4b4")),
        SECTION(Color.decode("#1abc9c")),
        STRUCTURE(Color.decode("#e056fd"));
        private final Color color;

        SyntaxType(Color color) {
            this.color = color;
        }
    }

    public static class Card {
        private final SyntaxType type;
        private final String title;
        private final String adden;
        private final List<String> syntaxPattern;
        private final List<String> description;

        public Card(FontMetrics fontMetrics, JSONObject syntaxJson) {
            this.type = SyntaxType.valueOf(syntaxJson.getStr("syntax_type").toUpperCase());
            this.title = syntaxJson.getStr("title");
            String addon = syntaxJson.getJSONObject("addon").getStr("name");
            String addonVersion = syntaxJson.getStr("compatible_addon_version");
            if (!addonVersion.isEmpty()) {
                addon += "-v" + addonVersion;
            }
            this.adden = addon;
            this.syntaxPattern = splitLine(fontMetrics, syntaxJson.getStr("syntax_pattern"), 730);
            this.description = splitLine(fontMetrics, syntaxJson.getStr("description"), 730);
        }

        public int calcHeight(FontMetrics titleFontMetrics, FontMetrics codeFontMetrics) {
            return titleFontMetrics.getHeight() + codeFontMetrics.getHeight() * syntaxPattern.size() + codeFontMetrics.getHeight() * description.size() + 64;
        }

        public void draw(Graphics2D graphics, int width, int height) {
            FontMetrics fontMetrics;
            // 绘制背景
            graphics.setBackground(type.getColor());
            graphics.clearRect(0, 0, 16, height);
            graphics.setBackground(DESC_BACKGROUND_COLOR);
            graphics.clearRect(16,0, width - 16, height);
            // 绘制标题
            graphics.setFont(TITLE_FONT);
            graphics.setColor(Color.BLACK);
            fontMetrics = graphics.getFontMetrics();
            int titleHeight = fontMetrics.getHeight();
            graphics.drawString(title, 23, fontMetrics.getAscent() + 7);
            // 绘制依赖版本
            int addonWidth = fontMetrics.stringWidth(adden);
            graphics.setColor(DESC_TEXT_COLOR);
            graphics.drawString(adden, width - addonWidth - 7, fontMetrics.getAscent() + 7);
            // 绘制语法
            graphics.setFont(CODE_FONT);
            graphics.setColor(CODE_TEXT_COLOR);
            fontMetrics = graphics.getFontMetrics();
            graphics.setBackground(CODE_BACKGROUND_COLOR);
            graphics.clearRect(16, titleHeight + 14, width - 16, fontMetrics.getHeight() * syntaxPattern.size() + 16);
            for (int i = 0; i < syntaxPattern.size(); i++) {
                graphics.drawString(syntaxPattern.get(i), 23, titleHeight + 21 + fontMetrics.getAscent() + fontMetrics.getHeight() * i);
            }
            // 绘制描述
            int descPos = titleHeight + 48 + fontMetrics.getHeight() * syntaxPattern.size();
            graphics.setColor(DESC_TEXT_COLOR);
            for (int i = 0; i < description.size(); i++) {
                graphics.drawString(description.get(i), 23, descPos + fontMetrics.getAscent() + fontMetrics.getHeight() * i);
            }

            graphics.dispose();
        }
    }
}
