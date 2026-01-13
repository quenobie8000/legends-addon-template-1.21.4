package legends.ultra.cool.addons.util;

public class TextHeathbar {

    public static String heathBar(double current, double max) {
        int percentH = 0;
        if (max > 0) percentH = (int)((current / max)*100);
        if (current > max) percentH = 100;

        String HB = "⬛⬛⬛⬛⬛" + percentH + "%⬛⬛⬛⬛⬛";
        StringBuilder health = new StringBuilder(HB);

        health.insert((int)(HB.length() * ((double)percentH/100)),"§7");

        HB = health.toString();

        return HB;
    }
}
