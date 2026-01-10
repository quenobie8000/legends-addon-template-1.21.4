package legends.ultra.cool.addons.hud;

import legends.ultra.cool.addons.data.WidgetConfigManager;
import legends.ultra.cool.addons.hud.widget.settings.ColorPicker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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

        // Layout
        int startX = x + 8;
        int startY = y + 28;
        int rowH = 16;
        int gap = 6;

        int btnW = 60;
        int btnH = 14;
        int btnX = x + MODAL_W - 8 - btnW;

        // Picker placement (inside modal, below rows)
        int pickerH = 150;
        int pickerW = 180;
        int pickerX = modalX() + MODAL_W + gap;
        int pickerY = modalY();


        // Row 0: BG toggle
        int row0Y = startY + 0 * (rowH + gap);
        int toggleW = 42;
        int toggleX = x + MODAL_W - 8 - toggleW;
        int toggleY = row0Y - 2;

        if (inside(mouseX, mouseY, toggleX, toggleY, toggleW, btnH)) {
            settingsWidget.style.drawBackground = !settingsWidget.style.drawBackground;
            WidgetConfigManager.updateWidget(settingsWidget);
            return true;
        }

        // Row 1: BG color pick button
        int row1Y = startY + 1 * (rowH + gap);
        int btnY1 = row1Y - 2;

        if (inside(mouseX, mouseY, btnX, btnY1, btnW, btnH)) {
            if (openPicker == PickerTarget.BG) openPicker(PickerTarget.NONE, 0, 0, 0, 0);
            else openPicker(PickerTarget.BG, pickerX, pickerY, pickerW, pickerH);
            return true;
        }

        // Row 2: Border toggle
        int row2Y = startY + 2 * (rowH + gap);
        int toggleY2 = row2Y - 2;

        if (inside(mouseX, mouseY, toggleX, toggleY2, toggleW, btnH)) {
            settingsWidget.style.drawBorder = !settingsWidget.style.drawBorder;
            WidgetConfigManager.updateWidget(settingsWidget);
            // If border turned off, close border picker
            if (!settingsWidget.style.drawBorder && openPicker == PickerTarget.BORDER) {
                openPicker(PickerTarget.NONE, 0, 0, 0, 0);
            }
            return true;
        }

        // Row 3: Border color pick button (only if border enabled)
        int row3Y = startY + 3 * (rowH + gap);
        int btnY3 = row3Y - 2;

        if (settingsWidget.style.drawBorder && inside(mouseX, mouseY, btnX, btnY3, btnW, btnH)) {
            if (openPicker == PickerTarget.BORDER) openPicker(PickerTarget.NONE, 0, 0, 0, 0);
            else openPicker(PickerTarget.BORDER, pickerX, pickerY, pickerW, pickerH);
            return true;
        }

        // Row 4: Text color pick button
        int row4Y = startY + 4 * (rowH + gap);
        int btnY4 = row4Y - 2;

        if (inside(mouseX, mouseY, btnX, btnY4, btnW, btnH)) {
            if (openPicker == PickerTarget.TEXT) openPicker(PickerTarget.NONE, 0, 0, 0, 0);
            else openPicker(PickerTarget.TEXT, pickerX, pickerY, pickerW, pickerH);
            return true;
        }

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


        // Layout
        int startX = x + 8;
        int startY = y + 28;
        int rowH = 16;
        int gap = 6;

        int btnW = 60;
        int btnH = 14;
        int btnX = x + MODAL_W - 8 - btnW;

        int toggleW = 42;
        int toggleX = x + MODAL_W - 8 - toggleW;

        // Row 0: BG toggle
        int row0Y = startY + 0 * (rowH + gap);
        ctx.drawText(textRenderer, "Background", startX, row0Y, 0xFFFFFF, false);
        drawTogglePill(ctx, toggleX, row0Y - 2, toggleW, btnH, settingsWidget.style.drawBackground);

        // Row 1: BG color
        int row1Y = startY + 1 * (rowH + gap);
        ctx.drawText(textRenderer, "BG Color", startX, row1Y, 0xFFFFFF, false);
        if (settingsWidget.style.drawBackground) {
            drawPickButton(ctx, btnX, row1Y - 2, btnW, btnH, mouseX, mouseY);
            drawSwatch(ctx, btnX - 18, row1Y - 1, settingsWidget.style.backgroundColor);
        } else {
            ctx.drawText(textRenderer, "-", btnX + 26, row1Y, 0x777777, false);
        }

        // Row 2: Border toggle
        int row2Y = startY + 2 * (rowH + gap);
        ctx.drawText(textRenderer, "Border", startX, row2Y, 0xFFFFFF, false);
        drawTogglePill(ctx, toggleX, row2Y - 2, toggleW, btnH, settingsWidget.style.drawBorder);

        // Row 3: Border color (only if border enabled)
        int row3Y = startY + 3 * (rowH + gap);
        ctx.drawText(textRenderer, "Border Color", startX, row3Y, 0xFFFFFF, false);
        if (settingsWidget.style.drawBorder) {
            drawPickButton(ctx, btnX, row3Y - 2, btnW, btnH, mouseX, mouseY);
            drawSwatch(ctx, btnX - 18, row3Y - 1, settingsWidget.style.borderColor);
        } else {
            ctx.drawText(textRenderer, "-", btnX + 26, row3Y, 0x777777, false);
        }

        // Row 4: Text color
        int row4Y = startY + 4 * (rowH + gap);
        ctx.drawText(textRenderer, "Text Color", startX, row4Y, 0xFFFFFF, false);
        drawPickButton(ctx, btnX, row4Y - 2, btnW, btnH, mouseX, mouseY);
        drawSwatch(ctx, btnX - 18, row4Y - 1, settingsWidget.style.textColor);

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

