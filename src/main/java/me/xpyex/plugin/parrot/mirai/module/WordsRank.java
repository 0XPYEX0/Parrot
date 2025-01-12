package me.xpyex.plugin.parrot.mirai.module;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.FontWeight;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.filter.Filter;
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer;
import com.kennycason.kumo.palette.ColorPalette;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandArguments;
import me.xpyex.plugin.parrot.mirai.core.command.CommandBus;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.GroupParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.StringUtil;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageContent;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

public class WordsRank extends Module {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final WeakHashMap<Long, File> TEXT_FILE_CACHE = new WeakHashMap<>();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
    private static final WeakHashMap<Long, Long> GROUP_CDs = new WeakHashMap<>();  //GroupID, System.currentTimeMillis()
    private static final int COOLDOWN = 300;  // 5分钟
    private static JSONObject CONFIG = new JSONObject().set("Groups", new JSONArray());  // {"Groups": [123, 456]}
    private File CONFIG_FILE;

    @Override
    public void register() throws Throwable {
        CONFIG_FILE = new File(getConfigFolder(), "config.json");
        reload();

        registerCommand(Group.class,
            CommandNode.<Group>of((source, sender, arguments) -> {
                    new CommandMenu(arguments)
                        .add("today", "今日词云")
                        .add("yesterday", "昨日词云")
                        .add("date <yyyy-MM-dd>", "某日词云")
                        .add("enable <GroupID>", "在指定群启用词云")
                        .add("disable <GroupID>", "在指定群禁用词云")
                        .send(source);
                })
                .child(CommandNode.of((source, sender, arguments) -> {
                    CommandBus.dispatchCommand(source, sender, CommandArguments.of(arguments.getLabel(0), "date", DATE_FORMAT.format(new Date())));
                }), "today")
                .child(CommandNode.of((source, sender, arguments) -> {
                    CommandBus.dispatchCommand(source, sender, CommandArguments.of(arguments.getLabel(0), "date", DATE_FORMAT.format(new Date(System.currentTimeMillis() - 86400000))));
                }), "yesterday")
                .child(CommandNode.<Group>of((source, sender, arguments) -> source.sendMessage("请填写日期"))
                           .executableCheck((source, sender) -> {
                               if (GROUP_CDs.getOrDefault(source.getId(), System.currentTimeMillis()) - System.currentTimeMillis() < COOLDOWN * 1000) {
                                   source.sendMessage("冷却中");
                                   return false;
                               }
                               if (CONFIG.getJSONArray("Groups").contains(source.getId())) return true;
                               source.sendMessage("当前群未启用词云记录");
                               return false;
                           })
                           .notMatchedArg((source, sender, arguments) -> {
                               try {
                                   source.sendMessage("正在生成词云...");
                                   GROUP_CDs.put(source.getId(), System.currentTimeMillis());
                                   Date date = DATE_FORMAT.parse(arguments.getArgument(0));
                                   source.sendMessage(generateImage(source.getContactAsGroup(), date));
                               } catch (ParseException ignored) {
                                   source.sendMessage("日期格式错误，请按照 yyyy-MM-dd 格式填写");
                               }
                           })
                    , "date")
                .child(CommandNode.<Group>of((source, sender, arguments) -> {
                    arguments.getArgument(0, GroupParser.class, Group.class)
                        .ifPresentOrElse(group -> {
                            boolean isEnable = "enable".equalsIgnoreCase(arguments.getLabelReverse(0));
                            boolean result = modifyConfig(group.getId(), isEnable);
                            source.sendMessage(result ? "已在群 <" + group.getId() + "> " + (isEnable ? "启用" : "禁用") + "词云记录" : "无需重复操作，记录未修改");
                        }, () -> source.sendMessage("请填写正确的群号"));
                }).executableCheck((source, sender) -> {
                    if (!sender.hasPerm(getName() + ".admin")) {
                        source.sendMessage("缺少权限节点: " + getName() + ".admin");
                        return false;
                    }
                    return true;
                }), "enable", "disable")
            , "词云", "wordRank", "wordsRank", "wordsCloud", "wordCloud", "words");

        listenEvent(GroupMessageEvent.class, event -> {
            JSONArray groups = new JSONArray(CONFIG.getJSONArray("Groups"));
            if (groups.isEmpty()) return;
            MessageContent plainText = event.getMessage().get(PlainText.Key);
            if (plainText == null) return;

            if (groups.contains(event.getGroup().getId())) {
                File todayWordsFile = getGroupWordsFile(event.getGroup(), new Date());
                Files.writeString(todayWordsFile.toPath(),
                    Files.readString(todayWordsFile.toPath(), StandardCharsets.UTF_8) + System.lineSeparator() + plainText.contentToString(),
                    StandardCharsets.UTF_8);
            }
        });

        executeOnce(BotOnlineEvent.class, event -> {
            modifyConfig(0L, true);
            modifyConfig(0L, false);
        });
    }

    private boolean modifyConfig(long id, boolean isEnable) {
        HashSet<Long> groups = new HashSet<>(CONFIG.getJSONArray("Groups").toList(Long.class));
        boolean result = isEnable ? groups.add(id) : groups.remove(id);
        CONFIG.set("Groups", new JSONArray(groups));
        try {
            Files.writeString(CONFIG_FILE.toPath(), CONFIG.toStringPretty(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private void reload() throws Throwable {
        if (!CONFIG_FILE.exists()) {
            Files.writeString(CONFIG_FILE.toPath(), CONFIG.toStringPretty(), StandardCharsets.UTF_8);
        }

        CONFIG = new JSONObject(info(Files.readString(CONFIG_FILE.toPath(), StandardCharsets.UTF_8)));
    }

    private File getGroupWordsFile(Group group, Date date) {
        ValueUtil.notNull("’group‘ ,'date‘ must not be null", group, date);
        return getGroupWordsFile(group.getId(), date);
    }

    private File getGroupWordsFile(long id, Date date) {
        ValueUtil.notNull("'date' must not be null", date);
        return TEXT_FILE_CACHE.computeIfAbsent(id, g -> {
            File file = new File(getDataFolder(), "texts/" + id + "/" + DATE_FORMAT.format(date) + ".txt");
            if (!file.exists()) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return file;
        });
    }

    private Image generateImage(Group group, Date date) throws Throwable {
        File cacheImageFile = File.createTempFile("WordsRank-" + TIME_FORMAT.format(new Date()) + "-for[" + DATE_FORMAT.format(date) + "]", ".png");
        FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setCharacterEncoding("UTF-8");
        frequencyAnalyzer.setWordTokenizer(new ChineseWordTokenizer());
        frequencyAnalyzer.addFilter(new Filter() {
            @Override
            public boolean test(String s) {
                return !StringUtil.startsWithIgnoreCaseOr(s, "@");
            }
        });
        frequencyAnalyzer.setMinWordLength(2);
        frequencyAnalyzer.setWordFrequenciesToReturn(500);
        frequencyAnalyzer.setStopWords(Set.of("[图片]", "[动画表情]"));
        List<WordFrequency> frequencies = frequencyAnalyzer.load(getGroupWordsFile(group, date));


        Dimension dimension = new Dimension(1920, 1080);
        WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
        wordCloud.setPadding(2);
        wordCloud.setBackground(new RectangleBackground(dimension));
        wordCloud.setBackgroundColor(Color.WHITE);
        wordCloud.setColorPalette(new ColorPalette(Color.RED, Color.GREEN, Color.ORANGE, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.BLUE, Color.DARK_GRAY, Color.GRAY));
        wordCloud.setFontScalar(new LinearFontScalar(5, 300));
        wordCloud.setKumoFont(new KumoFont("楷体 常规", FontWeight.BOLD));
        wordCloud.build(frequencies);
        wordCloud.writeToStreamAsPNG(Files.newOutputStream(cacheImageFile.toPath()));
        return group.uploadImage(ExternalResource.create(cacheImageFile));
    }
}
