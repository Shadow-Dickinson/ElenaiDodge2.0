package com.elenai.elenaidodge2.event;

import com.elenai.elenaidodge2.api.DodgeEvent.Direction;
import com.elenai.elenaidodge2.util.ClientStorage;
import com.elenai.elenaidodge2.util.DodgeAnimation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class DodgeAnimationEventListener {
	private static final float[] ROLL_KEYS = { 0.0F, 0.104F, 0.208F, 0.375F, 0.542F, 0.667F, 0.792F, 0.896F,
			1.0F };
	private static final float[] FORWARD_PITCH = { 0.0F, -28.0F, -70.0F, -140.0F, -220.0F, -300.0F, -348.0F,
			-362.0F, -360.0F };
	private static final float[] BACK_PITCH = { 0.0F, 26.0F, 68.0F, 138.0F, 218.0F, 300.0F, 348.0F, 362.0F,
			360.0F };
	private static final float[] FORWARD_Z = { 0.0F, 0.04F, 0.13F, 0.28F, 0.42F, 0.52F, 0.42F, 0.18F, 0.0F };
	private static final float[] BACK_Z = { 0.0F, -0.04F, -0.12F, -0.26F, -0.39F, -0.48F, -0.38F, -0.16F, 0.0F };
	private static final float[] FORWARD_DROP = { 0.0F, 0.07F, 0.2F, 0.34F, 0.42F, 0.36F, 0.22F, 0.08F, 0.0F };
	private static final float[] BACK_DROP = { 0.0F, 0.05F, 0.15F, 0.26F, 0.32F, 0.28F, 0.16F, 0.06F, 0.0F };

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.side == Side.CLIENT && event.phase == TickEvent.Phase.END) {
			DodgeAnimation.tick();
		}
	}

	@SubscribeEvent
	public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
		EntityPlayer player = event.getEntityPlayer();
		DodgeAnimation animation = DodgeAnimation.get(player.getEntityId());
		if (animation == null) {
			return;
		}

		float progress = animation.getProgress(event.getPartialRenderTick());
		float pulse = (float) Math.sin(progress * Math.PI);
		float intensity = Math.max(0.0F, ClientStorage.dodgeAnimationIntensity);
		double drop = pulse * 0.2D;
		double pivot = 0.9D;
		float yaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * event.getPartialRenderTick();
		double x = 0.0D;
		double z = 0.0D;
		float pitch = 0.0F;
		float roll = 0.0F;
		float visualYaw = 0.0F;

		if (animation.getDirection() == Direction.FORWARD) {
			pitch = sample(progress, FORWARD_PITCH) * intensity;
			z = sample(progress, FORWARD_Z) * intensity;
			drop = sample(progress, FORWARD_DROP) * intensity;
			pivot = 0.64D;
		} else if (animation.getDirection() == Direction.BACK) {
			pitch = sample(progress, BACK_PITCH) * intensity;
			z = sample(progress, BACK_Z) * intensity;
			drop = sample(progress, BACK_DROP) * intensity;
			pivot = 0.7D;
		} else if (animation.getDirection() == Direction.LEFT) {
			pitch = sample(progress, FORWARD_PITCH) * intensity;
			visualYaw = 45.0F * getTurnProgress(progress);
			x = pulse * 0.36D * intensity;
			drop = sample(progress, FORWARD_DROP) * 0.75D * intensity;
			pivot = 0.68D;
		} else {
			pitch = sample(progress, FORWARD_PITCH) * intensity;
			visualYaw = -45.0F * getTurnProgress(progress);
			x = -pulse * 0.36D * intensity;
			drop = sample(progress, FORWARD_DROP) * 0.75D * intensity;
			pivot = 0.68D;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(event.getX(), event.getY(), event.getZ());
		GlStateManager.rotate(180.0F - yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(x, -drop, z);
		GlStateManager.translate(0.0D, pivot, 0.0D);
		GlStateManager.rotate(visualYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(roll, 0.0F, 0.0F, 1.0F);
		GlStateManager.translate(0.0D, -pivot, 0.0D);
		GlStateManager.rotate(yaw - 180.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-event.getX(), -event.getY(), -event.getZ());
	}

	@SubscribeEvent
	public void onRenderSpecificHand(RenderSpecificHandEvent event) {
		if (!ClientStorage.firstPersonDodgeAnimation) {
			return;
		}

		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null) {
			return;
		}

		DodgeAnimation animation = DodgeAnimation.get(player.getEntityId());
		if (animation == null) {
			return;
		}

		float progress = animation.getProgress(event.getPartialTicks());
		float pulse = (float) Math.sin(progress * Math.PI);
		float intensity = Math.max(0.0F, ClientStorage.dodgeAnimationIntensity);
		float side = event.getHand() == EnumHand.OFF_HAND ? -1.0F : 1.0F;
		float x = 0.0F;
		float y = -pulse * 0.08F * intensity;
		float z = -pulse * 0.12F * intensity;
		float roll = side * pulse * 8.0F * intensity;
		float pitch = pulse * 5.0F * intensity;

		if (animation.getDirection() == Direction.FORWARD) {
			z -= pulse * 0.08F * intensity;
			pitch += pulse * 6.0F * intensity;
		} else if (animation.getDirection() == Direction.BACK) {
			z += pulse * 0.08F * intensity;
			pitch -= pulse * 5.0F * intensity;
		} else if (animation.getDirection() == Direction.LEFT) {
			x += pulse * 0.12F * intensity;
			roll += pulse * 8.0F * intensity;
		} else {
			x -= pulse * 0.12F * intensity;
			roll -= pulse * 8.0F * intensity;
		}

		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(roll, 0.0F, 0.0F, 1.0F);
	}

	@SubscribeEvent
	public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
		if (!ClientStorage.firstPersonCameraDodgeAnimation || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) {
			return;
		}

		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null || event.getEntity() != player) {
			return;
		}

		DodgeAnimation animation = DodgeAnimation.get(player.getEntityId());
		if (animation == null) {
			return;
		}

		float progress = animation.getProgress((float) event.getRenderPartialTicks());
		float pulse = (float) Math.sin(progress * Math.PI);
		float intensity = Math.max(0.0F, ClientStorage.firstPersonCameraIntensity);
		float pitch = 0.0F;
		float roll = 0.0F;
		float spin = getCameraSpin(progress, intensity);

		if (animation.getDirection() == Direction.FORWARD) {
			pitch = spin;
			roll = 0.0F;
		} else if (animation.getDirection() == Direction.BACK) {
			pitch = -spin;
			roll = 0.0F;
		} else if (animation.getDirection() == Direction.LEFT) {
			pitch = pulse * 4.0F * intensity;
			roll = -spin;
		} else {
			pitch = pulse * 4.0F * intensity;
			roll = spin;
		}

		event.setPitch(event.getPitch() + pitch);
		event.setRoll(event.getRoll() + roll);
	}

	private float getCameraSpin(float progress, float intensity) {
		float turns = Math.max(1.0F, (float) Math.ceil(intensity));
		float shape = 1.0F + Math.max(0.0F, intensity - 1.0F) * 0.35F;
		float value = smooth(progress);
		if (shape != 1.0F) {
			value = (float) Math.pow(value, 1.0F / shape);
		}
		return value * 360.0F * turns;
	}

	private float getTurnProgress(float progress) {
		if (progress < 0.18F) {
			return smooth(progress / 0.18F);
		}
		if (progress > 0.82F) {
			return 1.0F - smooth((progress - 0.82F) / 0.18F);
		}
		return 1.0F;
	}

	private float sample(float progress, float[] values) {
		if (progress <= 0.0F) {
			return values[0];
		}
		for (int index = 1; index < ROLL_KEYS.length; index++) {
			if (progress <= ROLL_KEYS[index]) {
				float start = ROLL_KEYS[index - 1];
				float end = ROLL_KEYS[index];
				float value = smooth((progress - start) / (end - start));
				return values[index - 1] + (values[index] - values[index - 1]) * value;
			}
		}
		return values[values.length - 1];
	}

	private float smooth(float value) {
		return value * value * (3.0F - 2.0F * value);
	}

	@SubscribeEvent
	public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
		if (DodgeAnimation.get(event.getEntityPlayer().getEntityId()) != null) {
			GlStateManager.popMatrix();
		}
	}
}
