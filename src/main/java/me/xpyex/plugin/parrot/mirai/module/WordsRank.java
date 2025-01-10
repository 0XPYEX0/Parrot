package me.xpyex.plugin.parrot.mirai.module;

import cn.hutool.core.lang.Pair;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.palette.ColorPalette;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.BreakIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import me.xpyex.plugin.parrot.mirai.api.CommandMenu;
import me.xpyex.plugin.parrot.mirai.core.command.CommandArguments;
import me.xpyex.plugin.parrot.mirai.core.command.CommandBus;
import me.xpyex.plugin.parrot.mirai.core.command.CommandNode;
import me.xpyex.plugin.parrot.mirai.core.command.parsers.GroupParser;
import me.xpyex.plugin.parrot.mirai.core.module.Module;
import me.xpyex.plugin.parrot.mirai.utils.ValueUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.utils.ExternalResource;

public class WordsRank extends Module {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final HashMap<Long, Pair<File, JSONObject>> WORDS_CACHE = new HashMap<>();
    private static final JSONObject CONFIG = new JSONObject();  // {"Groups": [123, 456]}
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
    private static final WordCloud WORD_CLOUD;

    static {
        CONFIG.set("Groups", new JSONArray());

        Dimension dimension = new Dimension(1920, 1080);
        WORD_CLOUD = new WordCloud(dimension, CollisionMode.RECTANGLE);
        WORD_CLOUD.setPadding(0);
        WORD_CLOUD.setBackground(new RectangleBackground(dimension));
        WORD_CLOUD.setColorPalette(new ColorPalette(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE));
        WORD_CLOUD.setFontScalar(new LinearFontScalar(10, 40));
    }

    @Override
    public void register() throws Throwable {
        reload();

        CronUtil.schedule("55 59 23 * * *", (Task) () -> {  //每天23:59:55保存词云，同时清理缓存
            try {
                saveWords();
                WORDS_CACHE.clear();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

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
                               if (CONFIG.getJSONArray("Groups").contains(source.getId())) return true;
                               source.sendMessage("当前群未启用词云记录");
                               return false;
                           })
                           .notMatchedArg((source, sender, arguments) -> {
                               try {
                                   saveWords();
                                   source.sendMessage("正在生成词云...");
                                   Date date = DATE_FORMAT.parse(arguments.getArgument(0));
                                   source.sendMessage(source.getContact().uploadImage(generateImageToFile(WORDS_CACHE.get(source.getId()).getValue())));
                               } catch (ParseException ignored) {
                                   source.sendMessage("日期格式错误，请按照 yyyy-MM-dd 格式填写");
                               }
                           })
                    , "date")
                .child(CommandNode.<Group>of((source, sender, arguments) -> {
                    arguments.getArgument(0, GroupParser.class, Group.class)
                        .ifPresentOrElse(group -> {
                            JSONArray groups = CONFIG.getJSONArray("Groups");
                            boolean isEnable = "enable".equalsIgnoreCase(arguments.getLabelReverse(0));
                            if (isEnable) {
                                groups.add(group.getId());
                            } else {
                                groups.remove(group.getId());
                            }
                            CONFIG.set("Groups", groups);
                            source.sendMessage("已在群 <" + group.getId() + "> " + (isEnable ? "启用" : "禁用") + "词云记录");
                        }, () -> source.sendMessage("请填写正确的群号"));
                }).executableCheck((source, sender) -> {
                    if (!sender.hasPerm(getName() + ".admin")) {
                        source.sendMessage("缺少权限节点: " + getName() + ".admin");
                        return false;
                    }
                    return true;
                }), "enable", "disable")
            , "词云", "wordRank", "wordsRank", "wordsCloud", "wordCloud");

        listenEvent(GroupMessageEvent.class, event -> {
            if (!CONFIG.getJSONArray("Groups").contains(event.getGroup().getId())) {
                return;
            }
            File todayWordsFile = getGroupWordsFile(event.getGroup(), new Date());
            Pair<File, JSONObject> todayWords = WORDS_CACHE.computeIfAbsent(event.getGroup().getId(), groupId -> {
                JSONObject words = todayWordsFile.exists() ? JSONUtil.readJSONObject(todayWordsFile, StandardCharsets.UTF_8) : new JSONObject();
                return Pair.of(todayWordsFile, words);
            });
            breakWords(event.getMessage().contentToString())
                .forEach(word -> todayWords.getValue().set(word, todayWords.getValue().getInt(word, 0) + 1));
            //"Word": count
        });

        listenEvent(BotOfflineEvent.class, event -> saveWords());
    }

    private void reload() throws Throwable {
        File configFile = new File(getConfigFolder(), "config.json");
        if (!configFile.exists()) {
            Files.writeString(configFile.toPath(), CONFIG.toStringPretty(), StandardCharsets.UTF_8);
        }
        CONFIG.putAll(JSONUtil.readJSONObject(configFile, StandardCharsets.UTF_8));
    }

    private File getGroupWordsFile(Group group, Date date) {
        ValueUtil.notNull("’group‘ ,'date‘ must not be null", group, date);
        return getGroupWordsFile(group.getId(), date);
    }

    private File getGroupWordsFile(long id, Date date) {
        ValueUtil.notNull("'date' must not be null", date);
        return new File(getDataFolder(), "words/" + id + "/" + DATE_FORMAT.format(date) + ".json");
    }

    private void saveWords() throws Throwable {
        for (Map.Entry<Long, Pair<File, JSONObject>> entry : WORDS_CACHE.entrySet()) {
            Long groupId = entry.getKey();
            JSONObject words = entry.getValue().getValue();

            File wordsFile = getGroupWordsFile(groupId, new Date());
            if (!wordsFile.getParentFile().exists()) {
                wordsFile.getParentFile().mkdirs();
            }
            Files.writeString(wordsFile.toPath(), JSONUtil.toJsonPrettyStr(words));
        }
    }

    private List<String> breakWords(String sentence) {
        ArrayList<String> result = new ArrayList<>();
        BreakIterator wordIterator = BreakIterator.getWordInstance(Locale.CHINA);
        wordIterator.setText(sentence);
        int start = wordIterator.first();
        for (int end = wordIterator.next(); end != BreakIterator.DONE; start = end, end = wordIterator.next()) {
            String word = sentence.substring(start, end).trim();
            if (!word.isEmpty()) {
                result.add(word);
            }
        }
        return result;
    }

    private ExternalResource generateImageToFile(JSONObject words) throws Throwable {
        File cacheImageFile = File.createTempFile("WordsRank-" + TIME_FORMAT.format(new Date()), ".png");
        List<WordFrequency> wordFrequencies = words.entrySet().stream()
                                          .map(entry -> new WordFrequency(entry.getKey(), (Integer) entry.getValue()))
                                          .collect(Collectors.toList());
        WORD_CLOUD.build(wordFrequencies);
        WORD_CLOUD.writeToStreamAsPNG(Files.newOutputStream(cacheImageFile.toPath()));
        return ExternalResource.create(cacheImageFile);
    }
}
