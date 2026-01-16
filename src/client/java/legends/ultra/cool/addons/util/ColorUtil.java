package legends.ultra.cool.addons.util;

import net.minecraft.util.math.MathHelper;

public final class ColorUtil {
    private ColorUtil() {}

    // Takes ARGB, returns ARGB with same H/S but V forced to 1.0
    public static int forceValueToMax(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>>  8) & 0xFF;
        int b = (argb       ) & 0xFF;

        float[] hsv = rgbToHsv(r, g, b);
        hsv[2] = 1.0f; // V = 100%

        int rgb = hsvToRgb(hsv[0], hsv[1], hsv[2]); // returns 0xRRGGBB
        return (a << 24) | rgb;
    }

    // ---- minimal rgb<->hsv helpers ----

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
        h = (h - (float)Math.floor(h)) * 6f;
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

        int r = MathHelper.clamp((int)(rf * 255f + 0.5f), 0, 255);
        int g = MathHelper.clamp((int)(gf * 255f + 0.5f), 0, 255);
        int b = MathHelper.clamp((int)(bf * 255f + 0.5f), 0, 255);

        return (r << 16) | (g << 8) | b;
    }
}