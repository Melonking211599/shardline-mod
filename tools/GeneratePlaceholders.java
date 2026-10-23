import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Erzeugt alle Platzhalter-Texturen der Shardline-Mod.
 * Aufruf aus dem Projektordner:  java tools/GeneratePlaceholders.java
 * Vorhandene Dateien werden nur mit --force ueberschrieben, damit fertige Texturen sicher sind.
 */
public class GeneratePlaceholders {
    private static final Path ROOT = Path.of("src/main/resources/assets/shardline/textures");
    private static boolean force;

    public static void main(String[] args) throws IOException {
        force = args.length > 0 && args[0].equals("--force");

        // ---------- Bloecke (16x16) ----------
        stone("block/shardstone", 0x6B4F9E);
        bricks("block/shardstone_bricks", 0x6B4F9E, false);
        bricks("block/cracked_shardstone_bricks", 0x5E4590, true);
        ore("block/shard_ore", 0x6B4F9E, 0xF07BFF);
        ore("block/deepslate_shard_ore", 0x3A3A44, 0xF07BFF);
        stone("block/shard_block", 0xC77DFF);
        stone("block/glow_crystal_block", 0x8FF3FF);
        stone("block/crystal_moss", 0x52D6C8);
        mossSide("block/crystal_moss_side", 0x6B4F9E, 0x52D6C8);
        stone("block/ash_block", 0x6E6668);
        logSide("block/crystal_log", 0x7FB8E0);
        logTop("block/crystal_log_top", 0x7FB8E0);
        planks("block/crystal_planks", 0x9FC9EC);
        leaves("block/crystal_leaves", 0xE29BFF);
        plant("block/crystal_sapling", 0x7FB8E0, 0xE29BFF);
        plant("block/glowbloom", 0x3FA58F, 0x8FF3FF);
        plant("block/shard_fern", 0x52D6C8, 0x52D6C8);
        plant("block/glowcap", 0xB7E4FF, 0x5FE1FF);
        plant("block/luminous_berry_bush", 0x3FA58F, 0xFF7BE5);
        crystal("block/glow_crystal_cluster", 0x8FF3FF);
        crystal("block/crystal_web", 0xC6F6FF);
        frame("block/shard_portal_frame", 0x2B1B47, 0xC77DFF);
        portal("block/shard_portal");
        stone("block/colossus_altar", 0x4B3570);
        ore("block/colossus_altar_top", 0x4B3570, 0x8FF3FF);
        stone("block/queen_altar", 0x2A1740);
        ore("block/queen_altar_top", 0x2A1740, 0xFF5EDB);
        stone("block/colossus_trophy", 0x8FF3FF);
        stone("block/queen_trophy", 0xFF5EDB);

        // ---------- Items (16x16) ----------
        gem("item/shard_fragment", 0xE07BFF);
        gem("item/raw_shard", 0xA05FD0);
        ingot("item/shard_ingot", 0xC77DFF);
        gem("item/ash_dust", 0x8A8284);
        gem("item/scout_eye", 0xFFD1FF);
        gem("item/void_ray_hide", 0x2B2050);
        gem("item/shadow_pelt", 0x1A1020);
        gem("item/knight_crest", 0xB486FF);
        gem("item/luminous_berry", 0xFF7BE5);
        key("item/shard_key", 0xE07BFF);
        gem("item/colossus_summoning_crystal", 0x8FF3FF);
        gem("item/colossus_core", 0x9FFCFF);
        template("item/colossus_upgrade_smithing_template", 0x9FFCFF);
        gem("item/heart_of_the_expanse", 0xFF3FD0);
        armorIcon("item/shard_helmet", 0xC77DFF, 0);
        armorIcon("item/shard_chestplate", 0xC77DFF, 1);
        armorIcon("item/shard_leggings", 0xC77DFF, 2);
        armorIcon("item/shard_boots", 0xC77DFF, 3);
        armorIcon("item/colossus_helmet", 0x7FE8FF, 0);
        armorIcon("item/colossus_chestplate", 0x7FE8FF, 1);
        armorIcon("item/colossus_leggings", 0x7FE8FF, 2);
        armorIcon("item/colossus_boots", 0x7FE8FF, 3);
        tool("item/shard_sword", 0xC77DFF, 0);
        tool("item/shard_pickaxe", 0xC77DFF, 1);
        tool("item/shard_axe", 0xC77DFF, 2);
        tool("item/shard_shovel", 0xC77DFF, 3);
        tool("item/colossus_sword", 0x7FE8FF, 0);
        tool("item/colossus_pickaxe", 0x7FE8FF, 1);
        tool("item/colossus_axe", 0x7FE8FF, 2);
        tool("item/colossus_shovel", 0x7FE8FF, 3);
        tool("item/queens_shardblade", 0xFF5EDB, 0);
        gem("item/colossus_gauntlet", 0x9FFCFF);
        bow("item/crystal_bow", 0x8FF3FF, 0);
        bow("item/crystal_bow_pulling_0", 0x8FF3FF, 1);
        bow("item/crystal_bow_pulling_1", 0x8FF3FF, 2);
        bow("item/crystal_bow_pulling_2", 0x8FF3FF, 3);

        // ---------- Ruestungsschichten (64x32) ----------
        armorLayer("models/armor/shard_layer_1", 0xC77DFF);
        armorLayer("models/armor/shard_layer_2", 0xA65FE0);
        armorLayer("models/armor/colossus_layer_1", 0x7FE8FF);
        armorLayer("models/armor/colossus_layer_2", 0x5FC8E8);

        // ---------- Entities (Groesse = texture_width/height der .geo.json) ----------
        entity("entity/shard_creeper", 64, 64, 0x6A3FA0, 0xE07BFF, true);
        entity("entity/crystal_golem", 128, 128, 0x5B8FB9, 0xC6F1FF, true);
        entity("entity/crystal_golem_angry", 128, 128, 0x7A4A6A, 0xFF4A6A, false);
        entity("entity/glow_moth", 64, 64, 0x2C3E50, 0x9AF7FF, true);
        entity("entity/ash_walker", 64, 64, 0x4A4A4A, 0xFF7A3D, false);
        entity("entity/shard_scout", 64, 64, 0x7D3CC4, 0xFFD1FF, true);
        entity("entity/void_ray", 128, 128, 0x101025, 0x7A5CFF, true);
        entity("entity/crystal_spider", 64, 64, 0x2B1B3D, 0x7FE7FF, true);
        entity("entity/shard_trader", 64, 64, 0x3F2D5C, 0xE8C66A, false);
        entity("entity/shadow_fox", 64, 64, 0x111111, 0x6A2C8C, true);
        entity("entity/shattered_knight", 64, 64, 0x3A3A4A, 0xB486FF, true);
        entity("entity/crystal_colossus", 256, 256, 0x4B2C7A, 0x9FFCFF, true);
        entity("entity/shard_queen", 128, 128, 0x1A0B2E, 0xFF5EDB, true);
        entity("entity/queen_clone", 128, 128, 0x6A4A9E, 0xFFB0F0, false);
        entity("entity/crystal_pillar", 64, 64, 0x6FD8F0, 0xE0FFFF, true);

        // ---------- Partikel (8x8, je 4 Frames) ----------
        for (int i = 0; i < 4; i++) {
            sparkle("particle/shard_sparkle_" + i, i, 0xF0B0FF);
            sparkle("particle/void_mote_" + i, i, 0xFFFFFF);
        }

        // ---------- Umgebung, GUI ----------
        sky("environment/splitterweite_sky");
        moon("environment/splitterweite_moon");
        overlay("misc/shard_portal_overlay");
        stone("gui/advancements/backgrounds/shardline", 0x24163A);
        logo(Path.of("src/main/resources/shardline_logo.png"));

        System.out.println("Fertig.");
    }

    // ===================== Zeichenhelfer =====================

    private static Color c(int rgb) {
        return new Color(rgb);
    }

    private static Color shade(int rgb, float factor) {
        Color base = c(rgb);
        return new Color(clamp(base.getRed() * factor), clamp(base.getGreen() * factor), clamp(base.getBlue() * factor));
    }

    private static Color alpha(Color color, int a) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }

    private static Random rnd(String name) {
        return new Random(name.hashCode());
    }

    private static BufferedImage image(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    private static void noiseFill(BufferedImage img, int rgb, float spread, Random random) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                float f = 1.0F + (random.nextFloat() - 0.5F) * spread;
                img.setRGB(x, y, shade(rgb, f).getRGB());
            }
        }
    }

    private static void save(String relative, BufferedImage img) throws IOException {
        save(ROOT.resolve(relative + ".png"), img);
    }

    private static void save(Path file, BufferedImage img) throws IOException {
        if (Files.exists(file) && !force) {
            return;
        }
        Files.createDirectories(file.getParent());
        ImageIO.write(img, "png", file.toFile());
    }

    // ---------- Bloecke ----------

    private static void stone(String name, int rgb) throws IOException {
        BufferedImage img = image(16, 16);
        noiseFill(img, rgb, 0.25F, rnd(name));
        Graphics2D g = img.createGraphics();
        g.setColor(alpha(shade(rgb, 1.35F), 160));
        Random r = rnd(name + "facet");
        for (int i = 0; i < 3; i++) {
            int x = r.nextInt(12), y = r.nextInt(12);
            g.drawLine(x, y, x + 3, y + 3);
        }
        g.dispose();
        save(name, img);
    }

    private static void bricks(String name, int rgb, boolean cracked) throws IOException {
        BufferedImage img = image(16, 16);
        noiseFill(img, rgb, 0.2F, rnd(name));
        Graphics2D g = img.createGraphics();
        g.setColor(shade(rgb, 0.55F));
        for (int y = 0; y < 16; y += 4) {
            g.drawLine(0, y, 15, y);
            int offset = (y / 4) % 2 == 0 ? 0 : 4;
            for (int x = offset; x < 16; x += 8) {
                g.drawLine(x, y, x, y + 3);
            }
        }
        if (cracked) {
            g.setColor(shade(rgb, 0.35F));
            g.drawLine(3, 2, 7, 9);
            g.drawLine(7, 9, 5, 14);
            g.drawLine(11, 1, 13, 6);
        }
        g.dispose();
        save(name, img);
    }

    private static void ore(String name, int base, int accent) throws IOException {
        BufferedImage img = image(16, 16);
        noiseFill(img, base, 0.25F, rnd(name));
        Random r = rnd(name + "ore");
        for (int i = 0; i < 6; i++) {
            int x = 1 + r.nextInt(13), y = 1 + r.nextInt(13);
            img.setRGB(x, y, c(accent).getRGB());
            img.setRGB(x + 1, y, shade(accent, 0.8F).getRGB());
            img.setRGB(x, y + 1, shade(accent, 1.2F).getRGB());
        }
        save(name, img);
    }

    private static void mossSide(String name, int stone, int moss) throws IOException {
        BufferedImage img = image(16, 16);
        noiseFill(img, stone, 0.25F, rnd(name));
        Random r = rnd(name + "moss");
        for (int x = 0; x < 16; x++) {
            int depth = 3 + r.nextInt(3);
            for (int y = 0; y < depth; y++) {
                img.setRGB(x, y, shade(moss, 0.85F + r.nextFloat() * 0.3F).getRGB());
            }
        }
        save(name, img);
    }

    private static void logSide(String name, int rgb) throws IOException {
        BufferedImage img = image(16, 16);
        Random r = rnd(name);
        for (int x = 0; x < 16; x++) {
            float stripe = (x % 4 == 0) ? 0.7F : 1.0F;
            for (int y = 0; y < 16; y++) {
                img.setRGB(x, y, shade(rgb, stripe * (0.9F + r.nextFloat() * 0.2F)).getRGB());
            }
        }
        save(name, img);
    }

    private static void logTop(String name, int rgb) throws IOException {
        BufferedImage img = image(16, 16);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                double d = Math.hypot(x - 7.5, y - 7.5);
                float ring = ((int) d) % 3 == 0 ? 0.75F : 1.05F;
                img.setRGB(x, y, shade(rgb, d > 7 ? 0.6F : ring).getRGB());
            }
        }
        save(name, img);
    }

    private static void planks(String name, int rgb) throws IOException {
        BufferedImage img = image(16, 16);
        noiseFill(img, rgb, 0.12F, rnd(name));
        Graphics2D g = img.createGraphics();
        g.setColor(shade(rgb, 0.65F));
        for (int y = 3; y < 16; y += 4) {
            g.drawLine(0, y, 15, y);
        }
        g.dispose();
        save(name, img);
    }

    private static void leaves(String name, int rgb) throws IOException {
        BufferedImage img = image(16, 16);
        Random r = rnd(name);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (r.nextFloat() < 0.2F) {
                    continue;
                }
                img.setRGB(x, y, shade(rgb, 0.75F + r.nextFloat() * 0.5F).getRGB());
            }
        }
        save(name, img);
    }

    private static void plant(String name, int stem, int bloom) throws IOException {
        BufferedImage img = image(16, 16);
        Graphics2D g = img.createGraphics();
        g.setColor(c(stem));
        g.fillRect(7, 6, 2, 10);
        g.fillRect(4, 10, 3, 1);
        g.fillRect(9, 9, 3, 1);
        g.setColor(c(bloom));
        g.fillOval(4, 1, 8, 7);
        g.setColor(shade(bloom, 1.3F));
        g.fillRect(7, 3, 2, 2);
        g.dispose();
        save(name, img);
    }

    private static void crystal(String name, int rgb) throws IOException {
        BufferedImage img = image(16, 16);
        Graphics2D g = img.createGraphics();
        int[][] spikes = {{7, 1, 3}, {3, 6, 2}, {11, 5, 2}};
        for (int[] s : spikes) {
            g.setColor(c(rgb));
            g.fillPolygon(new int[] {s[0] - s[2], s[0] + 1, s[0] + s[2] + 1}, new int[] {16, s[1], 16}, 3);
            g.setColor(shade(rgb, 1.3F));
            g.drawLine(s[0], s[1] + 2, s[0], 15);
        }
        g.dispose();
        save(name, img);
    }

    private static void frame(String name, int base, int accent) throws IOException {
        BufferedImage img = image(16, 16);
        noiseFill(img, base, 0.3F, rnd(name));
        Graphics2D g = img.createGraphics();
        g.setColor(c(accent));
        g.drawRect(0, 0, 15, 15);
        g.drawLine(3, 3, 12, 12);
        g.drawLine(12, 3, 3, 12);
        g.dispose();
        save(name, img);
    }

    /** Animiertes Portal: 8 Frames uebereinander + .mcmeta. */
    private static void portal(String name) throws IOException {
        int frames = 8;
        BufferedImage img = image(16, 16 * frames);
        for (int f = 0; f < frames; f++) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    double v = Math.sin((x + f * 2) * 0.6) + Math.cos((y - f) * 0.5) + Math.sin((x + y + f) * 0.3);
                    float t = (float) ((v + 3) / 6);
                    Color col = new Color(clamp(120 + 100 * t), clamp(40 + 60 * t), clamp(200 + 55 * t), 190);
                    img.setRGB(x, f * 16 + y, col.getRGB());
                }
            }
        }
        save(name, img);
        Path meta = ROOT.resolve(name + ".png.mcmeta");
        if (!Files.exists(meta) || force) {
            Files.writeString(meta, "{\n  \"animation\": {\n    \"frametime\": 2,\n    \"interpolate\": true\n  }\n}\n");
        }
    }

    // ---------- Items ----------

    private static Graphics2D itemGraphics(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        return g;
    }

    private static void outline(BufferedImage img) {
        BufferedImage copy = image(img.getWidth(), img.getHeight());
        copy.getGraphics().drawImage(img, 0, 0, null);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if ((copy.getRGB(x, y) >>> 24) != 0) {
                    continue;
                }
                boolean edge = false;
                for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    int nx = x + d[0], ny = y + d[1];
                    if (nx >= 0 && ny >= 0 && nx < img.getWidth() && ny < img.getHeight() && (copy.getRGB(nx, ny) >>> 24) != 0) {
                        edge = true;
                    }
                }
                if (edge) {
                    img.setRGB(x, y, new Color(20, 10, 30).getRGB());
                }
            }
        }
    }

    private static void gem(String name, int rgb) throws IOException {
        BufferedImage img = image(16, 16);
        Graphics2D g = itemGraphics(img);
        g.setColor(c(rgb));
        g.fillPolygon(new int[] {8, 13, 8, 3}, new int[] {2, 8, 14, 8}, 4);
        g.setColor(shade(rgb, 1.35F));
        g.fillPolygon(new int[] {8, 10, 8, 6}, new int[] {4, 7, 9, 7}, 4);
        g.setColor(shade(rgb, 0.7F));
        g.drawLine(8, 10, 11, 8);
        g.dispose();
        outline(img);
        save(name, img);
    }

    private static void ingot(String name, int rgb) throws IOException {
        BufferedImage img = image(16, 16);
        Graphics2D g = itemGraphics(img);
        g.setColor(c(rgb));
        g.fillPolygon(new int[] {2, 10, 14, 6}, new int[] {10, 6, 8, 12}, 4);
        g.setColor(shade(rgb, 1.3F));
        g.drawLine(3, 10, 10, 7);
        g.dispose();
        outline(img);
        save(name, img);
    }

    private static void key(String name, int rgb) throws IOException {
        BufferedImage img = image(16, 16);
        Graphics2D g = itemGraphics(img);
        g.setColor(c(rgb));
        g.fillOval(9, 2, 5, 5);
        g.setStroke(new BasicStroke(2));
        g.drawLine(10, 6, 3, 13);
        g.drawLine(5, 11, 7, 13);
        g.dispose();
        outline(img);
        save(name, img);
    }

    private static void template(String name, int rgb) throws IOException {
        BufferedImage img = image(16, 16);
        Graphics2D g = itemGraphics(img);
        g.setColor(new Color(0x3A2A50));
        g.fillRect(2, 1, 12, 14);
        g.setColor(c(rgb));
        g.fillPolygon(new int[] {8, 11, 8, 5}, new int[] {3, 8, 13, 8}, 4);
        g.dispose();
        outline(img);
        save(name, img);
    }

    private static void armorIcon(String name, int rgb, int piece) throws IOException {
        BufferedImage img = image(16, 16);
        Graphics2D g = itemGraphics(img);
        g.setColor(c(rgb));
        switch (piece) {
            case 0 -> {
                g.fillRect(3, 4, 10, 4);
                g.fillRect(3, 8, 3, 3);
                g.fillRect(10, 8, 3, 3);
            }
            case 1 -> {
                g.fillRect(2, 3, 12, 4);
                g.fillRect(4, 7, 8, 7);
            }
            case 2 -> {
                g.fillRect(4, 3, 8, 3);
                g.fillRect(4, 6, 3, 8);
                g.fillRect(9, 6, 3, 8);
            }
            default -> {
                g.fillRect(3, 8, 4, 5);
                g.fillRect(9, 8, 4, 5);
                g.fillRect(2, 12, 5, 2);
                g.fillRect(9, 12, 5, 2);
            }
        }
        g.setColor(shade(rgb, 1.35F));
        g.fillRect(7, 5, 2, 2);
        g.dispose();
        outline(img);
        save(name, img);
    }

    private static void tool(String name, int rgb, int kind) throws IOException {
        BufferedImage img = image(16, 16);
        Graphics2D g = itemGraphics(img);
        g.setColor(new Color(0x6B4A2E));
        if (kind != 0) {
            g.setStroke(new BasicStroke(2));
            g.drawLine(3, 13, 10, 6);
        }
        g.setColor(c(rgb));
        switch (kind) {
            case 0 -> {
                g.setStroke(new BasicStroke(2));
                g.drawLine(4, 12, 13, 3);
                g.setColor(new Color(0x6B4A2E));
                g.drawLine(2, 14, 4, 12);
                g.drawLine(3, 10, 6, 13);
            }
            case 1 -> g.fillPolygon(new int[] {5, 9, 14, 12, 9, 7}, new int[] {3, 2, 7, 9, 5, 5}, 6);
            case 2 -> g.fillPolygon(new int[] {8, 12, 14, 11}, new int[] {4, 2, 6, 9}, 4);
            default -> g.fillOval(9, 2, 5, 5);
        }
        g.dispose();
        outline(img);
        save(name, img);
    }

    private static void bow(String name, int rgb, int pull) throws IOException {
        BufferedImage img = image(16, 16);
        Graphics2D g = itemGraphics(img);
        g.setColor(c(rgb));
        g.setStroke(new BasicStroke(2));
        g.drawArc(2 + pull, 2 + pull, 18 - pull * 2, 18 - pull * 2, 90, 90);
        g.setColor(new Color(230, 230, 255));
        g.setStroke(new BasicStroke(1));
        g.drawLine(3 + pull, 13, 13, 3 + pull);
        g.dispose();
        outline(img);
        save(name, img);
    }

    private static void armorLayer(String name, int rgb) throws IOException {
        BufferedImage img = image(64, 32);
        noiseFill(img, rgb, 0.25F, rnd(name));
        save(name, img);
    }

    // ---------- Entities ----------

    private static void entity(String name, int w, int h, int base, int accent, boolean glowmask) throws IOException {
        BufferedImage img = image(w, h);
        Random r = rnd(name);
        noiseFill(img, base, 0.3F, r);
        Graphics2D g = img.createGraphics();
        int stripes = Math.max(2, w / 16);
        for (int i = 0; i < stripes; i++) {
            g.setColor(alpha(c(accent), 200));
            int x = r.nextInt(w), y = r.nextInt(h);
            g.fillRect(x, y, Math.max(2, w / 24), Math.max(2, h / 24));
        }
        g.setColor(alpha(c(accent), 120));
        for (int y = 0; y < h; y += Math.max(4, h / 8)) {
            g.drawLine(0, y, w, y);
        }
        g.dispose();
        save(name, img);

        if (glowmask) {
            BufferedImage mask = image(w, h);
            Random mr = rnd(name + "glow");
            for (int i = 0; i < (w * h) / 40; i++) {
                int x = mr.nextInt(w), y = mr.nextInt(h);
                mask.setRGB(x, y, shade(accent, 1.2F).getRGB());
            }
            save(name + "_glowmask", mask);
        }
    }

    // ---------- Partikel, Himmel, GUI ----------

    private static void sparkle(String name, int frame, int rgb) throws IOException {
        BufferedImage img = image(8, 8);
        int size = 3 - Math.min(frame, 2);
        Color col = c(rgb);
        for (int i = -size; i <= size; i++) {
            set(img, 4 + i, 4, col);
            set(img, 4, 4 + i, col);
        }
        set(img, 4, 4, Color.WHITE);
        save(name, img);
    }

    private static void set(BufferedImage img, int x, int y, Color col) {
        if (x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight()) {
            img.setRGB(x, y, col.getRGB());
        }
    }

    private static void sky(String name) throws IOException {
        BufferedImage img = image(128, 128);
        Random r = rnd(name);
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                double cloud = Math.sin(x * 0.07) * Math.cos(y * 0.05) + Math.sin((x + y) * 0.03);
                int v = clamp((float) (60 + cloud * 30));
                img.setRGB(x, y, new Color(clamp(v * 0.8F), clamp(v * 0.45F), clamp(v * 1.4F)).getRGB());
            }
        }
        for (int i = 0; i < 90; i++) {
            img.setRGB(r.nextInt(128), r.nextInt(128), new Color(240, 220, 255).getRGB());
        }
        save(name, img);
    }

    private static void moon(String name) throws IOException {
        BufferedImage img = image(64, 64);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(235, 210, 255));
        g.fillOval(6, 6, 52, 52);
        g.setColor(new Color(60, 20, 90));
        g.setStroke(new BasicStroke(2));
        g.drawLine(20, 10, 34, 32);
        g.drawLine(34, 32, 26, 54);
        g.drawLine(34, 32, 52, 28);
        g.dispose();
        save(name, img);
    }

    private static void overlay(String name) throws IOException {
        int size = 256;
        BufferedImage img = image(size, size);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                double d = Math.hypot(x - size / 2.0, y - size / 2.0) / (size / 2.0);
                int a = clamp((float) (Math.min(1.0, d) * 200));
                img.setRGB(x, y, new Color(170, 90, 255, a).getRGB());
            }
        }
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(240, 200, 255, 160));
        Random r = rnd(name);
        for (int i = 0; i < 24; i++) {
            int x = r.nextInt(size), y = r.nextInt(size);
            g.drawLine(x, y, x + r.nextInt(60) - 30, y + r.nextInt(60) - 30);
        }
        g.dispose();
        save(name, img);
    }

    private static void logo(Path file) throws IOException {
        BufferedImage img = image(256, 128);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, new Color(40, 20, 70), 256, 128, new Color(90, 40, 140)));
        g.fillRoundRect(0, 0, 256, 128, 24, 24);
        g.setColor(new Color(230, 150, 255));
        g.fillPolygon(new int[] {40, 58, 40, 22}, new int[] {20, 64, 108, 64}, 4);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        g.drawString("Shardline", 74, 76);
        g.dispose();
        save(file, img);
    }
}
