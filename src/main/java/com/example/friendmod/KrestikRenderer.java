package com.example.friendmod;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class KrestikRenderer extends MobRenderer<KrestikEntity, PlayerModel<KrestikEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(FriendMod.MODID, "textures/entity/krestik.png");

    public KrestikRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(KrestikEntity entity) {
        return TEXTURE;
    }

    @Mod.EventBusSubscriber(modid = FriendMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(FriendMod.KRESTIK.get(), KrestikRenderer::new);
        }
    }
}
