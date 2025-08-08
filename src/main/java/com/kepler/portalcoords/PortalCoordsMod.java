package com.kepler.portalcoords;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class PortalCoordsMod implements ClientModInitializer {
    private static boolean showOverlay = false;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.portalcoords.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.portalcoords"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                showOverlay = !showOverlay;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Portal coords overlay " + (showOverlay ? "enabled" : "disabled")), true);
                }
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!showOverlay) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null) return;

            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            String dim = mc.world.getRegistryKey().getValue().toString();

            String currentCoords = String.format("X: %.2f Y: %.2f Z: %.2f", x, y, z);
            String convertedCoords;

            if (dim.contains("overworld")) {
                convertedCoords = String.format("Nether: %.2f, %.2f, %.2f", x / 8.0, y, z / 8.0);
                dim = "Overworld";
            } else if (dim.contains("nether")) {
                convertedCoords = String.format("Overworld: %.2f, %.2f, %.2f", x * 8.0, y, z * 8.0);
                dim = "Nether";
            } else {
                convertedCoords = "N/A";
            }

            int screenWidth = mc.getWindow().getScaledWidth();
            drawContext.drawText(mc.textRenderer, dim, screenWidth - 180, 10, 0xFFFFFF, true);
            drawContext.drawText(mc.textRenderer, currentCoords, screenWidth - 180, 25, 0xFFFFFF, true);
            drawContext.drawText(mc.textRenderer, convertedCoords, screenWidth - 180, 40, 0xFFFFFF, true);
        });
    }
}
