package com.elenai.elenaidodge2.network.message;

import com.elenai.elenaidodge2.ElenaiDodge2;
import com.elenai.elenaidodge2.api.DodgeEvent.Direction;
import com.elenai.elenaidodge2.effects.ClientDodgeEffects;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class CDodgeEffectsMessage implements IMessage {
	
	/*
	 * A Message to transfer server side values when the player dodges
	 */

	private int entityId, dodges, absorption;
	private String direction;

	private boolean messageValid;

	public CDodgeEffectsMessage() {
		this.messageValid = false;
	}

	public CDodgeEffectsMessage(int dodges, int absorption) {
		this(-1, Direction.FORWARD, dodges, absorption);
	}

	public CDodgeEffectsMessage(int entityId, Direction direction, int dodges, int absorption) {
		this.entityId = entityId;
		this.direction = direction.toString();
		this.dodges = dodges;
		this.absorption = absorption;

		this.messageValid = true;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		try {
			this.entityId = buf.readInt();
			this.direction = ByteBufUtils.readUTF8String(buf);
			this.dodges = buf.readInt();
			this.absorption = buf.readInt();

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
		buf.writeInt(entityId);
		ByteBufUtils.writeUTF8String(buf, direction);
		buf.writeInt(dodges);
		buf.writeInt(absorption);

	}

	public static class Handler implements IMessageHandler<CDodgeEffectsMessage, IMessage> {

		@Override
		public IMessage onMessage(CDodgeEffectsMessage message, MessageContext ctx) {
			if (!message.messageValid && ctx.side != Side.CLIENT) {
				return null;
			}
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler)
					.addScheduledTask(() -> processMessage(message, ctx));
			return null;
		}

		void processMessage(CDodgeEffectsMessage message, MessageContext ctx) {
				ClientDodgeEffects.run(message.entityId, Direction.valueOf(message.direction), message.dodges, message.absorption);
		}
	}
}
