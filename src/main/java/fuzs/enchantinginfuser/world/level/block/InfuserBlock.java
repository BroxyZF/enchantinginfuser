package fuzs.enchantinginfuser.world.level.block;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.network.message.S2CInfuserDataMessage;
import fuzs.enchantinginfuser.registry.ModRegistry;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class InfuserBlock extends EnchantmentTableBlock {
    private static final Component CHOOSE_TOOLTIP = new TranslatableComponent("block.enchantinginfuser.description.choose");
    private static final Component CHOOSE_AND_MODIFY_TOOLTIP = new TranslatableComponent("block.enchantinginfuser.description.chooseAndModify");
    private static final Component REPAIR_TOOLTIP = new TranslatableComponent("block.enchantinginfuser.description.repair");

    private final InfuserType type;

    public InfuserBlock(InfuserType type, Properties p_52953_) {
        super(p_52953_);
        this.type = type;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new InfuserBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, ModRegistry.INFUSER_BLOCK_ENTITY_TYPE.get(), EnchantmentTableBlockEntity::bookAnimationTick) : null;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof InfuserBlockEntity blockEntity) {
            if (pLevel.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
            if (pPlayer.containerMenu instanceof InfuserMenu menu) {
                // items might still be in inventory slots, so this needs to update so that enchantment buttons are shown
                menu.slotsChanged(blockEntity);
                final int power = menu.setEnchantingPower(pLevel, pPos);
                final int repairCost = menu.setRepairCost();
                EnchantingInfuser.NETWORK.sendTo(new S2CInfuserDataMessage(pPlayer.containerMenu.containerId, power, repairCost), (ServerPlayer) pPlayer);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        if (pLevel.getBlockEntity(pPos) instanceof InfuserBlockEntity blockentity) {
            Component component = blockentity.getDisplayName();
            return new SimpleMenuProvider((p_52959_, p_52960_, p_52961_) -> {
                if (blockentity.canOpen(p_52961_)) {
                    return InfuserMenu.create(this.type, p_52959_, p_52960_, blockentity, ContainerLevelAccess.create(pLevel, pPos));
                }
                return null;
            }, component);
        } else {
            return null;
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        super.animateTick(state, level, pos, random);

        for(BlockPos blockpos : BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) == 0 && InfuserMenu.isValidBookShelf(level, pos, blockpos)) {
                level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D, ((float)blockpos.getX() + random.nextFloat()) - 0.5D, ((float)blockpos.getY() - random.nextFloat() - 1.0F), ((float)blockpos.getZ() + random.nextFloat()) - 0.5D);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (worldIn.getBlockEntity(pos) instanceof InfuserBlockEntity blockEntity) {
                Containers.dropContents(worldIn, pos, blockEntity);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
        // don't let this go through when initially gathering tooltip data during start-up, configs do not exist then and it's ok if this is not searchable
        if (!EnchantingInfuser.CONFIG.isServerAvailable()) return;
        Component component;
        if (this.type.config().allowModifyingEnchantments == ServerConfig.ModifyableItems.UNENCHANTED) {
            component = CHOOSE_TOOLTIP;
        } else {
            component = CHOOSE_AND_MODIFY_TOOLTIP;
        }
        MutableComponent mutableComponent = new TextComponent("").append(component).withStyle(ChatFormatting.GRAY);
        if (this.type.config().allowRepairing) {
            mutableComponent = mutableComponent.append(" ").append(REPAIR_TOOLTIP);
        }
        list.add(mutableComponent);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof InfuserBlockEntity blockEntity) {
            if (!blockEntity.getItem(0).isEmpty()) {
                return 15;
            }
        }
        return 0;
    }

    public enum InfuserType {
        NORMAL, ADVANCED;

        public MenuType<?> menuType() {
            return switch (this) {
                case NORMAL -> ModRegistry.INFUSING_MENU_TYPE.get();
                case ADVANCED -> ModRegistry.ADVANCED_INFUSING_MENU_TYPE.get();
            };
        }

        public ServerConfig.InfuserConfig config() {
            return switch (this) {
                case NORMAL -> EnchantingInfuser.CONFIG.server().normalInfuser;
                case ADVANCED -> EnchantingInfuser.CONFIG.server().advancedInfuser;
            };
        }
    }
}
