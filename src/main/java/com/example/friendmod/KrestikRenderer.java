package com.example.friendmod;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class KrestikRenderer extends HumanoidMobRenderer<KrestikEntity, PlayerModel<KrestikEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FriendMod.MODID, "textures/entity/krestik.png");

    public KrestikRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(KrestikEntity entity) {
        return TEXTURE;
    }
}
