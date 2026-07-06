package com.elenai.elenaidodge2.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class MaxFeathersEvent extends Event {
	private final EntityPlayer player;
	private final int baseMaximum;
	private final int absoluteMaximum;
	private int maximum;

	public MaxFeathersEvent(EntityPlayer player, int baseMaximum) {
		this(player, baseMaximum, Integer.MAX_VALUE);
	}

	public MaxFeathersEvent(EntityPlayer player, int baseMaximum, int absoluteMaximum) {
		this.player = player;
		this.absoluteMaximum = Math.max(1, absoluteMaximum);
		this.baseMaximum = clamp(baseMaximum);
		this.maximum = this.baseMaximum;
	}

	public EntityPlayer getPlayer() {
		return player;
	}

	public int getBaseMaximum() {
		return baseMaximum;
	}

	public int getAbsoluteMaximum() {
		return absoluteMaximum;
	}

	public int getMaximum() {
		return maximum;
	}

	public void setMaximum(int maximum) {
		this.maximum = clamp(maximum);
	}

	public void addMaximum(int amount) {
		setMaximum(maximum + amount);
	}

	private int clamp(int value) {
		return Math.max(1, Math.min(value, absoluteMaximum));
	}
}
