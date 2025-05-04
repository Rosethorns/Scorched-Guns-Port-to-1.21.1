package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import top.ribs.scguns.block.CryoniterBlock;
import top.ribs.scguns.client.screen.CryoniterMenu;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CryoniterBlockEntity extends BlockEntity implements MenuProvider {
    private static final ResourceLocation CRYONITER_INGREDIENT_TAG = new ResourceLocation("scguns", "cryoniter_ingredient");
    private static final int FREEZE_INTERVAL = 40;
    private static final double FREEZE_RADIUS = 4.0;
    private static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::create);

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            updateLitState();
        }
    };

    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private int tickCounter = 0;
    private boolean isLit = false;

    public CryoniterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYONITER.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        updateLitState();

        if (isLit) {
            tickCounter++;
            if (tickCounter >= FREEZE_INTERVAL) {
                tickCounter = 0;
                freezeRandomWaterBlock();
            }
        } else {
            tickCounter = 0;
        }
    }

    private void freezeRandomWaterBlock() {
        RandomSource rand = RANDOM.get();

        double angle = rand.nextDouble() * 2 * Math.PI;
        double radius = Math.sqrt(rand.nextDouble()) * FREEZE_RADIUS;
        int dx = (int) Math.round(radius * Math.cos(angle));
        int dz = (int) Math.round(radius * Math.sin(angle));

        BlockPos targetPos = worldPosition.offset(dx, rand.nextInt(3) - 1, dz);

        if (level.getBlockState(targetPos).is(Blocks.WATER)) {
            level.setBlockAndUpdate(targetPos, Blocks.ICE.defaultBlockState());
            if (rand.nextFloat() < 0.75f) {
                itemHandler.extractItem(0, 1, false);
            }

            // Send packet to spawn particles on the client
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5,
                        10, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }

    private void updateLitState() {
        ItemStack stack = itemHandler.getStackInSlot(0);
        boolean shouldBeLit = !stack.isEmpty() && stack.is(ItemTags.create(CRYONITER_INGREDIENT_TAG));
        if (isLit != shouldBeLit) {
            isLit = shouldBeLit;
            level.setBlock(worldPosition, getBlockState().setValue(CryoniterBlock.LIT, isLit), Block.UPDATE_ALL);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.cryoniter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CryoniterMenu(id, playerInventory, this);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.ITEM_HANDLER) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putBoolean("isLit", isLit);
        tag.putInt("tickCounter", tickCounter);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        isLit = tag.getBoolean("isLit");
        tickCounter = tag.getInt("tickCounter");
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public boolean isLit() {
        return isLit;
    }

    public int getContainerSize() {
        return itemHandler.getSlots();
    }
}