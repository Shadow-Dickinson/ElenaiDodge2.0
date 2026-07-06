package com.elenai.elenaidodge2.network.message;

import com.elenai.elenaidodge2.ElenaiDodge2;
import com.elenai.elenaidodge2.event.ArmorTickEventListener;
import com.elenai.elenaidodge2.util.ClientStorage;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class CUpdateConfigMessage implements IMessage {

	/*
	 * A Message to transfer server side values from the Config.
	 */

	private int regenRate, dodges, absorption, maxDodges, dodgeAnimationDuration;
	private float dodgeAnimationIntensity, firstPersonCameraIntensity;
	private String weights;
	private boolean half, tanEnabled, enhancedDodgeEffects, dodgeAnimation, firstPersonDodgeAnimation, firstPersonCameraDodgeAnimation;

	private boolean messageValid;

	public CUpdateConfigMessage() {
		this.messageValid = false;
	}

	public CUpdateConfigMessage(int regenRate, int dodges, String weights, boolean half, int absorption, int maxDodges,
			boolean tanEnabled, boolean enhancedDodgeEffects, boolean dodgeAnimation, int dodgeAnimationDuration,
			float dodgeAnimationIntensity, boolean firstPersonDodgeAnimation, boolean firstPersonCameraDodgeAnimation,
			float firstPersonCameraIntensity) {
		this.regenRate = regenRate;
		this.dodges = dodges;
		this.weights = weights;
		this.half = half;
		this.absorption = absorption;
		this.maxDodges = maxDodges;
		this.tanEnabled = tanEnabled;
		this.enhancedDodgeEffects = enhancedDodgeEffects;
		this.dodgeAnimation = dodgeAnimation;
		this.dodgeAnimationDuration = dodgeAnimationDuration;
		this.dodgeAnimationIntensity = dodgeAnimationIntensity;
		this.firstPersonDodgeAnimation = firstPersonDodgeAnimation;
		this.firstPersonCameraDodgeAnimation = firstPersonCameraDodgeAnimation;
		this.firstPersonCameraIntensity = firstPersonCameraIntensity;


		this.messageValid = true;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		try {
			this.regenRate = buf.readInt();
			this.dodges = buf.readInt();
			this.weights = ByteBufUtils.readUTF8String(buf);
			this.half = buf.readBoolean();
			this.absorption = buf.readInt();
			this.maxDodges = buf.readInt();
			this.tanEnabled = buf.readBoolean();
			this.enhancedDodgeEffects = buf.readBoolean();
			this.dodgeAnimation = buf.readBoolean();
			this.dodgeAnimationDuration = buf.readInt();
			this.dodgeAnimationIntensity = buf.readFloat();
			this.firstPersonDodgeAnimation = buf.readBoolean();
			this.firstPersonCameraDodgeAnimation = buf.readBoolean();
			this.firstPersonCameraIntensity = buf.readFloat();


		} catch (IndexOutOfBoundsException ioe) {
			ElenaiDodge2.LOG.error("Error occured whilst networking!", ioe);
			return;
		}
		this.messageValid = true;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (!this.messageValid) {
			return;
		}
		buf.writeInt(regenRate);
		buf.writeInt(dodges);
		ByteBufUtils.writeUTF8String(buf, weights);
		buf.writeBoolean(half);
		buf.writeInt(absorption);
		buf.writeInt(maxDodges);
		buf.writeBoolean(tanEnabled);
		buf.writeBoolean(enhancedDodgeEffects);
		buf.writeBoolean(dodgeAnimation);
		buf.writeInt(dodgeAnimationDuration);
		buf.writeFloat(dodgeAnimationIntensity);
		buf.writeBoolean(firstPersonDodgeAnimation);
		buf.writeBoolean(firstPersonCameraDodgeAnimation);
		buf.writeFloat(firstPersonCameraIntensity);

	}

	public static class Handler implements IMessageHandler<CUpdateConfigMessage, IMessage> {

		@Override
		public IMessage onMessage(CUpdateConfigMessage message, MessageContext ctx) {
			if (!message.messageValid && ctx.side != Side.CLIENT) {
				return null;
			}
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler)
					.addScheduledTask(() -> processMessage(message, ctx));
			return null;
		}

		void processMessage(CUpdateConfigMessage message, MessageContext ctx) {
			ClientStorage.regenSpeed = message.regenRate;
			ClientStorage.maxDodges = message.maxDodges;

			if (message.dodges != 9999) {
				ClientStorage.dodges = message.dodges;
				ClientStorage.absorption = message.absorption;
			}
			ClientStorage.weightValues = message.weights;
			ClientStorage.halfFeathers = message.half;
			ClientStorage.tanEnabled = message.tanEnabled;
			ClientStorage.enhancedDodgeEffects = message.enhancedDodgeEffects;
			ClientStorage.dodgeAnimation = message.dodgeAnimation;
			ClientStorage.dodgeAnimationDuration = message.dodgeAnimationDuration;
			ClientStorage.dodgeAnimationIntensity = message.dodgeAnimationIntensity;
			ClientStorage.firstPersonDodgeAnimation = message.firstPersonDodgeAnimation;
			ClientStorage.firstPersonCameraDodgeAnimation = message.firstPersonCameraDodgeAnimation;
			ClientStorage.firstPersonCameraIntensity = message.firstPersonCameraIntensity;
			
			// Forces Armor Refresh
			ArmorTickEventListener.previousArmor.clear();

		}
	}
}
