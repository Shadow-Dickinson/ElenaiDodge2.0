package com.elenai.elenaidodge2.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.elenai.elenaidodge2.ModConfig;
import com.elenai.elenaidodge2.api.DodgeEvent.Direction;

public class DodgeAnimation {
	private static final Map<Integer, DodgeAnimation> animations = new HashMap<>();

	private final Direction direction;
	private int age;

	private DodgeAnimation(Direction direction) {
		this.direction = direction;
	}

	public static void start(int entityId, Direction direction) {
		if (ClientStorage.dodgeAnimation) {
			animations.put(entityId, new DodgeAnimation(direction));
		}
	}

	public static void tick() {
		Iterator<Map.Entry<Integer, DodgeAnimation>> iterator = animations.entrySet().iterator();
		while (iterator.hasNext()) {
			DodgeAnimation animation = iterator.next().getValue();
			animation.age++;
			if (animation.age >= getDuration()) {
				iterator.remove();
			}
		}
	}

	public static DodgeAnimation get(int entityId) {
		return animations.get(entityId);
	}

	public Direction getDirection() {
		return direction;
	}

	public float getProgress(float partialTicks) {
		return Math.min(1.0F, (age + partialTicks) / getDuration());
	}

	public static int getDuration() {
		return Math.max(1, ClientStorage.dodgeAnimationDuration);
	}
}
