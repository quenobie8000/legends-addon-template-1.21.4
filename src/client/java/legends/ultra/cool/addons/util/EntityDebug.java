package legends.ultra.cool.addons.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public class EntityDebug {

    public static void dumpTargetFiltered(LivingEntity e) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!(e instanceof LivingEntity mob)) {
            client.player.sendMessage(Text.literal("[LegendsAddon] Target is not a LivingEntity"),false);
            return;
        }

        int argb = 0;
        for (ItemStack armor : e.getArmorItems()) {
            if (armor.isEmpty()) continue;

            argb = getArmorColorARGB(armor);
            // use argb
        }

        String mobName =  mob.getDisplayName().getString();
        double[] mobStats = getMobStats(mob);
        double maxHp = mobStats[1];
        double itemDef = mobStats[2];
        double itemDmg = mobStats[3];

        client.player.sendMessage(Text.literal("\nMob: " + mobName + "\nMaxHP: " + maxHp + "\ndef: " + itemDef + "\ndmg: " + itemDmg + "\n armor color: " + toHexARGB(argb)),false);
    }

    public static double[] getMobStats(LivingEntity e) {

        ItemStack main = e.getMainHandStack();
        String mobName;

        double maxHp = 0;
        double currentHp = 0;
        double itemDef = readCustomInt(main, "def");
        double itemDmg = readCustomInt(main, "dmg");

        mobName =  e.getDisplayName().getString();
        if (mobName.matches(".*?(\\d+).*\\/(\\d+).*")) currentHp = Double.parseDouble(mobName.replaceAll(".*?(\\d+(?:[.,]\\d+)?).*?\\/.*?(\\d+(?:[.,]\\d+)?).*", "$1"));
        if (mobName.matches(".*?(\\d+).*\\/(\\d+).*")) maxHp = Double.parseDouble(mobName.replaceAll(".*?(\\d+(?:[.,]\\d+)?).*?\\/.*?(\\d+(?:[.,]\\d+)?).*", "$2"));

        return new double[]{currentHp, maxHp, itemDef, itemDmg};
    }

    private static int readCustomInt(ItemStack stack, String key) {
        if (stack.isEmpty()) return 0;

        // CUSTOM_DATA is stored as an NbtComponent in 1.20.5+ / 1.21+ item components :contentReference[oaicite:1]{index=1}
        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null || custom.isEmpty()) return 0;

        NbtCompound nbt = custom.copyNbt(); // copies the NBT payload :contentReference[oaicite:2]{index=2}
        return nbt.contains(key) ? nbt.getInt(key) : 0;
    }

    public static int getArmorColorARGB(ItemStack stack) {
        DyedColorComponent dyed = stack.get(DataComponentTypes.DYED_COLOR);

        if (dyed == null) {
            return 0xFFFFFFFF; // no dye → white
        }

        int rgb = dyed.rgb(); // 0xRRGGBB
        return 0xFF000000 | rgb; // ARGB
    }

    public static String toHexARGB(int argb) {
        return String.format("%08X", argb);
    }
}


