package eu.shardline.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.shardline.mod.Shardline;
import eu.shardline.mod.entity.ModEntities;
import eu.shardline.mod.story.ShardlineWorldData;
import eu.shardline.mod.story.StoryChapter;
import eu.shardline.mod.story.StoryEnding;
import eu.shardline.mod.story.StoryManager;
import eu.shardline.mod.story.ending.EndingManager;
import eu.shardline.mod.story.lore.LoreBooks;
import eu.shardline.mod.virus.Purification;
import eu.shardline.mod.virus.PurityAnchors;
import eu.shardline.mod.virus.VirusOutbreak;
import eu.shardline.mod.virus.VirusSpreadManager;
import java.util.Collection;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /shardline}-Befehle. Alles ausser {@code story vote} erfordert Operator-Rechte (Stufe 2).
 *
 * <pre>
 * /shardline story set &lt;kapitel&gt; [spieler]     Kapitel setzen (0-6)
 * /shardline story info [spieler]               Kapitel, gelesene Buecher, Visionen, Virusherzen
 * /shardline story vote &lt;seal|dominate&gt;        Stimme in der laufenden Abstimmung abgeben
 * /shardline story ending reset                 Ende und Titel zuruecksetzen
 * /shardline lore give &lt;buch|all&gt; [spieler]     Lore-Buecher ausgeben
 * /shardline virus status                       Zustand, Warteschlange, Statistik
 * /shardline virus pause | resume               Ausbreitung anhalten / fortsetzen
 * /shardline virus purge &lt;radius&gt;              Infizierte Bloecke um die eigene Position reinigen
 * /shardline virus outbreak                     Ausbruch an der eigenen Position ausloesen (Test)
 * /shardline virus reset                        Weltzustand "infiziert" aufheben (Bloecke bleiben)
 * </pre>
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class ShardlineCommands {
    private static final int MAX_PURGE_RADIUS = 256;

    private ShardlineCommands() {
    }

    @SubscribeEvent
    static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shardline")
                .then(Commands.literal("story")
                        .then(Commands.literal("set").requires(source -> source.hasPermission(2))
                                .then(Commands.argument("chapter", IntegerArgumentType.integer(0, StoryChapter.MAX))
                                        .executes(context -> setChapter(context, List.of(context.getSource().getPlayerOrException())))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(context -> setChapter(context, EntityArgument.getPlayers(context, "targets"))))))
                        .then(Commands.literal("status")
                                .executes(context -> status(context.getSource(), context.getSource().getPlayerOrException())))
                        .then(Commands.literal("info").requires(source -> source.hasPermission(2))
                                .executes(context -> info(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> info(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                        .then(Commands.literal("vote")
                                .then(Commands.literal("seal").executes(context -> vote(context, StoryEnding.SEALED)))
                                .then(Commands.literal("dominate").executes(context -> vote(context, StoryEnding.DOMINATED))))
                        .then(Commands.literal("ending").requires(source -> source.hasPermission(2))
                                .then(Commands.literal("reset").executes(ShardlineCommands::resetEnding))))
                .then(Commands.literal("dialogue").requires(source -> source.hasPermission(2))
                        .then(Commands.literal("start")
                                .then(Commands.argument("npc", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                eu.shardline.mod.story.dialogue.Dialogues.all().stream().map(d -> d.speaker()).distinct(), builder))
                                        .then(Commands.argument("id", net.minecraft.commands.arguments.ResourceLocationArgument.id())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                                        eu.shardline.mod.story.dialogue.Dialogues.all().stream().map(d -> d.id()), builder))
                                                .executes(ShardlineCommands::startDialogue))))
                        .then(Commands.literal("reload").executes(ShardlineCommands::reloadDialogues)))
                .then(Commands.literal("quest").requires(source -> source.hasPermission(2))
                        .then(Commands.literal("list")
                                .executes(context -> listQuests(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> listQuests(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                        .then(questCommand("start"))
                        .then(questCommand("complete"))
                        .then(questCommand("reset")))
                .then(Commands.literal("lore").requires(source -> source.hasPermission(2))
                        .then(Commands.literal("give")
                                .then(Commands.argument("book", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                java.util.stream.Stream.concat(java.util.stream.Stream.of("all"),
                                                        LoreBooks.ALL.stream().map(LoreBooks.LoreBook::id)), builder))
                                        .executes(context -> giveLore(context, List.of(context.getSource().getPlayerOrException())))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(context -> giveLore(context, EntityArgument.getPlayers(context, "targets")))))))
                .then(Commands.literal("virus").requires(source -> source.hasPermission(2))
                        .then(Commands.literal("status").executes(context -> status(context.getSource())))
                        .then(Commands.literal("pause").executes(context -> setPaused(context.getSource(), true)))
                        .then(Commands.literal("resume").executes(context -> setPaused(context.getSource(), false)))
                        .then(Commands.literal("purge")
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1, MAX_PURGE_RADIUS))
                                        .executes(ShardlineCommands::purge)))
                        .then(Commands.literal("outbreak").executes(ShardlineCommands::outbreak))
                        .then(Commands.literal("reset").executes(context -> resetVirus(context.getSource())))));
    }

    // ------------------------------------------------------------------ dialogue & quest

    private static int startDialogue(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String npc = StringArgumentType.getString(context, "npc");
        net.minecraft.resources.ResourceLocation id = net.minecraft.commands.arguments.ResourceLocationArgument.getId(context, "id");
        var dialogue = eu.shardline.mod.story.dialogue.Dialogues.get(id);
        if (dialogue == null || !dialogue.speaker().equals(npc)) {
            context.getSource().sendFailure(Component.translatable("commands.shardline.dialogue.unknown", npc, id.toString()));
            return 0;
        }
        if (!eu.shardline.mod.story.dialogue.DialogueSessions.start(player, null, id)) {
            context.getSource().sendFailure(Component.translatable("commands.shardline.dialogue.no_start", id.toString()));
            return 0;
        }
        return 1;
    }

    private static int reloadDialogues(CommandContext<CommandSourceStack> context) {
        var result = eu.shardline.mod.story.StoryDataLoader.reloadNow(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.translatable("commands.shardline.dialogue.reloaded", result.dialogues().size(),
                result.quests().size(), result.errors().size()), true);
        for (String error : result.errors().subList(0, Math.min(8, result.errors().size()))) {
            context.getSource().sendFailure(Component.literal(error));
        }
        return result.dialogues().size();
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> questCommand(String action) {
        return Commands.literal(action)
                .then(Commands.argument("quest", StringArgumentType.string())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                eu.shardline.mod.story.quest.Quests.all().stream().map(q -> q.id()), builder))
                        .executes(context -> quest(context, action, List.of(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> quest(context, action, EntityArgument.getPlayers(context, "targets")))));
    }

    private static int quest(CommandContext<CommandSourceStack> context, String action, Collection<ServerPlayer> targets) {
        String id = StringArgumentType.getString(context, "quest");
        if (eu.shardline.mod.story.quest.Quests.get(id) == null) {
            context.getSource().sendFailure(Component.translatable("commands.shardline.quest.unknown", id));
            return 0;
        }
        int changed = 0;
        for (ServerPlayer player : targets) {
            boolean ok = switch (action) {
                case "start" -> eu.shardline.mod.story.quest.Quests.start(player, id);
                case "complete" -> eu.shardline.mod.story.quest.Quests.completeStep(player, id);
                default -> {
                    eu.shardline.mod.story.quest.Quests.reset(player, id);
                    yield true;
                }
            };
            changed += ok ? 1 : 0;
        }
        int count = changed;
        context.getSource().sendSuccess(() -> Component.translatable("commands.shardline.quest." + action, id, count), true);
        return count;
    }

    private static int listQuests(CommandSourceStack source, ServerPlayer player) {
        ShardlineWorldData data = StoryManager.data(source.getServer());
        source.sendSuccess(() -> Component.translatable("commands.shardline.quest.list", player.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
        data.quests(player.getUUID()).forEach((id, progress) -> {
            var quest = eu.shardline.mod.story.quest.Quests.get(id);
            Component title = quest != null ? quest.title() : Component.literal(id);
            Component state = progress.active()
                    ? Component.translatable("commands.shardline.quest.list.active", progress.step() + 1, quest != null ? quest.steps().size() : 0,
                    progress.count())
                    : Component.translatable("commands.shardline.quest.list.done", progress.completions());
            source.sendSuccess(() -> Component.literal(" - ").append(title).append(" ").append(state), false);
        });
        return data.quests(player.getUUID()).size();
    }

    // ------------------------------------------------------------------ story

    private static int setChapter(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) {
        int chapter = IntegerArgumentType.getInteger(context, "chapter");
        for (ServerPlayer player : targets) {
            StoryManager.setChapter(player, chapter);
        }
        Component chapterName = chapter == 0 ? Component.translatable("story.shardline.chapter.none")
                : StoryChapter.byNumber(chapter).title(StoryManager.data(context.getSource().getServer()).ending());
        context.getSource().sendSuccess(() -> Component.translatable("commands.shardline.story.set", targets.size(), chapter, chapterName), true);
        return targets.size();
    }

    private static int info(CommandSourceStack source, ServerPlayer player) {
        ShardlineWorldData data = StoryManager.data(source.getServer());
        int chapter = data.chapter(player.getUUID());
        Component chapterName = chapter == 0 ? Component.translatable("story.shardline.chapter.none")
                : StoryChapter.byNumber(chapter).title(data.branch(player.getUUID()));
        source.sendSuccess(() -> Component.translatable("commands.shardline.story.info", player.getDisplayName(), chapter, chapterName,
                data.countFlags(player.getUUID(), "book:"), LoreBooks.ALL.size(),
                data.countFlags(player.getUUID(), "vision:"), eu.shardline.mod.story.lore.Visions.COUNT,
                data.heartsDefeated(player.getUUID()),
                data.isKeeper(player.getUUID()) ? Component.translatable("title.shardline.keeper")
                        : data.isRuler(player.getUUID()) ? Component.translatable("title.shardline.ruler")
                        : Component.translatable("title.shardline.none")), false);
        // Nach dem Ausbruch: Zweig, Epilog und Auftraege
        StoryEnding branch = data.branch(player.getUUID());
        String epilogue = data.epilogue(player.getUUID());
        long active = data.quests(player.getUUID()).values().stream().filter(eu.shardline.mod.story.quest.QuestProgress::active).count();
        long done = data.quests(player.getUUID()).values().stream().filter(eu.shardline.mod.story.quest.QuestProgress::everCompleted).count();
        source.sendSuccess(() -> Component.translatable("commands.shardline.story.info.aftermath", branch.title(),
                epilogue.isEmpty() ? Component.translatable("story.shardline.epilogue.none") : Component.translatable("story.shardline.epilogue." + epilogue),
                active, done), false);
        return chapter;
    }

    /** Fortschritt des eigenen Spielers: Kapitelleiste, aktuelles Kapitel, naechstes Ziel, Sammelstand. */
    private static int status(CommandSourceStack source, ServerPlayer player) {
        for (Component line : statusLines(source.getServer(), player.getUUID())) {
            source.sendSuccess(() -> line, false);
        }
        return StoryManager.data(source.getServer()).chapter(player.getUUID());
    }

    /** Schluessel des naechsten Ziels: story.shardline.next.&lt;kapitel&gt;[.&lt;zweig&gt;], nach dem Epilog story.shardline.next.epilogue. */
    public static String nextKey(int chapter, StoryEnding branch, String epilogue) {
        if (!epilogue.isEmpty()) {
            return "story.shardline.next.epilogue";
        }
        StoryChapter current = StoryChapter.byNumber(chapter);
        return current != null && current.branched() && branch != StoryEnding.NONE
                ? "story.shardline.next." + chapter + "." + branch.id() : "story.shardline.next." + chapter;
    }

    /** Zeilen fuer {@code /shardline story status} (auch fuer Tests). */
    public static List<Component> statusLines(net.minecraft.server.MinecraftServer server, java.util.UUID player) {
        ShardlineWorldData data = StoryManager.data(server);
        int chapter = data.chapter(player);
        StoryEnding branch = data.branch(player);
        List<Component> lines = new java.util.ArrayList<>();
        lines.add(Component.translatable("commands.shardline.story.status.header").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        // Kapitelleiste: erreichte Kapitel golden, das aktuelle hervorgehoben, kommende grau
        net.minecraft.network.chat.MutableComponent bar = Component.empty();
        for (StoryChapter step : StoryChapter.values()) {
            ChatFormatting color = step.number() < chapter ? ChatFormatting.GOLD : step.number() == chapter ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY;
            bar.append(Component.literal(step.number() <= chapter ? "\u25C6" : "\u25C7").withStyle(color)
                    .withStyle(style -> style.withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                            net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, step.label().copy().append(": ").append(step.title(branch))))));
            if (step.number() < StoryChapter.MAX) {
                bar.append(Component.literal("\u2500").withStyle(step.number() < chapter ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
            }
        }
        lines.add(Component.empty().append(bar).append(Component.literal("  " + chapter + "/" + StoryChapter.MAX).withStyle(ChatFormatting.GRAY)));
        StoryChapter current = StoryChapter.byNumber(chapter);
        lines.add(current == null ? Component.translatable("commands.shardline.story.status.none").withStyle(ChatFormatting.GRAY)
                : Component.translatable("commands.shardline.story.status.chapter", current.label(), current.title(branch)).withStyle(ChatFormatting.GOLD));
        if (current != null) {
            lines.add(current.summary(branch).copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        String next = chapter == StoryChapter.BEFORE_ENDING && data.ending() != StoryEnding.NONE ? "story.shardline.next.ended"
                : nextKey(chapter, branch, data.epilogue(player));
        lines.add(Component.translatable("commands.shardline.story.status.next", Component.translatable(next)).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("commands.shardline.story.status.collection",
                data.countFlags(player, "book:"), LoreBooks.ALL.size(),
                data.countFlags(player, "vision:"), eu.shardline.mod.story.lore.Visions.COUNT,
                data.heartsDefeated(player), eu.shardline.mod.config.ShardlineConfig.HEARTS_FOR_CORE.get()).withStyle(ChatFormatting.DARK_AQUA));
        if (data.ending() != StoryEnding.NONE) {
            lines.add(Component.translatable("commands.shardline.story.status.ending", data.ending().title()).withStyle(ChatFormatting.DARK_PURPLE));
        }
        return lines;
    }

    private static int vote(CommandContext<CommandSourceStack> context, StoryEnding choice) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!EndingManager.isVoteRunning()) {
            context.getSource().sendFailure(Component.translatable("commands.shardline.story.no_vote"));
            return 0;
        }
        EndingManager.onChoice(player, choice);
        return 1;
    }

    private static int resetEnding(CommandContext<CommandSourceStack> context) {
        EndingManager.reset(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.translatable("commands.shardline.story.ending_reset"), true);
        return 1;
    }

    private static int giveLore(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) {
        String id = StringArgumentType.getString(context, "book");
        List<LoreBooks.LoreBook> books = id.equals("all") ? LoreBooks.ALL
                : LoreBooks.byId(id) != null ? List.of(LoreBooks.byId(id)) : List.of();
        if (books.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.shardline.lore.unknown", id));
            return 0;
        }
        for (ServerPlayer player : targets) {
            for (LoreBooks.LoreBook book : books) {
                ItemStack stack = LoreBooks.create(book);
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.shardline.lore.given", books.size(), targets.size()), true);
        return books.size() * targets.size();
    }

    // ------------------------------------------------------------------ virus

    private static int status(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        ShardlineWorldData data = StoryManager.data(server);
        ServerLevel level = source.getLevel();
        Component state;
        if (!data.isInfected()) {
            state = Component.translatable("commands.shardline.virus.state.dormant").withStyle(ChatFormatting.GREEN);
        } else if (data.ending() == StoryEnding.SEALED) {
            state = Component.translatable("commands.shardline.virus.state.sealed").withStyle(ChatFormatting.GOLD);
        } else if (StoryManager.isSpreadPaused(server)) {
            state = Component.translatable("commands.shardline.virus.state.paused").withStyle(ChatFormatting.YELLOW);
        } else {
            state = Component.translatable("commands.shardline.virus.state.spreading").withStyle(ChatFormatting.DARK_PURPLE);
        }
        long hearts = 0;
        for (ServerLevel each : server.getAllLevels()) {
            hearts += each.getEntities(ModEntities.VIRUS_HEART.get(), heart -> heart.isAlive()).size();
        }
        long heartCount = hearts;
        source.sendSuccess(() -> Component.translatable("commands.shardline.virus.status", state, data.ending().title()), false);
        source.sendSuccess(() -> Component.translatable("commands.shardline.virus.status.stats", data.totalInfectedChunks(),
                data.totalInfectedBlocks(), data.herds(level.dimension()).size(), PurityAnchors.count(level),
                VirusSpreadManager.queuedCount(level), Purification.runningJobs(), heartCount), false);
        return data.isInfected() ? 1 : 0;
    }

    private static int setPaused(CommandSourceStack source, boolean paused) {
        StoryManager.data(source.getServer()).setPaused(paused);
        if (paused) {
            VirusSpreadManager.clearQueues();
        }
        StoryManager.syncAll(source.getServer());
        source.sendSuccess(() -> Component.translatable(paused ? "commands.shardline.virus.paused" : "commands.shardline.virus.resumed"), true);
        return 1;
    }

    private static int purge(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int radius = IntegerArgumentType.getInteger(context, "radius");
        BlockPos center = BlockPos.containing(source.getPosition());
        source.sendSuccess(() -> Component.translatable("commands.shardline.virus.purge.started", radius), true);
        Purification.purifySphere(source.getLevel(), center, radius,
                purified -> source.sendSuccess(() -> Component.translatable("commands.shardline.virus.purge.done", purified), true));
        return radius;
    }

    private static int outbreak(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        VirusOutbreak.trigger(source.getLevel(), BlockPos.containing(source.getPosition()), source.getPlayer());
        source.sendSuccess(() -> Component.translatable("commands.shardline.virus.outbreak"), true);
        return 1;
    }

    private static int resetVirus(CommandSourceStack source) {
        ShardlineWorldData data = StoryManager.data(source.getServer());
        data.setInfected(false, 0L);
        data.setPaused(false);
        data.clearInfectionStats();
        VirusSpreadManager.clearQueues();
        StoryManager.syncAll(source.getServer());
        source.sendSuccess(() -> Component.translatable("commands.shardline.virus.reset"), true);
        return 1;
    }
}
