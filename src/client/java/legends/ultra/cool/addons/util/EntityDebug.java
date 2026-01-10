package legends.ultra.cool.addons.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class EntityDebug {

    public static void dumpTargetFiltered(LivingEntity e) {
        MinecraftClient client = MinecraftClient.getInstance();
        String mobName;
        double maxHp = 0;

        if (client.player == null || client.world == null) return;

        if (!(e instanceof LivingEntity mob)) {
            client.player.sendMessage(Text.literal("[LegendsAddon] Target is not a LivingEntity"),false);
            return;
        }

        mobName =  mob.getDisplayName().getString();
        if (mobName.matches("\\d+")) maxHp = Double.parseDouble(mobName.replaceAll(".*?(\\d+).*\\/(\\d+).*", "$2"));


        ItemStack main = mob.getMainHandStack();
        double itemDef = readCustomInt(main, "def");
        double itemDmg = readCustomInt(main, "dmg");

        client.player.sendMessage(Text.literal("Mob: " + mobName),false);
        client.player.sendMessage(Text.literal("MaxHP: " + maxHp + "\ndef: " + itemDef + "\ndmg: " + itemDmg),false);
    }

    public double[] getMobStats(LivingEntity e) {

        ItemStack main = e.getMainHandStack();
        String mobName;

        double maxHp = 0;
        double currentHp = 0;
        double itemDef = readCustomInt(main, "def");
        double itemDmg = readCustomInt(main, "dmg");

        mobName =  e.getDisplayName().getString();
        if (mobName.matches("\\d+")) currentHp = Double.parseDouble(mobName.replaceAll(".*?(\\d+).*\\/(\\d+).*", "$1"));
        if (mobName.matches("\\d+")) maxHp = Double.parseDouble(mobName.replaceAll(".*?(\\d+).*\\/(\\d+).*", "$2"));

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
}


