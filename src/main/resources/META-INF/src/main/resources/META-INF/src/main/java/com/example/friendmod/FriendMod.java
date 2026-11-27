package com.example.friendmod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod(FriendMod.MODID)
public class FriendMod {
    public static final String MODID = "friendmod";

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<KrestikEntity>> KRESTIK = ENTITIES.register("krestik",
            () -> EntityType.Builder.of(KrestikEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build(new ResourceLocation(MODID, "krestik").toString()));

    private boolean day4Triggered = false;

    public FriendMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(modEventBus);
        modEventBus.addListener(this::registerAttributes);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(KRESTIK.get(), KrestikEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;
        if (!(event.level instanceof ServerLevel level)) return;

        long time = level.getDayTime();

        // День 1: спавним сталкера вдалеке от игрока
        if (time == 1200L && !level.players().isEmpty()) {
            ServerPlayer player = level.players().get(0);
            List<KrestikEntity> existing = level.getEntitiesOfClass(KrestikEntity.class, player.getBoundingBox().inflate(200));
            if (existing.isEmpty()) {
                BlockPos pos = player.blockPosition().offset(28, 0, 28);
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
                KrestikEntity krestik = KRESTIK.get().create(level);
                if (krestik != null) {
                    krestik.moveTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
                    level.addFreshEntity(krestik);
                }
            }
        }

        // День 4 (72000 тиков = начало 4-го дня): звук и подсказка позвать его в чат
        if (time >= 72000L && !day4Triggered && !level.players().isEmpty()) {
            day4Triggered = true;
            for (ServerPlayer player : level.players()) {
                level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 1.0F, 0.8F);
                player.sendSystemMessage(Component.literal("§7[?] Вы чувствуете, что кто-то следит за вами уже 4 дня... Позовите его в чате (напишите: §fкуку§7)"));
            }
        }
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        String msg = event.getMessage().getString().toLowerCase().trim();
        if (msg.contains("куку") || msg.contains("привет") || msg.contains("крестик")) {
            ServerPlayer player = event.getPlayer();
            ServerLevel level = player.serverLevel();
            AABB box = player.getBoundingBox().inflate(150.0D);
            List<KrestikEntity> krestiks = level.getEntitiesOfClass(KrestikEntity.class, box);

            if (!krestiks.isEmpty()) {
                for (KrestikEntity krestik : krestiks) {
                    krestik.teleportTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0);
                    krestik.befriendByChat(player);
                }
            } else {
                KrestikEntity krestik = KRESTIK.get().create(level);
                if (krestik != null) {
                    krestik.moveTo(player.getX() + 2.0, player.getY(), player.getZ() + 2.0, 0, 0);
                    level.addFreshEntity(krestik);
                    krestik.befriendByChat(player);
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(KRESTIK.get(), KrestikRenderer::new);
        }
    }
}
