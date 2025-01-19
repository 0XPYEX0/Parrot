package me.xpyex.plugin.parrot.mirai.module;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ColorUtil;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import me.xpyex.plugin.parrot.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandArguments;
import me.xpyex.plugin.parrot.mirai.core.command.CommandBus;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.GroupParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.utils.StringUtil;
import me.xpyex.plugin.parrot.utils.ValueUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

public class WordsRank extends Module {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
    private static final WeakHashMap<Long, File> TEXT_FILE_CACHE = new WeakHashMap<>();
    private static final int COOLDOWN = 300;  // 5分钟
    private static JSONObject CONFIG = new JSONObject().set("Groups", new JSONArray());  // {"Groups": [123, 456]}
    private File CONFIG_FILE;

    private static Color[] parseHEX(String... hex) {
        return Arrays.stream(hex)
                   .map(s -> {
                       if (s.length() != 6 && s.length() != 7)
                           throw new IllegalArgumentException("HEX参数错误");
                       return ColorUtil.hexToColor(s);
                   })
                   .toList()
                   .toArray(new Color[0]);
    }

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
                    Date yesterday = DateUtil.yesterday().toJdkDate();
                    CommandBus.dispatchCommand(source, sender, CommandArguments.of(arguments.getLabel(0), "date", info(DATE_FORMAT.format(yesterday))));
                }), "yesterday")
                .child(CommandNode.<Group>of((source, sender, arguments) -> source.sendMessage("请填写日期"))
                           .subjectCooldown(COOLDOWN)
                           .executableCheck((source, sender) -> {
                               if (CONFIG.getJSONArray("Groups").contains(source.getId())) return true;
                               source.sendMessage("当前群未启用词云记录");
                               return false;
                           })
                           .notMatchedArg((source, sender, arguments) -> {
                               try {
                                   source.sendMessage("正在生成词云...");
                                   Date date = info(DATE_FORMAT.parse(arguments.getArgument(0)));
                                   source.sendMessage(ValueUtil.getOrDefault(generateImage(source.getContactAsGroup(), date), new PlainText("该日期未记录词云")));
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
                    }).permission(getName() + ".admin", "缺少权限节点: " + getName() + ".admin"),
                    "enable", "disable")
            , "词云", "wordRank", "wordsRank", "wordsCloud", "wordCloud", "words");

        listenEvent(GroupMessageEvent.class, event -> {
            JSONArray groups = new JSONArray(CONFIG.getJSONArray("Groups"));
            if (groups.isEmpty()) return;

            if (groups.contains(event.getGroup().getId())) {
                File todayWordsFile = getGroupWordsFile(event.getGroup(), new Date());
                Files.writeString(todayWordsFile.toPath(),
                    Files.readString(todayWordsFile.toPath(), StandardCharsets.UTF_8) + System.lineSeparator() + event.getMessage().stream()
                                                                                                                     .filter(singleMessage -> singleMessage instanceof PlainText)
                                                                                                                     .map(Message::contentToString)
                                                                                                                     .collect(Collectors.joining(".")),
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
        frequencyAnalyzer.setStopWords(
            Set.of("[图片]", "[动画表情]", "什么", "为什么", "可以", "不可以", "因为", "所以", "虽然", "但是", "然后",
                "就是", "这个", "那个", "这样", "那样", "这么", "那么", "这里", "那里", "这种", "那种", "这时", "那时", "这些", "那些",
                "不但", "而且", "不过", "于是", "何况", "乃至", "至于", "比如", "就像", "然而", "并且", "只是", "况且", "此外", "原来",
                "本来", "不是", "因此", "就得", "假如", "要是", "似乎", "好像", "不如", "怎么", "谭明", "不能", "没有", "一样", "哪个",
                "应该", "可能", "以为", "现在", "还是", "主要", "还有", "啊啊", "了了", "哈哈", "一个", "一下")
        );
        frequencyAnalyzer.addFilter(new Filter() {
            @Override
            public boolean test(String s) {
                if (StringUtil.containsIgnoreCaseOr(s, "你", "我", "他", "她", "它")) return false;
                return !StringUtil.startsWithIgnoreCaseOr(s.trim(), "@");
            }
        });
        frequencyAnalyzer.setMinWordLength(2);
        frequencyAnalyzer.setWordFrequenciesToReturn(1000);
        List<WordFrequency> frequencies = frequencyAnalyzer.load(getGroupWordsFile(group, date));


        Dimension dimension = new Dimension(1920, 1080);
        WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
        wordCloud.setPadding(2);
        wordCloud.setBackground(new RectangleBackground(dimension));
        wordCloud.setColorPalette(new ColorPalette(parseHEX(
            "#908724", "#8E3158", "#00FFFF", "C800FF", "00FF00", "404040", "808080", "#8B01B5", "#1D31C8", "#54A12E", "#949EEF",
            "#4C5EE5", "#C0B430", "#30A070", "#74CB48", "#70C000"
        )));
        wordCloud.setFontScalar(new LinearFontScalar(5, 250));
        wordCloud.setKumoFont(new KumoFont("楷体 常规", FontWeight.BOLD));
        wordCloud.build(frequencies);
        wordCloud.writeToStreamAsPNG(Files.newOutputStream(cacheImageFile.toPath()));
        try (ExternalResource resource = ExternalResource.create(cacheImageFile)) {
            return group.uploadImage(resource);
        }
    }
}
