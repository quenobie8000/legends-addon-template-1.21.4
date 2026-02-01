package legends.ultra.cool.addons.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;

import java.util.Objects;
import java.util.function.Predicate;

public final class ContainerScan {
    public static boolean containerHasItem(Item target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return false;

        for (int i = 0; i < screen.getScreenHandler().slots.toArray().length - 36; i++) {
            DefaultedList<Slot> slots = screen.getScreenHandler().slots;
            Slot slot = slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.isOf(target)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containerHasName(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return false;

        for (int i = 0; i < screen.getScreenHandler().slots.toArray().length - 36; i++) {
            Slot slot = screen.getScreenHandler().slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && Objects.equals(stack.getName().toString().toLowerCase(), "literal{"+name.toLowerCase()+"}")) {
                return true;
            }
        }
        return false;
    }

    public static Pair<Integer, ItemStack> findFirst(Item target) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return null;

        int i = 0;
        for (Slot slot : screen.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.isOf(target)) {
                return new Pair<>(i, stack.copy());
            }
            i++;
        }
        return null;
    }

    public static boolean containerHasMatching(Predicate<ItemStack> matcher) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return false;

        for (Slot slot : screen.getScreenHandler().slots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && matcher.test(stack)) return true;
        }
        return false;
    }

}