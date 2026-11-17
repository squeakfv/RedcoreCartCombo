package dev.redcore.cartcombo;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ComboConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("redcore-cart-combo.properties");

    public static int railDelay = 5;
    public static int cartDelay = 20;
    public static int igniteDelay = 12;
    public static int crossbowDelay = 0;
    public static boolean autoRestoreSlot = true;
    public static boolean showHud = true;
    public static boolean pauseOnScreen = true;

    private ComboConfig() {}

    public static void load() {
        Properties p = new Properties();
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                p.load(in);
                railDelay = clamp(parse(p, "railDelay", railDelay), 0, 100);
                cartDelay = clamp(parse(p, "cartDelay", cartDelay), 0, 200);
                igniteDelay = clamp(parse(p, "igniteDelay", igniteDelay), 0, 200);
                crossbowDelay = clamp(parse(p, "crossbowDelay", crossbowDelay), 0, 200);
                autoRestoreSlot = Boolean.parseBoolean(p.getProperty("autoRestoreSlot", "true"));
                showHud = Boolean.parseBoolean(p.getProperty("showHud", "true"));
                pauseOnScreen = Boolean.parseBoolean(p.getProperty("pauseOnScreen", "true"));
            } catch (IOException ignored) { }
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("railDelay", Integer.toString(railDelay));
        p.setProperty("cartDelay", Integer.toString(cartDelay));
        p.setProperty("igniteDelay", Integer.toString(igniteDelay));
        p.setProperty("crossbowDelay", Integer.toString(crossbowDelay));
        p.setProperty("autoRestoreSlot", Boolean.toString(autoRestoreSlot));
        p.setProperty("showHud", Boolean.toString(showHud));
        p.setProperty("pauseOnScreen", Boolean.toString(pauseOnScreen));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "Redcore Cart Combo settings");
            }
        } catch (IOException ignored) { }
    }

    private static int parse(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key, Integer.toString(fallback))); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void changeDelay(String key, int delta) {
        switch (key) {
            case "rail" -> railDelay = clamp(railDelay + delta, 0, 100);
            case "cart" -> cartDelay = clamp(cartDelay + delta, 0, 200);
            case "ignite" -> igniteDelay = clamp(igniteDelay + delta, 0, 200);
            case "crossbow" -> crossbowDelay = clamp(crossbowDelay + delta, 0, 200);
        }
        save();
    }

    public static void setBinding(KeyBinding binding, int keyCode) {
        binding.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));
        MinecraftClient.getInstance().options.write();
    }

    public static String keyName(KeyBinding binding) {
        return binding.getBoundKeyLocalizedText().getString();
    }

    public static int[] editableKeys() {
        return new int[] {
            GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_B, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_D,
            GLFW.GLFW_KEY_E, GLFW.GLFW_KEY_F, GLFW.GLFW_KEY_G, GLFW.GLFW_KEY_H,
            GLFW.GLFW_KEY_I, GLFW.GLFW_KEY_J, GLFW.GLFW_KEY_K, GLFW.GLFW_KEY_L,
            GLFW.GLFW_KEY_M, GLFW.GLFW_KEY_N, GLFW.GLFW_KEY_O, GLFW.GLFW_KEY_P,
            GLFW.GLFW_KEY_Q, GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_T,
            GLFW.GLFW_KEY_U, GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_X,
            GLFW.GLFW_KEY_Y, GLFW.GLFW_KEY_Z,
            GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4,
            GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8,
            GLFW.GLFW_KEY_9, GLFW.GLFW_KEY_0,
            GLFW.GLFW_KEY_F1, GLFW.GLFW_KEY_F2, GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F4,
            GLFW.GLFW_KEY_F5, GLFW.GLFW_KEY_F6, GLFW.GLFW_KEY_F7, GLFW.GLFW_KEY_F8,
            GLFW.GLFW_KEY_F9, GLFW.GLFW_KEY_F10, GLFW.GLFW_KEY_F11, GLFW.GLFW_KEY_F12
        };
    }
}
