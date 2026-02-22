package legends.ultra.cool.addons.hud.widget.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ColorPicker {

    // Layout
    private int x, y;
    private final int w;

    private static final int PAD = 8;
    private static final int BAR_W = 10;
    private static final int BAR_GAP = 6;
    private static final int PREVIEW_GAP = 8;
    private static final int PREVIEW_H = 14;
    private static final int HEX_GAP = 2;
    private static final int GRADIENT_STEPS = 64;
    private static final int AREA_TOP = 20;
    private static final int BOTTOM_PAD = 4;


    // Reads/writes the color (ARGB int)
    private final IntSupplier getColor;
    private final IntConsumer setColor;

    // Internal drag state
    private static final int DRAG_NONE = 0;
    private static final int DRAG_AREA = 1;
    private static final int DRAG_HUE = 2;
    private static final int DRAG_ALPHA = 3;
    private int dragging = DRAG_NONE;

    // HSV(A) state
    private float hue = 0f;   // 0..1
    private float sat = 0f;   // 0..1
    private float val = 0f;   // 0..1
    private float alpha = 1f; // 0..1
    private int lastColor = 0;

    // Area + bars layout inside modal
    private int areaX, areaY, areaSize;
    private int hueX, hueY, hueW, hueH;
    private int alphaX, alphaY, alphaW, alphaH;
    private int previewX, previewY, previewW, previewH;
    private int hexY;

    public ColorPicker(int x, int y, int w, IntSupplier getColor, IntConsumer setColor) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.getColor = getColor;
        this.setColor = setColor;

        updateLayout();
        syncFromColor();
    }

    public int getHeight() {
        updateLayout();
        int bottom = hexY + 9 + BOTTOM_PAD; // roughly one line of text + padding
        return bottom - y;
    }

    public int getWidth() {
        return this.w;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;

        updateLayout();
    }

    public void render(DrawContext ctx, int mouseX, int mouseY) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        updateLayout();
        syncFromColor();

        int h = getHeight();

        // Panel background
        ctx.fill(x, y, x + w, y + h, 0xFF111111);
        drawBorder(ctx, x, y, w, h, 0xFFFFFFFF);

        // Title
        ctx.drawText(tr, "Color Picker", x + 8, y + 8, 0xFFFFFFFF, false);

        int c = getColor.getAsInt();
        int rgb = c & 0xFFFFFF;

        // Color area (S/V) for current hue
        drawColorArea(ctx);

        // Hue bar
        drawHueBar(ctx);

        // Alpha bar
        drawAlphaBar(ctx, rgb);

        // Preview box
        ctx.fill(previewX, previewY, previewX + previewW, previewY + previewH, c);
        drawBorder(ctx, previewX, previewY, previewW, previewH, 0xFF000000);

        // Hex display (optional)
        String hex = String.format("#%08X", c);
        ctx.drawText(tr, hex, previewX, hexY, 0xFFCCCCCC, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        updateLayout();
        syncFromColor();

        if (isInside(mouseX, mouseY, areaX, areaY, areaSize, areaSize)) {
            dragging = DRAG_AREA;
            updateFromArea(mouseX, mouseY);
            return true;
        }

        if (isInside(mouseX, mouseY, hueX, hueY, hueW, hueH)) {
            dragging = DRAG_HUE;
            updateFromHue(mouseY);
            return true;
        }

        if (isInside(mouseX, mouseY, alphaX, alphaY, alphaW, alphaH)) {
            dragging = DRAG_ALPHA;
            updateFromAlpha(mouseY);
            return true;
        }

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging == DRAG_NONE) return false;
        if (dragging == DRAG_AREA) updateFromArea(mouseX, mouseY);
        else if (dragging == DRAG_HUE) updateFromHue(mouseY);
        else if (dragging == DRAG_ALPHA) updateFromAlpha(mouseY);
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging == DRAG_NONE) return false;
        dragging = DRAG_NONE;
        return true;
    }

    public boolean isDragging() {
        return dragging != DRAG_NONE;
    }

    private void updateFromArea(double mouseX, double mouseY) {
        float s = clamp01((float) ((mouseX - areaX) / (double) areaSize));
        float v = 1f - clamp01((float) ((mouseY - areaY) / (double) areaSize));
        sat = s;
        val = v;
        applyColor();
    }

    private void updateFromHue(double mouseY) {
        float t = clamp01((float) ((mouseY - hueY) / (double) hueH));
        hue = t;
        applyColor();
    }

    private void updateFromAlpha(double mouseY) {
        float t = clamp01((float) ((mouseY - alphaY) / (double) alphaH));
        alpha = 1f - t;
        applyColor();
    }

    private void applyColor() {
        int rgb = hsvToRgb(hue, sat, val);
        int a = clamp((int) (alpha * 255f + 0.5f));
        int out = (a << 24) | rgb;
        lastColor = out;
        setColor.accept(out);
    }

    private void drawColorArea(DrawContext ctx) {
        int size = areaSize;
        int steps = Math.min(GRADIENT_STEPS, size);
        int hueRgb = hsvToRgb(hue, 1f, 1f);
        for (int i = 0; i < steps; i++) {
            float s = (steps == 1) ? 0f : (i / (float) (steps - 1));
            int base = lerpColor(0xFFFFFF, hueRgb, s);
            int x0 = areaX + (i * size) / steps;
            int x1 = areaX + ((i + 1) * size) / steps;
            ctx.fill(x0, areaY, x1, areaY + size, 0xFF000000 | base);
        }

        for (int i = 0; i < steps; i++) {
            float t = (steps == 1) ? 0f : (i / (float) (steps - 1));
            int a = clamp((int) (t * 255f + 0.5f));
            int y0 = areaY + (i * size) / steps;
            int y1 = areaY + ((i + 1) * size) / steps;
            ctx.fill(areaX, y0, areaX + size, y1, (a << 24));
        }

        drawBorder(ctx, areaX, areaY, size, size, 0xFF000000);

        int max = Math.max(1, size - 1);
        int selX = areaX + Math.round(sat * max);
        int selY = areaY + Math.round((1f - val) * max);
        drawSelector(ctx, selX, selY, areaX, areaY, areaX + size - 1, areaY + size - 1);
    }

    private void drawHueBar(DrawContext ctx) {
        int steps = Math.min(GRADIENT_STEPS, hueH);
        for (int i = 0; i < steps; i++) {
            float h = (steps == 1) ? 0f : (i / (float) (steps - 1));
            int rgb = hsvToRgb(h, 1f, 1f);
            int y0 = hueY + (i * hueH) / steps;
            int y1 = hueY + ((i + 1) * hueH) / steps;
            ctx.fill(hueX, y0, hueX + hueW, y1, 0xFF000000 | rgb);
        }
        drawBorder(ctx, hueX, hueY, hueW, hueH, 0xFF000000);

        int max = Math.max(1, hueH - 1);
        int markerY = hueY + Math.round(hue * max);
        drawBarMarker(ctx, hueX, hueY, hueW, hueH, markerY);
    }

    private void drawAlphaBar(DrawContext ctx, int rgb) {
        int checker = 4;
        for (int cy = 0; cy < alphaH; cy += checker) {
            for (int cx = 0; cx < alphaW; cx += checker) {
                boolean dark = ((cx / checker) + (cy / checker)) % 2 == 0;
                int bg = dark ? 0xFF666666 : 0xFFAAAAAA;
                ctx.fill(alphaX + cx, alphaY + cy,
                        alphaX + Math.min(cx + checker, alphaW),
                        alphaY + Math.min(cy + checker, alphaH),
                        bg
                );
            }
        }

        int steps = Math.min(GRADIENT_STEPS, alphaH);
        for (int i = 0; i < steps; i++) {
            float t = (steps == 1) ? 1f : (1f - (i / (float) (steps - 1)));
            int a = clamp((int) (t * 255f + 0.5f));
            int row = (a << 24) | rgb;
            int y0 = alphaY + (i * alphaH) / steps;
            int y1 = alphaY + ((i + 1) * alphaH) / steps;
            ctx.fill(alphaX, y0, alphaX + alphaW, y1, row);
        }

        drawBorder(ctx, alphaX, alphaY, alphaW, alphaH, 0xFF000000);

        int max = Math.max(1, alphaH - 1);
        int markerY = alphaY + Math.round((1f - alpha) * max);
        drawBarMarker(ctx, alphaX, alphaY, alphaW, alphaH, markerY);
    }

    private void drawSelector(DrawContext ctx, int cx, int cy, int minX, int minY, int maxX, int maxY) {
        int half = 2;
        int x1 = clamp(cx - half, minX, maxX);
        int y1 = clamp(cy - half, minY, maxY);
        int x2 = clamp(cx + half, minX, maxX);
        int y2 = clamp(cy + half, minY, maxY);

        ctx.fill(x1, y1, x2 + 1, y2 + 1, 0xFFFFFFFF);

        if (x2 - x1 >= 2 && y2 - y1 >= 2) {
            ctx.fill(x1 + 1, y1 + 1, x2, y2, 0xFF000000);
        }
    }

    private void drawBarMarker(DrawContext ctx, int x, int y, int w, int h, int markerY) {
        int top = clamp(markerY - 1, y, y + h - 1);
        int bot = clamp(markerY + 1, y, y + h - 1);
        ctx.fill(x - 1, top, x + w + 1, bot + 1, 0xFFFFFFFF);
        ctx.fill(x, top + 1, x + w, bot, 0xFF000000);
    }

    private static boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void updateLayout() {
        int contentW = w - PAD * 2;
        int size = contentW - BAR_W * 2 - BAR_GAP * 2;
        if (size < 60) size = Math.max(40, contentW);

        areaX = x + PAD;
        areaY = y + AREA_TOP;
        areaSize = size;

        hueX = areaX + areaSize + BAR_GAP;
        hueY = areaY;
        hueW = BAR_W;
        hueH = areaSize;

        alphaX = hueX + hueW + BAR_GAP;
        alphaY = areaY;
        alphaW = BAR_W;
        alphaH = areaSize;

        previewX = areaX;
        previewY = areaY + areaSize + PREVIEW_GAP;
        previewW = contentW;
        previewH = PREVIEW_H;

        hexY = previewY + previewH + HEX_GAP;
    }

    private void syncFromColor() {
        int c = getColor.getAsInt();
        if (c == lastColor && dragging != DRAG_NONE) return;

        int a = (c >>> 24) & 0xFF;
        int r = (c >>> 16) & 0xFF;
        int g = (c >>> 8) & 0xFF;
        int b = c & 0xFF;

        float[] hsv = rgbToHsv(r, g, b);
        if (hsv[1] > 0.0001f) hue = hsv[0];
        sat = hsv[1];
        val = hsv[2];
        alpha = a / 255f;
        lastColor = c;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;

        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;

        int r = clamp((int) (ar + (br - ar) * t + 0.5f));
        int g = clamp((int) (ag + (bg - ag) * t + 0.5f));
        int bch = clamp((int) (ab + (bb - ab) * t + 0.5f));

        return (r << 16) | (g << 8) | bch;
    }

    // h in [0,1), s in [0,1], v in [0,1]
    private static float[] rgbToHsv(int r8, int g8, int b8) {
        float r = r8 / 255f, g = g8 / 255f, b = b8 / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h;
        if (delta == 0f) h = 0f;
        else if (max == r) h = ((g - b) / delta) % 6f;
        else if (max == g) h = ((b - r) / delta) + 2f;
        else h = ((r - g) / delta) + 4f;

        h /= 6f;
        if (h < 0f) h += 1f;

        float s = (max == 0f) ? 0f : (delta / max);
        float v = max;

        return new float[]{h, s, v};
    }

    // returns 0xRRGGBB
    private static int hsvToRgb(float h, float s, float v) {
        h = (h - (float) Math.floor(h)) * 6f;
        int i = (int) Math.floor(h);
        float f = h - i;

        float p = v * (1f - s);
        float q = v * (1f - s * f);
        float t = v * (1f - s * (1f - f));

        float rf, gf, bf;
        switch (i) {
            case 0 -> { rf = v; gf = t; bf = p; }
            case 1 -> { rf = q; gf = v; bf = p; }
            case 2 -> { rf = p; gf = v; bf = t; }
            case 3 -> { rf = p; gf = q; bf = v; }
            case 4 -> { rf = t; gf = p; bf = v; }
            default -> { rf = v; gf = p; bf = q; }
        }

        int r = clamp((int) (rf * 255f + 0.5f));
        int g = clamp((int) (gf * 255f + 0.5f));
        int b = clamp((int) (bf * 255f + 0.5f));

        return (r << 16) | (g << 8) | b;
    }

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.drawVerticalLine(x, y, y + h, color);
        ctx.drawVerticalLine(x + w, y, y + h, color);
        ctx.drawHorizontalLine(x, x + w, y, color);
        ctx.drawHorizontalLine(x, x + w, y + h, color);
    }
}
