package wandweaver.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import wandweaver.WandWeaver;
import wandweaver.spells.ISpell;
import wandweaver.utils.Direction;
import java.util.List;

public class SpellcastingRenderer {
    private static final Identifier SPELL_CASTING_LAYER = Identifier.of(WandWeaver.MOD_ID, "spell-casting-layer");

    private static Identifier arrowUp;
    private static Identifier arrowUpProgress;
    private static Identifier arrowFrame;

    // Assuming all texture sizes here will be 16 pixels.
    private static final int TEXTURE_SIZE = 16;

    private static Identifier getArrowTexture() {
        if (arrowUp == null) {
            arrowUp = Identifier.of(WandWeaver.MOD_ID, "textures/gui/arrow-up.png");
        }

        return arrowUp;
    }

    private static Identifier getArrowProgressTexture() {
        if (arrowUpProgress == null) {
            arrowUpProgress = Identifier.of(WandWeaver.MOD_ID, "textures/gui/arrow-up-progress.png");
        }

        return arrowUpProgress;
    }

    private static Identifier getArrowFrameTexture() {
        if (arrowFrame == null) {
            arrowFrame = Identifier.of(WandWeaver.MOD_ID, "textures/gui/arrow-frame.png");
        }

        return arrowFrame;
    }

    private static void renderWithDirection(DrawContext context, Direction direction, int x, int y, Identifier upTexture) {
        context.getMatrices().push();

        // Offsetting the coordinates to correct the position after rotating the textures.
        if (direction == Direction.DOWN || direction == Direction.RIGHT) x += TEXTURE_SIZE;
        if (direction == Direction.DOWN || direction == Direction.LEFT) y += TEXTURE_SIZE;

        context.getMatrices().translate(x, y, 0);

        float rotationDegrees = switch (direction) {
            case UP -> 0f;
            case RIGHT -> 90f;
            case DOWN -> 180f;
            case LEFT -> 270f;
        };

        if (rotationDegrees != 0) {
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotation((float) Math.toRadians(rotationDegrees)));
        }

        context.drawTexture(
                RenderLayer::getGuiTextured, upTexture,
                0, 0,
                0, 0,
                TEXTURE_SIZE, TEXTURE_SIZE,
                TEXTURE_SIZE, TEXTURE_SIZE
        );

        context.getMatrices().pop();
    }

    private static void renderArrow(DrawContext context, Direction direction, int x, int y) {
        renderWithDirection(
                context, direction, x, y,
                getArrowTexture()
        );
    }

    private static void renderArrowProgress(DrawContext context, Direction direction, int x, int y, int progress) {
        context.drawTexture(
                RenderLayer::getGuiTextured, SpellcastingRenderer.getArrowFrameTexture(),
                x, y,
                0, 0,
                TEXTURE_SIZE, TEXTURE_SIZE,
                TEXTURE_SIZE, TEXTURE_SIZE
        );

        int scissorX = x;
        int scissorY = y;
        int width = x + TEXTURE_SIZE;
        int height = y + TEXTURE_SIZE;

        switch (direction) {
            case UP -> {
                height = scissorY + TEXTURE_SIZE;
                scissorY = height - getPartial(progress);
            }
            case DOWN -> {
                height = scissorY + getPartial(progress);
            }
            case LEFT -> {
                width = scissorX + TEXTURE_SIZE;
                scissorX = width - getPartial(progress);
            }
            case RIGHT -> {
                width = scissorX + getPartial(progress);
            }
        }

        context.enableScissor(
                scissorX, scissorY,
                width, height
        );

        renderWithDirection(
                context, direction, x, y,
                getArrowProgressTexture()
        );

        context.disableScissor();
    }

    public static void renderArrowsOnScreen(
            DrawContext context,
            List<Direction> directions,
            int leftProgress,
            int rightProgress,
            int upProgress,
            int downProgress) {
        // Subtract 8 from the x and y coordinates to center the arrows
        int x = (MinecraftClient.getInstance().getWindow().getScaledWidth() / 2) - 8;
        int y = (MinecraftClient.getInstance().getWindow().getScaledHeight() / 2) - 8;
        boolean first = true;

        for (Direction direction : directions) {
            if (!first) {
                // Adding one to create a 1 pixel gap between the arrows.
                switch (direction) {
                    case UP -> y -= TEXTURE_SIZE + 1;
                    case DOWN -> y += TEXTURE_SIZE + 1;
                    case LEFT -> x -= TEXTURE_SIZE + 1;
                    case RIGHT -> x += TEXTURE_SIZE + 1;
                }
            }

            SpellcastingRenderer.renderArrow(context, direction, x, y);

            first = false;
        }

        // For some reason, there is an offset of 1 pixel when rendering the up and left arrows.
        // I do not know why, but it is there. Maybe I'll see into it one day. Maybe I won't. But with luck, I won't
        // have to touch this code again, so I have elected to make this a problem for my future self.

        // For the record: Before this "bug", I added +1 to all TEXTURE_SIZE instead of -1 to left and up.
        // Honestly, maybe it's not even a bug, but eh.

        // Checking if the progress is greater than 10 so that the arrows do not render with little to no progress.
        if (upProgress > 10) {
            SpellcastingRenderer.renderArrowProgress(
                    context, Direction.UP, x, !first ? y - TEXTURE_SIZE - 1 : y, upProgress);
        }
        if (downProgress > 10) {
            SpellcastingRenderer.renderArrowProgress(
                    context, Direction.DOWN, x, !first ? y + TEXTURE_SIZE + 1 : y, downProgress);
        }
        if (leftProgress > 10) {
            SpellcastingRenderer.renderArrowProgress(
                    context, Direction.LEFT, !first ? x - TEXTURE_SIZE - 1 : x, y, leftProgress);
        }
        if (rightProgress > 10) {
            SpellcastingRenderer.renderArrowProgress(
                    context, Direction.RIGHT, !first ? x + TEXTURE_SIZE + 1 : x, y , rightProgress);
        }
    }

    public static void renderSpellOnScreen(DrawContext context, ISpell spell) {
        int scale = 3;
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        double scaleFactor = client.getWindow().getScaleFactor();
        int nameWidth = textRenderer.getWidth(spell.getName(null)) * scale;
        int x = (client.getWindow().getScaledWidth() / 2) - (nameWidth / 2);
        int y = (int) (((double) client.getWindow().getScaledHeight() / 6) / scaleFactor);

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 0);

        context.drawText(
                textRenderer,
                spell.getName(null),
                0, 0,
                0xFFFFFF,
                true
        );

        int descriptionWidth = textRenderer.getWidth(spell.getDescription(null));
        x = (client.getWindow().getScaledWidth() / 2) - (descriptionWidth / 2);
        y += 10;

        context.getMatrices().pop();
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);

        context.drawText(
               textRenderer,
               spell.getDescription(null),
               0, 35,
                0xFFFFFF,
                true
        );
        context.getMatrices().pop();
    }

    public static void register(LayeredDrawer.Layer drawer) {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
                layeredDrawer.attachLayerBefore(
                        IdentifiedLayer.HOTBAR_AND_BARS,
                        SPELL_CASTING_LAYER,
                        drawer
                )
        );
    }

    private static int getPartial(float percentage) {
        return (int) Math.ceil(((float) TEXTURE_SIZE * percentage) / 100);
    }
}
