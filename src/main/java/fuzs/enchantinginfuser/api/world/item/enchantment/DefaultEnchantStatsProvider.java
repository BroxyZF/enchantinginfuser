package fuzs.enchantinginfuser.api.world.item.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * implementation for {@link EnchantStatsProvider} for vanilla values
 */
public class DefaultEnchantStatsProvider implements EnchantStatsProvider {

    @Override
    public String getSourceNamespace() {
        return "minecraft";
    }

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public String[] getScalingNamespaces() {
        return new String[]{"minecraft"};
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, Level level, BlockPos pos) {
        return state.getEnchantPowerBonus(level, pos);
    }

    @Override
    public Enchantment.Rarity getRarity(Enchantment enchantment) {
        return enchantment.getRarity();
    }

    @Override
    public boolean isCompatibleWith(Enchantment enchantment, Enchantment other) {
        return enchantment.isCompatibleWith(other);
    }

    @Override
    public int getMinLevel(Enchantment enchantment) {
        return enchantment.getMinLevel();
    }

    @Override
    public int getMaxLevel(Enchantment enchantment) {
        return enchantment.getMaxLevel();
    }

    @Override
    public int getMinCost(Enchantment enchantment, int level) {
        return enchantment.getMinCost(level);
    }

    @Override
    public int getMaxCost(Enchantment enchantment, int level) {
        return enchantment.getMaxCost(level);
    }

    @Override
    public boolean isTreasureOnly(Enchantment enchantment) {
        return enchantment.isTreasureOnly();
    }

    @Override
    public boolean isCurse(Enchantment enchantment) {
        return enchantment.isCurse();
    }

    @Override
    public boolean isTradeable(Enchantment enchantment) {
        return enchantment.isTradeable();
    }

    @Override
    public boolean isDiscoverable(Enchantment enchantment) {
        return enchantment.isDiscoverable();
    }
}
