package dev.redcore.cartcombo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class RedcoreConfigScreen extends Screen {
    private final Screen parent;
    private boolean waitingForKey = false;
    private KeyBinding targetBinding;
    private String targetLabel = "";
    private final List<ButtonWidget> refreshable = new ArrayList<>();

    public RedcoreConfigScreen(Screen parent) {
        super(Text.literal("REDCORE // PARAMÈTRES"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        refreshable.clear();
        int left = this.width / 2 - 155;
        int y = 42;
        int row = 23;

        addHeader(left, y, "TOUCHES DU MOD");
        y += row;

        addKeyButton(left, y, 150, "Activer / lancer", RedcoreCartComboClient.startKey);
        addKeyButton(left + 160, y, 150, "Annuler", RedcoreCartComboClient.cancelKey);
        y += row;

        addKeyButton(left, y, 150, "Ouvrir ce menu", RedcoreCartComboClient.configKey);
        y += row + 5;

        addHeader(left, y, "TOUCHES DES 9 SLOTS DE LA BARRE RAPIDE");
        y += row;
        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int r = i / 3;
            addKeyButton(left + col * 105, y + r * row, 100,
                    "Slot " + (i + 1), MinecraftClient.getInstance().options.hotbarKeys[i]);
        }
        y += 3 * row + 5;

        addHeader(left, y, "DÉLAIS (TICKS)  ·  20 TICKS ≈ 1 SECONDE");
        y += row;
        addDelayRow(left, y, "Rail", "rail", ComboConfig.railDelay);
        y += row;
        addDelayRow(left, y, "Wagonnet", "cart", ComboConfig.cartDelay);
        y += row;
        addDelayRow(left, y, "Allumage", "ignite", ComboConfig.igniteDelay);
        y += row;
        addDelayRow(left, y, "Arbalète", "crossbow", ComboConfig.crossbowDelay);
        y += row + 5;

        addToggle(left, y, "Afficher le HUD", ComboConfig.showHud,
                () -> { ComboConfig.showHud = !ComboConfig.showHud; ComboConfig.save(); });
        addToggle(left + 160, y, "Restaurer le slot", ComboConfig.autoRestoreSlot,
                () -> { ComboConfig.autoRestoreSlot = !ComboConfig.autoRestoreSlot; ComboConfig.save(); });
        y += row;
        addToggle(left, y, "Pause si menu ouvert", ComboConfig.pauseOnScreen,
                () -> { ComboConfig.pauseOnScreen = !ComboConfig.pauseOnScreen; ComboConfig.save(); });

        addDrawableChild(ButtonWidget.builder(Text.literal("FERMER"), b -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 28, 150, 20).build());
    }

    private void addHeader(int x, int y, String label) {
        addDrawableChild(ButtonWidget.builder(Text.literal("◆ " + label), b -> {})
                .dimensions(x, y, 310, 19).build()).active = false;
    }

    private void addKeyButton(int x, int y, int width, String label, KeyBinding binding) {
        ButtonWidget button = ButtonWidget.builder(keyText(label, binding), b -> {
            waitingForKey = true;
            targetBinding = binding;
            targetLabel = label;
            b.setMessage(Text.literal(label + ": appuie sur une touche..."));
        }).dimensions(x, y, width, 20).build();
        refreshable.add(button);
        addDrawableChild(button);
    }

    private Text keyText(String label, KeyBinding binding) {
        return Text.literal(label + " : " + ComboConfig.keyName(binding));
    }

    private void addDelayRow(int x, int y, String label, String id, int value) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label + " : " + value), b -> {})
                .dimensions(x, y, 160, 20).build()).active = false;
        addDrawableChild(ButtonWidget.builder(Text.literal("−"), b -> {
            ComboConfig.changeDelay(id, -1);
            clearAndInit();
        }).dimensions(x + 170, y, 45, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
            ComboConfig.changeDelay(id, 1);
            clearAndInit();
        }).dimensions(x + 220, y, 45, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("±5"), b -> {
            ComboConfig.changeDelay(id, 5);
            clearAndInit();
        }).dimensions(x + 270, y, 40, 20).build());
    }

    private void addToggle(int x, int y, String label, boolean enabled, Runnable action) {
        addDrawableChild(ButtonWidget.builder(
                Text.literal(label + " : " + (enabled ? "ON" : "OFF")),
                b -> { action.run(); clearAndInit(); }
        ).dimensions(x, y, 150, 20).build());
    }

    private void clearAndInit() {
        clearChildren();
        init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForKey && targetBinding != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForKey = false;
                targetBinding = null;
                clearAndInit();
                return true;
            }
            targetBinding.setBoundKey(net.minecraft.client.util.InputUtil.fromKeyCode(keyCode, scanCode));
            KeyBinding.updateKeysByCode();
            MinecraftClient.getInstance().options.write();
            waitingForKey = false;
            targetBinding = null;
            clearAndInit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, width, height, 0xD9080303);
        context.fill(width / 2 - 160, 24, width / 2 + 160, height - 20, 0xE9100808);
        context.fill(width / 2 - 160, 24, width / 2 + 160, 26, 0xFFFF3030);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFF4545);
        if (waitingForKey) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Appuie sur la nouvelle touche pour : " + targetLabel + " (Échap = annuler)"),
                    width / 2, height - 43, 0xFFFF7777);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
