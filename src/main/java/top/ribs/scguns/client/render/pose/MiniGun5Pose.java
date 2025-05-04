package top.ribs.scguns.client.render.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.handler.ReloadHandler;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.item.animated.AnimatedGunItem;

/**
 * Author: MrCrayfish
 */
public class MiniGun5Pose extends WeaponPose
{
    @Override
    protected AimPose getUpPose()
    {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(35F).setItemRotation(new Vector3f(10F, 0F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-170F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointY(4).setRotationPointZ(-2)).setLeftArm(new LimbPose().setRotationAngleX(-130F).setRotationAngleY(65F).setRotationAngleZ(0F).setRotationPointX(3).setRotationPointY(2).setRotationPointZ(1));
        return pose;
    }

    @Override
    protected AimPose getForwardPose()
    {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(35F).setRightArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointY(2).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-91F).setRotationAngleY(35F).setRotationAngleZ(0F).setRotationPointX(4).setRotationPointY(2).setRotationPointZ(0));
        return pose;
    }

    @Override
    protected AimPose getDownPose()
    {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(35F).setRightArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointY(2).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(15F).setRotationAngleZ(30F).setRotationPointX(4).setRotationPointY(2).setRotationPointZ(0));
        return pose;
    }
    @Override
    protected AimPose getMeleePose() {
        AimPose meleePose = new AimPose();
        meleePose.getIdle().setRenderYawOffset(0F).setItemRotation(new Vector3f(0F, 0F, 0F))
                .setRightArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0))
                .setLeftArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0));
        meleePose.getAiming().setRenderYawOffset(0F).setItemRotation(new Vector3f(0F, 0F, 0F))
                .setRightArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0))
                .setLeftArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0));
        return meleePose;
    }

    @Override
    protected AimPose getBanzaiPose() {
        AimPose banzaiPose = new AimPose();
        banzaiPose.getIdle().setRenderYawOffset(0F).setItemRotation(new Vector3f(0F, 0F, 0F))
                .setRightArm(new LimbPose().setRotationAngleX(-70F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0))
                .setLeftArm(new LimbPose().setRotationAngleX(-70F).setRotationAngleY(0F).setRotationPointX(0).setRotationPointY(0).setRotationPointZ(0));
        return banzaiPose;
    }

    @Override
    protected boolean hasAimPose()
    {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerModelRotation(Player player, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress)
    {
        if(Config.CLIENT.display.oldAnimations.get())
        {
            boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            ModelPart mainArm = right ? rightArm : leftArm;
            ModelPart secondaryArm = right ? leftArm : rightArm;
            mainArm.xRot = (float) Math.toRadians(-90F);
            mainArm.yRot = (float) Math.toRadians(-35F) * (right ? 1F : -1F);
            mainArm.zRot = (float) Math.toRadians(0F);
            secondaryArm.xRot = (float) Math.toRadians(-91F);
            secondaryArm.yRot = (float) Math.toRadians(45F) * (right ? 1F : -1F);
            secondaryArm.zRot = (float) Math.toRadians(0F);
        }
        else
        {
            super.applyPlayerModelRotation(player, rightArm, leftArm, head, hand, aimProgress);
        }

        if (GunRenderingHandler.get().isThirdPersonMeleeAttacking()) {
            float banzaiProgress = GunRenderingHandler.get().getThirdPersonMeleeProgress();
            applyBanzaiPose(rightArm, leftArm, banzaiProgress);
        }
    }

    private void applyBanzaiPose(ModelPart rightArm, ModelPart leftArm, float banzaiProgress) {
        rightArm.xRot = Mth.lerp(banzaiProgress, rightArm.xRot, (float) Math.toRadians(-90F));
        leftArm.xRot = Mth.lerp(banzaiProgress, leftArm.xRot, (float) Math.toRadians(-90F));
    }

    @Override
    public void applyPlayerPreRender(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        if(Config.CLIENT.display.oldAnimations.get())
        {
            boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            player.yBodyRotO = player.yRotO + 35F * (right ? 1F : -1F);
            player.yBodyRot = player.getYRot() + 35F * (right ? 1F : -1F);
        }
        else
        {
            super.applyPlayerPreRender(player, hand, aimProgress, poseStack, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        if(!Config.CLIENT.display.oldAnimations.get())
        {
            super.applyHeldItemTransforms(player, hand, aimProgress, poseStack, buffer);
        }
    }

    @Override
    public boolean applyOffhandTransforms(Player player, PlayerModel model, ItemStack stack, PoseStack poseStack, float partialTicks)
    {
        return GripType.applyBackTransforms(player, poseStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderFirstPersonArms(Player player, HumanoidArm hand, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, float partialTicks) {
        if (stack.getItem() instanceof AnimatedGunItem) {
            return;
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(180F));

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, player.level(), player, 0);
        float translateX = model.getTransforms().firstPersonRightHand.translation.x();
        int side = hand.getOpposite() == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(translateX * side, 0, 0);

        boolean slim = Minecraft.getInstance().player.getModelName().equals("slim");
        float armWidth = slim ? 3.0F : 4.0F;

        // Front arm holding the barrel
        poseStack.pushPose();
        {
            float reloadProgress = ReloadHandler.get().getReloadProgress(partialTicks);
            poseStack.translate(reloadProgress * 0.5, -reloadProgress, -reloadProgress * 0.5);

            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.translate(4.0 * 0.0625 * side, 0.3, 0);
            poseStack.translate((armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.translate(-0.55 * side, -0.0, -0.7);

            poseStack.mulPose(Axis.XP.rotationDegrees(110F));
            poseStack.mulPose(Axis.YP.rotationDegrees(12f * -side));
            poseStack.mulPose(Axis.ZP.rotationDegrees(12f * -side));
            poseStack.mulPose(Axis.XP.rotationDegrees(-35F));
            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand.getOpposite(), poseStack, buffer, light);
        }
        poseStack.popPose();
        // Back arm holding the handle
        poseStack.pushPose();
        {
            poseStack.translate(0, 0.1, -0.675);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(-4.0 * 0.0625 * side, 0, 0);
            poseStack.translate(-(armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(80F));

            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand, poseStack, buffer, light);
        }
        poseStack.popPose();
    }

    @Override
    public boolean canApplySprintingAnimation()
    {
        return false;
    }
}
