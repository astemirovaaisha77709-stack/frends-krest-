package com.example.friendmod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KrestikEntity extends TamableAnimal {

    private int chatCooldown = 0;

    private static final List<String> FRIEND_PHRASES = List.of(
            "<Крестик> Куда дальше пойдём?",
            "<Крестик> Я прикрою спину, не бойся!",
            "<Крестик> Надо бы нам добыть побольше ресурсов...",
            "<Крестик> Мне нравится проходить игру с тобой!",
            "<Крестик> Слышал шорох? Будь начеку!"
    );

    public KrestikEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 10.0F, 1.1D, 1.3D,
                player -> !this.isTame()));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.2D, 5.0F, 2.0F, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    public void befriendByChat(Player player) {
        if (!this.isTame() && !this.level().isClientSide) {
            this.tame(player);
            this.setOrderedToSit(false);
            this.level().broadcastEntityEvent(this, (byte) 7);
            this.level().playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.0F);
            player.sendSystemMessage(Component.literal("<Крестик> Привет! Я 4 дня наблюдал за тобой... Теперь мы будем проходить игру вместе!"));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.isTame() && this.getOwner() instanceof Player owner) {
            if (chatCooldown > 0) {
                chatCooldown--;
            } else if (this.distanceTo(owner) < 6.0F && this.random.nextInt(1200) == 0) {
                String phrase = FRIEND_PHRASES.get(this.random.nextInt(FRIEND_PHRASES.size()));
                owner.sendSystemMessage(Component.literal(phrase));
                chatCooldown = 2400;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        if (this.isTame() && this.isOwnedBy(player)) {
            boolean willSit = !this.isOrderedToSit();
            this.setOrderedToSit(willSit);
            this.jumping = false;
            this.navigation.stop();
            if (willSit) {
                player.sendSystemMessage(Component.literal("<Крестик> Хорошо, подожду тебя здесь!"));
            } else {
                player.sendSystemMessage(Component.literal("<Крестик> Снова в путь!"));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && this.isTame() && this.getOwner() instanceof Player owner && chatCooldown <= 0) {
            owner.sendSystemMessage(Component.literal("<Крестик> Ай! Меня атакуют, помоги!"));
            chatCooldown = 400;
        }
        return super.hurt(source, amount);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }
}
