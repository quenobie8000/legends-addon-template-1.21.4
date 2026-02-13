package legends.ultra.cool.addons.rei;

import legends.ultra.cool.addons.LegendsAddon;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class LegendsWeaponCategory implements DisplayCategory<BasicDisplay> {
    public static final CategoryIdentifier<LegendsWeaponDisplay> LEGENDS_WEAPON =
            CategoryIdentifier.of(LegendsAddon.MOD_ID, "weapons");

    @Override
    public CategoryIdentifier<? extends BasicDisplay> getCategoryIdentifier() {
        return null;
    }

    @Override
    public Text getTitle() {
        return Text.literal("Weapons");
    }

    @Override
    public Renderer getIcon() {
        return null;
    }
}
