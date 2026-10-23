package eu.shardline.mod.story;

import eu.shardline.mod.Shardline;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Verzoegerte Aufgaben auf dem Server-Hauptthread, z. B. Dialogzeilen, die nacheinander erscheinen.
 * Nicht persistent: beim Serverstopp werden offene Aufgaben verworfen.
 */
@EventBusSubscriber(modid = Shardline.MODID)
public final class StoryScheduler {
    private static final List<Task> TASKS = new ArrayList<>();
    private static long tick;

    private StoryScheduler() {
    }

    public static void schedule(int delayTicks, Runnable action) {
        TASKS.add(new Task(tick + Math.max(1, delayTicks), action));
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        tick++;
        if (TASKS.isEmpty()) {
            return;
        }
        List<Runnable> due = new ArrayList<>();
        for (Iterator<Task> iterator = TASKS.iterator(); iterator.hasNext(); ) {
            Task task = iterator.next();
            if (task.dueTick <= tick) {
                due.add(task.action);
                iterator.remove();
            }
        }
        for (Runnable action : due) {
            try {
                action.run();
            } catch (RuntimeException e) {
                Shardline.LOGGER.error("Story-Aufgabe fehlgeschlagen", e);
            }
        }
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        TASKS.clear();
        tick = 0L;
    }

    private record Task(long dueTick, Runnable action) {
    }
}
