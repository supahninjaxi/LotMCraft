package de.jakob.lotm.entity.client.projectiles.spirit_ball;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.projectiles.fireball.FireballModel;
import de.jakob.lotm.entity.custom.projectiles.FireballEntity;
import de.jakob.lotm.entity.custom.projectiles.SpiritBallEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector3f;

import java.awt.*;

public class SpiritBallRenderer extends EntityRenderer<SpiritBallEntity> {

    public SpiritBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SpiritBallEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        Random random = new Random(entity.getUUID().getMostSignificantBits());
        float scale = .125f;
        float hue        = random.nextFloat();
        float saturation = 0.7f + random.nextFloat() * 0.3f;
        float brightness = 0.8f + random.nextFloat() * 0.2f;

        int rgb   = Color.HSBtoRGB(hue, saturation, brightness);
        float red   = ((rgb >> 16) & 0xFF) / 255f;
        float green = ((rgb >> 8)  & 0xFF) / 255f;
        float blue  = ( rgb        & 0xFF) / 255f;

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));

        renderSphere(matrix, vc, red, green, blue);
        poseStack.popPose();
        poseStack.popPose();
    }

    protected int getBlockLightLevel(SpiritBallEntity projectileEntity, BlockPos blockpos) {
        return 15;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SpiritBallEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/spirit_ball/spirit_ball.png");
    }

    private void renderSphere(Matrix4f matrix, VertexConsumer buffer, float red, float green, float blue) {
        int rings = 24;
        int segments = 48;

        for (int i = 0; i < rings; i++) {
            float theta1 = (float) (Math.PI * i / rings);
            float theta2 = (float) (Math.PI * (i + 1) / rings);

            for (int j = 0; j < segments; j++) {
                float phi1 = (float) (2 * Math.PI * j / segments);
                float phi2 = (float) (2 * Math.PI * (j + 1) / segments);

                Vector3f v1 = spherical(theta1, phi1);
                Vector3f v2 = spherical(theta2, phi1);
                Vector3f v3 = spherical(theta2, phi2);
                Vector3f v4 = spherical(theta1, phi2);

                putQuad(buffer, matrix, v1, v2, v3, v4, red, green, blue);
            }
        }
    }

    private Vector3f spherical(float theta, float phi) {
        float x = (float) (Math.sin(theta) * Math.cos(phi));
        float y = (float) Math.cos(theta);
        float z = (float) (Math.sin(theta) * Math.sin(phi));
        return new Vector3f(x, y, z);
    }

    private void putQuad(VertexConsumer buffer, Matrix4f matrix, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, float red, float green, float blue) {
        int r = (int)(red   * 255);
        int g = (int)(green * 255);
        int b = (int)(blue  * 255);

        // Add 4 vertices — each must have color, UV, light, overlay, and normal
        buffer.addVertex(matrix, v1.x, v1.y, v1.z)
                .setColor(r, g, b, 255)
                .setUv(0f, 0f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);

        buffer.addVertex(matrix, v2.x, v2.y, v2.z)
                .setColor(r, g, b, 255)
                .setUv(0f, 1f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);

        buffer.addVertex(matrix, v3.x, v3.y, v3.z)
                .setColor(r, g, b, 255)
                .setUv(1f, 1f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);

        buffer.addVertex(matrix, v4.x, v4.y, v4.z)
                .setColor(r, g, b, 255)
                .setUv(1f, 0f)
                .setLight(240)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);
    }
}