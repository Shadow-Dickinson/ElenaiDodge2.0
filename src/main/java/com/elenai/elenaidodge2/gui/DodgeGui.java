package com.elenai.elenaidodge2.gui;

import org.lwjgl.opengl.GL11;

import com.elenai.elenaidodge2.ElenaiDodge2;
import com.elenai.elenaidodge2.ModConfig;
import com.elenai.elenaidodge2.util.ClientStorage;
import com.elenai.elenaidodge2.util.PatronRewardHandler;
import com.elenai.elenaidodge2.util.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DodgeGui {

	private static final int UNITS_PER_LAYER = 20;
	private static final int FEATHER_SLOTS = 10;
	private static final float[][] FEATHER_LAYER_COLORS = {
			{ 1.0F, 0.0F, 0.0F },
			{ 1.0F, 0.38F, 0.0F },
			{ 1.0F, 0.95F, 0.0F },
			{ 0.0F, 1.0F, 0.0F },
			{ 1.0F, 1.0F, 1.0F } };
	private static final float[][] ABSORPTION_LAYER_COLORS = {
			{ 1.0F, 0.0F, 0.0F },
			{ 1.0F, 0.38F, 0.0F },
			{ 1.0F, 0.95F, 0.0F },
			{ 0.0F, 1.0F, 0.0F },
			{ 1.0F, 1.0F, 1.0F } };

	public static ResourceLocation DODGE_ICONS = new ResourceLocation(ElenaiDodge2.MODID, "textures/gui/icons.png");
	public static ResourceLocation ADVANCED_DODGE_ICONS = new ResourceLocation(ElenaiDodge2.MODID,
			"textures/gui/advanced_icons.png");
	public static float alpha = 1f;

	@SubscribeEvent
	public void onRenderDodgeGUIEvent(RenderGameOverlayEvent.Post event) {
		if (ModConfig.client.hud.hud && !Minecraft.getMinecraft().player.isCreative()
				&& !Minecraft.getMinecraft().player.isSpectator()
				&& Utils.dodgeTraitUnlocked(Minecraft.getMinecraft().player)) {

			if ((event.getType() == ElementType.ALL && ModConfig.client.hud.compatHud)
					|| (event.getType() == ElementType.FOOD && !ModConfig.client.hud.compatHud)) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(DODGE_ICONS);
				GlStateManager.enableBlend();
				enableAlpha(alpha);

				if (alpha > 0) {
					renderFeathers(event.getResolution().getScaledHeight(), event.getResolution().getScaledWidth(),
							ClientStorage.dodges, ClientStorage.weight, ClientStorage.healing, 16, 25, 34, 43, 52, 61,
							70, PatronRewardHandler.localPatronTier);

					renderAbsorptionFeathers(event.getResolution().getScaledHeight(),
							event.getResolution().getScaledWidth(), ClientStorage.absorption, ClientStorage.weight,
							ClientStorage.healing, 79, 88);
				}
				disableAlpha(alpha);

				Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
				GlStateManager.disableBlend();
			}
		}
	}

	public static void renderFeathers(int screenHeight, int screenWidth, int dodges, int weight, boolean healing,
			int noFeather, int halfFeather, int fullFeather, int armoredFeather, int halfArmoredFeather,
			int halfMixedFeather, int mixedFeather, int patronLevel) {
		GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
		ResourceLocation texture = DODGE_ICONS;
		if (patronLevel > 4 && patronLevel != 99) {
			texture = ADVANCED_DODGE_ICONS;
			patronLevel -= 5;
			patronLevel *= 9;
		} else if (patronLevel > 0 && patronLevel != 99) {
			patronLevel += 1;
			patronLevel *= 9;
		}

		if (patronLevel == 99) {
			patronLevel = 5;
			patronLevel *= 9;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int visibleDodges = Math.max(0, dodges - Math.max(0, weight));
		int top = screenHeight - GuiIngameForge.right_height + ModConfig.client.hud.yOffset;
		renderLayeredBar(gui, screenWidth, top, visibleDodges, patronLevel, noFeather, halfFeather, fullFeather,
				halfArmoredFeather, armoredFeather, FEATHER_LAYER_COLORS, healing, ClientStorage.failed, true);
		GuiIngameForge.right_height += 10;
	}

	public static void renderAbsorptionFeathers(int screenHeight, int screenWidth, int dodges, int weight,
			boolean healing, int halfFeather, int fullFeather) {
		if (dodges <= 0) {
			return;
		}
		GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
		Minecraft.getMinecraft().getTextureManager().bindTexture(DODGE_ICONS);
		int uncoveredWeight = Math.max(0, weight - ClientStorage.dodges);
		int visibleAbsorption = Math.max(0, dodges - uncoveredWeight);
		if (visibleAbsorption <= 0) {
			return;
		}
		int top = screenHeight - GuiIngameForge.right_height + ModConfig.client.hud.yOffset;
		renderLayeredBar(gui, screenWidth, top, visibleAbsorption, 0, 16, halfFeather, fullFeather, 52, 43,
				ABSORPTION_LAYER_COLORS, healing, false, false);
		GuiIngameForge.right_height += 10;
	}

	private static void renderLayeredBar(GuiIngame gui, int screenWidth, int top, int amount, int textureY,
			int emptyU, int halfU, int fullU, int layerHalfU, int layerFullU, float[][] colors, boolean healing,
			boolean failed, boolean drawEmptySlots) {
		int right = (screenWidth / 2 + 82) + ModConfig.client.hud.xOffset;
		if (drawEmptySlots) {
			for (int slot = 0; slot < FEATHER_SLOTS; slot++) {
				gui.drawTexturedModalRect(right - (slot * 8), top, emptyU, textureY, 9, 9);
			}
		}

		int layers = MathHelper.ceil(amount / (float) UNITS_PER_LAYER);
		for (int slot = 0; slot < FEATHER_SLOTS; slot++) {
			int x = right - (slot * 8);
			int unit = (slot * 2) + 1;
			int visibleLayer = -1;
			int visibleLayerAmount = 0;
			boolean half = false;

			for (int layer = 0; layer < layers; layer++) {
				int layerAmount = MathHelper.clamp(amount - (layer * UNITS_PER_LAYER), 0, UNITS_PER_LAYER);
				if (unit < layerAmount) {
					visibleLayer = layer;
					visibleLayerAmount = layerAmount;
					half = false;
				} else if (unit == layerAmount) {
					visibleLayer = layer;
					visibleLayerAmount = layerAmount;
					half = true;
				}
			}

			if (visibleLayer < 0 || visibleLayerAmount <= 0) {
				continue;
			}

			float[] color = colors[visibleLayer % colors.length];
			boolean useBaseSprite = visibleLayer % colors.length == colors.length - 1;
			GlStateManager.color(color[0], color[1], color[2], alpha);
			gui.drawTexturedModalRect(x, top, useBaseSprite ? (half ? halfU : fullU) : (half ? layerHalfU : layerFullU),
					textureY, 9, 9);
			GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		}

		for (int slot = 0; slot < FEATHER_SLOTS; slot++) {
			int x = right - (slot * 8);
			if (!drawEmptySlots && amount <= slot * 2) {
				continue;
			}

			if (healing) {
				gui.drawTexturedModalRect(x, top, 16, 9, 9, 9);
			} else if (failed && ModConfig.client.hud.flash) {
				gui.drawTexturedModalRect(x, top, 43, 9, 9, 9);
			}
		}
	}

	public static void enableAlpha(float alpha) {
		GlStateManager.enableBlend();

		if (alpha == 1f)
			return;

		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	public static void disableAlpha(float alpha) {
		GlStateManager.disableBlend();

		if (alpha == 1f)
			return;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

}
