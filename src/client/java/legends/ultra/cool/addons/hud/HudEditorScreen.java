package legends.ultra.cool.addons.hud;

import legends.ultra.cool.addons.data.WidgetConfigManager;
import legends.ultra.cool.addons.hud.widget.settings.ColorPicker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import javax.naming.Context;

public class HudEditorScreen extends Screen {
    private static final int PANEL_WIDTH = 120;
    private static final int HEADER_HEIGHT = 16;
    private static final int ROW_HEIGHT = 14;

    private HudWidget dragging;
    private double lastMouseX, lastMouseY;
    private boolean panelExpanded = true;

    private HudWidget settingsWidget = null; // null = closed
    private final int MODAL_W = 220;
    private final int MODAL_H = 140;

    private String draggingSliderKey = null;

    private boolean isSettingsOpen() {
        return settingsWidget != null;
    }

    private ColorPicker colorPicker = null;
    private PickerTarget openPicker = PickerTarget.NONE;

    private enum PickerTarget {NONE, BG, TEXT, BORDER}

    private int modalX() {
        int base = (this.width - MODAL_W) / 2;

        if (colorPicker != null) {
            int pickerW = colorPicker.getWidth();
            int gap = 8;
            return base - (pickerW + gap) / 2;
        }

        return base;
    }


    private int modalY() {
        return (this.height - MODAL_H) / 2;
    }

    private int panelX() {
        return this.width - PANEL_WIDTH;
    }

    public HudEditorScreen() {
        super(Text.literal("HUD Editor"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = panelX();

        // Header click (collapse/expand) FIRST
        int headerLeft = panelExpanded ? x : x + PANEL_WIDTH - HEADER_HEIGHT;
        int headerRight = x + PANEL_WIDTH;

        if (isSettingsOpen()) {
            if (colorPicker != null && colorPicker.mouseClicked(mouseX, mouseY, button)) return true;
            return handleSettingsClick(mouseX, mouseY, button);
        }

        if (mouseX >= headerLeft && mouseX <= headerRight
                && mouseY >= 0 && mouseY <= HEADER_HEIGHT) {
            panelExpanded = !panelExpanded;
            return true;
        }

        // If panel is collapsed, only allow dragging widgets on canvas
        if (!panelExpanded) {
            for (HudWidget widget : HudManager.getWidgets()) {
                if (!widget.isEnabled()) continue;
                if (widget.isMouseOver(mouseX, mouseY)) {
                    dragging = widget;
                    lastMouseX = mouseX;
                    lastMouseY = mouseY;
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        // Panel toggle clicks
        int y = HEADER_HEIGHT + 4;
        for (HudWidget widget : HudManager.getWidgets()) {
            int x1 = x + 5;
            int y1 = y;
            int x2 = x + PANEL_WIDTH - ROW_HEIGHT - 5;
            int y2 = y + ROW_HEIGHT;

            if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2 && widget != null) {
                widget.toggle();
                WidgetConfigManager.updateWidget(widget);
                return true;
            }

            //settings
            int sx1 = x + PANEL_WIDTH - ROW_HEIGHT - 5;
            int sy1 = y;
            int sx2 = x + PANEL_WIDTH - 5;
            int sy2 = y + ROW_HEIGHT;

            if (mouseX >= sx1 && mouseX <= sx2 && mouseY >= sy1 && mouseY <= sy2 && widget != null) {
                settingsWidget = widget;
                return true;
            }

            y += ROW_HEIGHT + 2;
        }

        // Dragging selection on canvas (after panel clicks)
        for (HudWidget widget : HudManager.getWidgets()) {
            if (!widget.isEnabled()) continue;
            if (widget.isMouseOver(mouseX, mouseY)) {
                dragging = widget;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        // If modal open, drag sliders first
        if (isSettingsOpen()) {
            if (colorPicker != null && colorPicker.mouseDragged(mouseX, mouseY, button, dx, dy)) return true;
            return true;
        }

        if (dragging != null) {
            dragging.x += dx;
            dragging.y += dy;

            dragging.x = Math.max(0, Math.min(dragging.x, this.width - dragging.getWidth()));
            dragging.y = Math.max(0, Math.min(dragging.y, this.height - dragging.getHeight()));
            return true;
        }

        if (isSettingsOpen() && draggingSliderKey != null) {
            // We re-run layout and find the slider row again by key
            SettingsLayout l = beginSettingsLayout();

            for (HudWidget.HudSetting s : settingsWidget.getSettings()) {
                // Compute rowY by consuming rows in the same order
                if (s.type() == HudWidget.HudSetting.Type.TOGGLE) {
                    nextRowY(l);
                } else if (s.type() == HudWidget.HudSetting.Type.COLOR) {
                    nextRowY(l);
                } else if (s.type() == HudWidget.HudSetting.Type.SLIDER) {
                    int rowY = nextRowY(l);

                    if (!s.key().equals(draggingSliderKey)) continue;

                    int barX = l.btnX - 70;
                    int barW = 120;

                    float t = (float)((mouseX - barX) / (double)barW);
                    t = Math.max(0f, Math.min(1f, t));
                    float raw = s.min() + t * (s.max() - s.min());
                    float snapped = (s.step() > 0f) ? (Math.round(raw / s.step()) * s.step()) : raw;
                    snapped = Math.max(s.min(), Math.min(s.max(), snapped));

                    // Save as float setting
                    WidgetConfigManager.setFloat(settingsWidget.getName(), s.key(), snapped, true);
                    return true;
                }
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isSettingsOpen()) {
            if (colorPicker != null && colorPicker.mouseReleased(mouseX, mouseY, button)) {
                WidgetConfigManager.updateWidget(settingsWidget);
                return true;
            }
            return true;
        }

        if (dragging != null) {
            WidgetConfigManager.updateWidget(dragging);
            dragging = null;
            return true;
        }

        if (isSettingsOpen()) {
            draggingSliderKey = null;
        }


        return super.mouseReleased(mouseX, mouseY, button);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void openPicker(PickerTarget target, int px, int py, int pw, int ph) {
        openPicker = target;

        if (target == PickerTarget.NONE) {
            colorPicker = null;
            return;
        }

        colorPicker = switch (target) {
            case BG -> new ColorPicker(px, py, pw,
                    () -> settingsWidget.style.backgroundColor,
                    c -> settingsWidget.style.backgroundColor = c);
            case BORDER -> new ColorPicker(px, py, pw,
                    () -> settingsWidget.style.borderColor,
                    c -> settingsWidget.style.borderColor = c);
            case TEXT -> new ColorPicker(px, py, pw,
                    () -> settingsWidget.style.textColor,
                    c -> settingsWidget.style.textColor = c);
            default -> null;
        };
    }

    private boolean handleSettingsClick(double mouseX, double mouseY, int button) {
        int x = modalX();
        int y = modalY();

        // Close "✕"
        int closeX = x + MODAL_W - 14;
        int closeY = y + 6;
        int cw = textRenderer.getWidth("✕");
        int ch = textRenderer.fontHeight;

        if (inside(mouseX, mouseY, closeX, closeY, cw, ch)) {
            WidgetConfigManager.updateWidget(settingsWidget);
            settingsWidget = null;
            openPicker(PickerTarget.NONE, 0, 0, 0, 0);
            return true;
        }

        SettingsLayout l = beginSettingsLayout();

        if (rowToggle(null, l, mouseX, mouseY, "Background", settingsWidget.style.drawBackground, () -> {
            settingsWidget.style.drawBackground = !settingsWidget.style.drawBackground;
            WidgetConfigManager.updateWidget(settingsWidget);
            if (!settingsWidget.style.drawBackground && openPicker == PickerTarget.BG) {
                openPicker(PickerTarget.NONE, 0, 0, 0, 0);
            }
        })) return true;

        if (rowColorPicker(null, l, mouseX, mouseY, "BG Color", settingsWidget.style.drawBackground, settingsWidget.style.backgroundColor, PickerTarget.BG)) return true;

        if (rowToggle(null, l, mouseX, mouseY, "Border", settingsWidget.style.drawBorder, () -> {
            settingsWidget.style.drawBorder = !settingsWidget.style.drawBorder;
            WidgetConfigManager.updateWidget(settingsWidget);
            if (!settingsWidget.style.drawBorder && openPicker == PickerTarget.BORDER) {
                openPicker(PickerTarget.NONE, 0, 0, 0, 0);
            }
        })) return true;

        if (rowColorPicker(null, l, mouseX, mouseY, "Border Color", settingsWidget.style.drawBorder, settingsWidget.style.borderColor, PickerTarget.BORDER)) return true;

        if (rowColorPicker(null, l, mouseX, mouseY, "Text Color", true, settingsWidget.style.textColor, PickerTarget.TEXT)) return true;

        return true;
    }

    private void renderPanelHeader(DrawContext context) {
        int x = panelX();
        int y = 0;

        context.fill(
                panelExpanded ? x : x + PANEL_WIDTH - HEADER_HEIGHT, y,
                x + PANEL_WIDTH, HEADER_HEIGHT,
                0xCC000000
        );

        String arrow = panelExpanded ? "▶" : "☰";

        context.drawText(
                MinecraftClient.getInstance().textRenderer,
                panelExpanded ? arrow + " Widgets" : arrow,
                panelExpanded ? x + 6 : x + PANEL_WIDTH - HEADER_HEIGHT + 6,
                y + 4,
                0xFFFFFF,
                false
        );
    }


    private void renderWidgetList(DrawContext context) {
        if (!panelExpanded) return;

        int x = panelX();
        int y = HEADER_HEIGHT + 4;

        context.fill(
                x,
                HEADER_HEIGHT,
                x + PANEL_WIDTH,
                this.height,
                0xAA000000
        );

        for (HudWidget widget : HudManager.getWidgets()) {
            int bgColor = widget.isEnabled()
                    ? 0xFF2ECC71
                    : 0xFF7F8C8D;

            context.fill(
                    x + 5,
                    y,
                    x + PANEL_WIDTH - 5,
                    y + ROW_HEIGHT,
                    bgColor
            );

            context.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    widget.getName(),
                    x + 8,
                    y + 3,
                    0x000000,
                    false
            );

            context.fill(
                    x + PANEL_WIDTH - ROW_HEIGHT - 5,
                    y,
                    x + PANEL_WIDTH - 5,
                    y + ROW_HEIGHT,
                    0xFF7F8C8D
            );


            context.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    "⚙",
                    x + PANEL_WIDTH - ROW_HEIGHT - 1,
                    y + 3,
                    0xFFFFFF,
                    false
            );

            y += ROW_HEIGHT + 2;
        }
    }

    private void renderSettingsModal(DrawContext ctx, int mouseX, int mouseY) {
        if (settingsWidget == null) return;

        int x = modalX();
        int y = modalY();
        int gap = 6;

        // dim background
        ctx.fill(0, 0, this.width, this.height, 0x88000000);

        // panel
        ctx.fill(x, y, x + MODAL_W, y + MODAL_H, 0xFF111111);
        ctx.drawBorder(x, y, MODAL_W, MODAL_H, 0xFFFFFFFF);

        // title
        ctx.drawText(textRenderer,
                settingsWidget.getName() + " Settings",
                x + 8, y + 8,
                0xFFFFFF, false
        );

        // close button (text)
        String close = "✕";
        int closeX = x + MODAL_W - 14;
        int closeY = y + 6;
        ctx.drawText(textRenderer, close, closeX, closeY, 0xFFFFFF, false);

        SettingsLayout l = beginSettingsLayout();

        for (HudWidget.HudSetting s : settingsWidget.getSettings()) {
            switch (s.type()) {
                case TOGGLE -> {
                    boolean val = WidgetConfigManager.getBool(settingsWidget.getName(), s.key(), false);

                    // Special-case: map legacy style toggles to current fields if you want
                    // or just store everything in config only.
                    rowToggle(ctx, l, mouseX, mouseY, s.label(), val, () -> {});
                }
                case COLOR -> {
                    int argb = WidgetConfigManager.getInt(settingsWidget.getName(), s.key(), 0xFFFFFFFF);
                    // You can map keys to PickerTarget, or add a new PickerTarget for arbitrary keys later
                    // For now, keep only these 3 supported:
                    PickerTarget target =
                            s.key().equals("bgColor") ? PickerTarget.BG :
                                    s.key().equals("borderColor") ? PickerTarget.BORDER :
                                            PickerTarget.TEXT;

                    rowColorPicker(ctx, l, mouseX, mouseY, s.label(), true, argb, target);
                }
                case SLIDER -> {
                    float val = WidgetConfigManager.getFloat(settingsWidget.getName(), s.key(), s.min());
                    rowSlider(ctx, l, mouseX, mouseY, s.label(), val, s.min(), s.max(), s.step(),
                            s.key(),
                            newVal -> WidgetConfigManager.setFloat(settingsWidget.getName(), s.key(), newVal, true)
                    );
                }
            }
        }

        // Render picker (if open)
        if (colorPicker != null) {
            int pickerW = colorPicker.getWidth();

            int px = modalX() + MODAL_W + gap;
            int py = modalY();

            if (px + pickerW > this.width) {
                px = modalX() - pickerW - gap;
            }

            colorPicker.setPos(px, py);
            colorPicker.render(ctx, mouseX, mouseY);
        }
    }

    private void drawPickButton(DrawContext ctx, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = hovered ? 0xFF444444 : 0xFF333333;
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.drawBorder(x, y, w, h, 0xFF000000);
        ctx.drawText(textRenderer, "Pick", x + 18, y + 3, 0xFFFFFF, false);
    }

    private void drawSwatch(DrawContext ctx, int x, int y, int color) {
        ctx.fill(x, y, x + 12, y + 12, color);
        ctx.drawBorder(x, y, 12, 12, 0xFF000000);
    }

    private void drawTogglePill(DrawContext ctx, int x, int y, int w, int h, boolean on) {
        int bg = on ? 0xFF2ECC71 : 0xFF7F8C8D;
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.drawBorder(x, y, w, h, 0xFF000000);
        ctx.drawText(textRenderer, on ? "ON" : "OFF", x + 10, y + 3, 0xFF000000, false);
    }

    // ---- Settings row builder (render + click share the same row order) ----
    private static final int SETTINGS_ROW_H = 16;
    private static final int SETTINGS_ROW_GAP = 6;

    private int settingsRowIndex = 0;

    private static final class SettingsLayout {
        final int x, y;
        final int startX, startY;
        final int btnW, btnH, btnX;
        final int toggleW, toggleX;
        final int pickerX, pickerY, pickerW, pickerH;

        SettingsLayout(int x, int y, int startX, int startY,
                       int btnW, int btnH, int btnX,
                       int toggleW, int toggleX,
                       int pickerX, int pickerY, int pickerW, int pickerH) {
            this.x = x;
            this.y = y;
            this.startX = startX;
            this.startY = startY;
            this.btnW = btnW;
            this.btnH = btnH;
            this.btnX = btnX;
            this.toggleW = toggleW;
            this.toggleX = toggleX;
            this.pickerX = pickerX;
            this.pickerY = pickerY;
            this.pickerW = pickerW;
            this.pickerH = pickerH;
        }
    }

    private SettingsLayout beginSettingsLayout() {
        settingsRowIndex = 0;

        int x = modalX();
        int y = modalY();

        int startX = x + 8;
        int startY = y + 28;

        int btnW = 60;
        int btnH = 14;
        int btnX = x + MODAL_W - 8 - btnW;

        int toggleW = 42;
        int toggleX = x + MODAL_W - 8 - toggleW;

        // Picker placement “near modal” like your current code
        int pickerW = 180;
        int pickerH = 150;

        int pickerX = x + 10;
        int pickerY = y + MODAL_H - pickerH - 10;

        return new SettingsLayout(x, y, startX, startY, btnW, btnH, btnX, toggleW, toggleX, pickerX, pickerY, pickerW, pickerH);
    }

    private int nextRowY(SettingsLayout l) {
        int rowY = l.startY + settingsRowIndex * (SETTINGS_ROW_H + SETTINGS_ROW_GAP);
        settingsRowIndex++;
        return rowY;
    }

    private boolean rowToggle(DrawContext ctx, SettingsLayout l, double mx, double my,
                              String label, boolean value, Runnable onToggle) {
        int rowY = nextRowY(l);

        // render
        if (ctx != null) {
            ctx.drawText(textRenderer, label, l.startX, rowY, 0xFFFFFF, false);
            drawTogglePill(ctx, l.toggleX, rowY - 2, l.toggleW, l.btnH, value);
        }

        // click
        if (ctx == null) {
            if (inside(mx, my, l.toggleX, rowY - 2, l.toggleW, l.btnH)) {
                onToggle.run();
                return true;
            }
        }

        return false;
    }

    private boolean rowColorPicker(DrawContext ctx, SettingsLayout l, double mx, double my,
                                   String label, boolean enabled, int argb,
                                   PickerTarget target) {
        int rowY = nextRowY(l);

        // render
        if (ctx != null) {
            ctx.drawText(textRenderer, label, l.startX, rowY, 0xFFFFFF, false);

            if (enabled) {
                drawPickButton(ctx, l.btnX, rowY - 2, l.btnW, l.btnH, (int) mx, (int) my);
                drawSwatch(ctx, l.btnX - 18, rowY - 1, argb);
            } else {
                ctx.drawText(textRenderer, "-", l.btnX + 26, rowY, 0x777777, false);
            }
        }

        // click
        if (ctx == null && enabled) {
            if (inside(mx, my, l.btnX, rowY - 2, l.btnW, l.btnH)) {
                if (openPicker == target) openPicker(PickerTarget.NONE, 0, 0, 0, 0);
                else openPicker(target, l.pickerX, l.pickerY, l.pickerW, l.pickerH);
                return true;
            }
        }

        return false;
    }

    private boolean rowSlider(DrawContext ctx, SettingsLayout l, double mx, double my,
                              String label,
                              float value, float min, float max, float step,
                              String sliderKey,
                              java.util.function.Consumer<Float> onChange) {
        int rowY = nextRowY(l);

        int barX = l.btnX - 70;     // slider bar left (tweak if you want)
        int barY = rowY - 1;
        int barW = 120;
        int barH = 12;

        // render
        if (ctx != null) {
            ctx.drawText(textRenderer, label, l.startX, rowY, 0xFFFFFF, false);

            // bar background
            ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF2A2A2A);
            ctx.drawBorder(barX, barY, barW, barH, 0xFF000000);

            float t = (max == min) ? 0f : (value - min) / (max - min);
            t = Math.max(0f, Math.min(1f, t));

            int knobX = barX + (int)(t * (barW - 4));
            ctx.fill(knobX, barY, knobX + 4, barY + barH, 0xFF7F8C8D);

            // value text
            String v = String.format("%.2f", value);
            ctx.drawText(textRenderer, v, barX + barW + 6, rowY, 0xAAAAAA, false);
        }

        // click/drag start
        if (ctx == null) {
            if (inside(mx, my, barX, barY, barW, barH)) {
                draggingSliderKey = sliderKey;

                // set immediately on click
                float t = (float)((mx - barX) / (double)barW);
                t = Math.max(0f, Math.min(1f, t));
                float raw = min + t * (max - min);

                float snapped = (step > 0f) ? (Math.round(raw / step) * step) : raw;
                snapped = Math.max(min, Math.min(max, snapped));
                onChange.accept(snapped);
                return true;
            }
        }

        return false;
    }

    private static String toHex(int argb) {
        return String.format("#%08X", argb);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        for (HudWidget widget : HudManager.getWidgets()) {
            if (widget.isEnabled()) {
                widget.render(context);
            }
        }

        renderWidgetList(context);
        renderPanelHeader(context);
        renderSettingsModal(context, mouseX, mouseY);
    }
}

