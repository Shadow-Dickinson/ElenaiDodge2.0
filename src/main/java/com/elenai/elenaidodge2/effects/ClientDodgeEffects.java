package com.elenai.elenaidodge2.effects;

import com.elenai.elenaidodge2.gui.DodgeStep;
import com.elenai.elenaidodge2.api.DodgeEvent.Direction;
import com.elenai.elenaidodge2.util.ClientStorage;
import com.elenai.elenaidodge2.util.DodgeAnimation;
import com.elenai.elenaidodge2.util.Utils;

public class ClientDodgeEffects {
	
	/**
	 * Runs the Client Dodge Effects
	 * @param dodges
	 * @param dodgeCost
	 * @side Client
	 */
	public static void run(int dodges, int absorption) {
		ClientStorage.absorption = absorption;
		ClientStorage.dodges = dodges;
		if(ClientStorage.tutorialDodges < 1) {
		ClientStorage.tutorialDodges+=0.25;
		DodgeStep.moveToast.setProgress((float)ClientStorage.tutorialDodges);
		}
		Utils.showDodgeBar();
	}

	public static void run(int entityId, Direction direction, int dodges, int absorption) {
		if (dodges >= 0) {
			ClientStorage.absorption = absorption;
			ClientStorage.dodges = dodges;
			if(ClientStorage.tutorialDodges < 1) {
			ClientStorage.tutorialDodges+=0.25;
			DodgeStep.moveToast.setProgress((float)ClientStorage.tutorialDodges);
			}
			Utils.showDodgeBar();
		}
		DodgeAnimation.start(entityId, direction);
	}
	
}
