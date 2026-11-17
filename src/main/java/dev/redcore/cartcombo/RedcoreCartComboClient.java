package dev.redcore.cartcombo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class RedcoreCartComboClient implements ClientModInitializer {
    public static KeyBinding startKey;
    public static KeyBinding cancelKey;
    public static KeyBinding configKey;

    private enum Phase { IDLE, PLACE_RAIL, PLACE_CART, IGNITE_CART, FIRE_CROSSBOW, DONE, ERROR }

    private static Phase phase = Phase.IDLE;
    private static String message = "F8: combo | F9: annuler | F10: options";
    private static int waitTicks;
    private static int originalSlot = -1;
    private static int railSlot = -1, cartSlot = -1, flintSlot = -1, crossbowSlot = -1;

    @Override
    public void onInitializeClient() {
        ComboConfig.load();

        startKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redcore_cart_combo.start", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F8,
                "category.redcore_cart_combo"));
        cancelKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redcore_cart_combo.cancel", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F9,
                "category.redcore_cart_combo"));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.redcore_cart_combo.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F10,
                "category.redcore_cart_combo"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) client.setScreen(new RedcoreConfigScreen(client.currentScreen));
            while (startKey.wasPressed()) startCombo(client);
            while (cancelKey.wasPressed()) cancelCombo(client, "Combo annulé.");
            tickCombo(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!ComboConfig.showHud || client.player == null || client.options.hudHidden) return;
            int x = 8, y = 8;
            drawContext.fill(x - 4, y - 4, x + 238, y + 43, 0xB5090505);
            drawContext.fill(x - 4, y - 4, x + 238, y - 3, 0xFFFF3030);
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal("REDCORE // CART COMBO"),
                    x, y, 0xFFFF4545);
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal("ÉTAPE: " + phaseLabel()),
                    x, y + 12, 0xFFFFFFFF);
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(message),
                    x, y + 25, 0xFFFF9999);
        });
    }

    private static void startCombo(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) {
            phase = Phase.ERROR; message = "Monde non disponible."; return;
        }
        if (phase != Phase.IDLE && phase != Phase.DONE && phase != Phase.ERROR) {
            message = "Combo en cours. Touche Annuler pour arrêter."; return;
        }

        railSlot = findItem(client, Items.RAIL);
        cartSlot = findItem(client, Items.TNT_MINECART);
        flintSlot = findItem(client, Items.FLINT_AND_STEEL);
        crossbowSlot = findChargedCrossbow(client);

        if (railSlot < 0 || cartSlot < 0 || flintSlot < 0 || crossbowSlot < 0) {
            phase = Phase.ERROR;
            message = "Rail, wagonnet TNT, briquet ou arbalète chargée manquant.";
            return;
        }

        originalSlot = client.player.getInventory().selectedSlot;
        phase = Phase.PLACE_RAIL;
        waitTicks = 0;
        message = "Vise le bloc où poser le rail.";
    }

    private static void tickCombo(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        if (phase == Phase.IDLE || phase == Phase.DONE || phase == Phase.ERROR) return;
        if (ComboConfig.pauseOnScreen && client.currentScreen != null) return;
        if (waitTicks > 0) { waitTicks--; return; }

        switch (phase) {
            case PLACE_RAIL -> {
                if (!(client.crosshairTarget instanceof BlockHitResult hit)
                        || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
                    fail(client, "Vise un bloc pour poser le rail."); return;
                }
                selectSlot(client, railSlot);
                ActionResult result = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
                client.player.swingHand(Hand.MAIN_HAND);
                if (result == ActionResult.FAIL) { fail(client, "Placement du rail refusé."); return; }
                phase = Phase.PLACE_CART;
                waitTicks = ComboConfig.railDelay;
                message = "Rail demandé. Garde le viseur sur le rail.";
            }
            case PLACE_CART -> {
                if (!(client.crosshairTarget instanceof BlockHitResult hit)) {
                    fail(client, "Vise le rail pour poser le wagonnet."); return;
                }
                selectSlot(client, cartSlot);
                ActionResult result = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
                client.player.swingHand(Hand.MAIN_HAND);
                if (result == ActionResult.FAIL) { fail(client, "Pose du wagonnet refusée."); return; }
                phase = Phase.IGNITE_CART;
                waitTicks = ComboConfig.cartDelay;
                message = "Wagonnet demandé. Vise le wagonnet TNT.";
            }
            case IGNITE_CART -> {
                if (!(client.crosshairTarget instanceof EntityHitResult entityHit)
                        || !(entityHit.getEntity() instanceof TntMinecartEntity)) {
                    fail(client, "Vise le wagonnet TNT avec ton réticule."); return;
                }
                selectSlot(client, flintSlot);
                ActionResult result = client.interactionManager.interactEntity(client.player, entityHit.getEntity(), Hand.MAIN_HAND);
                client.player.swingHand(Hand.MAIN_HAND);
                if (result == ActionResult.FAIL) { fail(client, "Allumage refusé."); return; }
                phase = Phase.FIRE_CROSSBOW;
                waitTicks = ComboConfig.igniteDelay + ComboConfig.crossbowDelay;
                message = "Allumage demandé. Vise ta cible.";
            }
            case FIRE_CROSSBOW -> {
                selectSlot(client, crossbowSlot);
                ItemStack stack = client.player.getMainHandStack();
                if (!CrossbowItem.isCharged(stack)) {
                    fail(client, "Arbalète déchargée : recharge-la puis recommence."); return;
                }
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                client.player.swingHand(Hand.MAIN_HAND);
                finish(client, "Séquence envoyée !");
            }
            default -> { }
        }
    }

    private static int findItem(MinecraftClient client, net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++)
            if (client.player.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }

    private static int findChargedCrossbow(MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(stack)) return i;
        }
        return -1;
    }

    private static void selectSlot(MinecraftClient client, int slot) {
        client.player.getInventory().selectedSlot = slot;
    }

    private static void finish(MinecraftClient client, String text) {
        restoreSlot(client); phase = Phase.DONE; message = text;
    }

    private static void fail(MinecraftClient client, String text) {
        restoreSlot(client); phase = Phase.ERROR; message = text;
    }

    private static void cancelCombo(MinecraftClient client, String text) {
        restoreSlot(client); phase = Phase.IDLE; waitTicks = 0; message = text;
    }

    private static void restoreSlot(MinecraftClient client) {
        if (ComboConfig.autoRestoreSlot && client.player != null && originalSlot >= 0 && originalSlot < 9)
            client.player.getInventory().selectedSlot = originalSlot;
        originalSlot = -1;
    }

    private static String phaseLabel() {
        return switch (phase) {
            case IDLE -> "EN ATTENTE";
            case PLACE_RAIL -> "POSE DU RAIL";
            case PLACE_CART -> "POSE DU WAGONNET TNT";
            case IGNITE_CART -> "ALLUMAGE";
            case FIRE_CROSSBOW -> "TIR ARBALÈTE";
            case DONE -> "TERMINÉ";
            case ERROR -> "ERREUR";
        };
    }
}
