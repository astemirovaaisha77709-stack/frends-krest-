package com.example.friendmod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(FriendMod.MODID)
public class FriendMod {
    public static final String MODID = "friendmod";

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<KrestikEntity>> KRESTIK = ENTITIES.register("krestik",
            () -> EntityType.Builder.of(KrestikEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build("krestik"));

    private static final Map<UUID, Boolean> soundPlayedMap = new HashMap<>();

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
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        ServerLevel level = player.serverLevel();
        long dayTime = level.getDayTime();

        // 4 игровых дня = 96000 тиков (4 * 24000)
        if (dayTime >= 96000L && !soundPlayedMap.getOrDefault(player.getUUID(), false)) {
            soundPlayedMap.put(player.getUUID(), true);
            level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 1.0F, 0.8F);
            player.sendSystemMessage(Component.literal("§7[?] Вы чувствуете, что за вами кто-то следит из тени... Позовите его в чате (напишите: куку)"));
        }
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String msg = event.getRawText().toLowerCase().trim();

        if (msg.contains("куку") || msg.contains("привет") || msg.contains("крестик")) {
            ServerLevel level = player.serverLevel();
            // Проверяем, есть ли уже Крестик рядом
            var nearby = level.getEntitiesOfClass(KrestikEntity.class, player.getBoundingBox().inflate(50.0D));
            if (nearby.isEmpty()) {
                BlockPos spawnPos = player.blockPosition().offset(player.getRandom().nextInt(6) - 3, 0, player.getRandom().nextInt(6) - 3);
                KrestikEntity krestik = KRESTIK.get().spawn(level, spawnPos, MobSpawnType.TRIGGERED);
                if (krestik != null) {
                    krestik.befriendByChat(player);
                }
            } else {
                for (KrestikEntity k : nearby) {
                    if (!k.isTame()) {
                        k.befriendByChat(player);
                    }
                }
            }
        }
    }
}
