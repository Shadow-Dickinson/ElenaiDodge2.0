package com.elenai.elenaidodge2.api;

import com.elenai.elenaidodge2.api.DodgeEvent.Direction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SpendFeatherEvent extends Event {

	protected final EntityPlayer player;
	protected final Direction direction;
	protected int cost;

	/**
	 * SpendFeatherEvent is called whenever a feather is spent
	 * @param cost the amount of cost spent
	 * @param player
	 * @author Elenai
	 */
	public SpendFeatherEvent(int cost, EntityPlayer player) {
		this(cost, player, null);
	}

	public SpendFeatherEvent(int cost, EntityPlayer player, Direction direction) {
		this.cost = cost;
		this.player = player;
		this.direction = direction;
	}
		
		/**
		 * @return Feather Cost
		 */
		public int getCost() {
			return cost;
		}

		/**
		 * Sets the Feather Cost. it is ALWAYS recommended to do setCost(event.getCost() + X) in order to ensure compatibility.
		 * @param force
		 */
		public void setCost(int cost) {
			this.cost = cost;
		}

		public void addCost(int amount) {
			this.cost += amount;
		}

		/**
		 * @return Player Spending cost
		 */
		public EntityPlayer getPlayer() {
			return player;
		}

		public Direction getDirection() {
			return direction;
		}
	
}
