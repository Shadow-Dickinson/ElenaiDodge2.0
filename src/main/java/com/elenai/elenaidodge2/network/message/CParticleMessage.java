package com.elenai.elenaidodge2.network.message;

import com.elenai.elenaidodge2.ElenaiDodge2;
import com.elenai.elenaidodge2.api.DodgeEvent.Direction;
import com.elenai.elenaidodge2.particle.ParticleGenerator;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class CParticleMessage implements IMessage {
	
	/*
	 * A Message to allow player.setVelocity to be run from the server
	 */

	private int level;
	private double x, y, z;
	private int direction;
	private float yaw;
	private boolean grounded;
	private boolean enhanced;

	private boolean messageValid;

	public CParticleMessage() {
		this.messageValid = false;
	}

	public CParticleMessage(int level, double x, double y, double z) {
		this.level = level;
		this.x = x;
		this.y = y;
		this.z = z;
		this.direction = Direction.FORWARD.ordinal();
		this.yaw = 0.0F;
		this.grounded = true;
		this.enhanced = false;

		this.messageValid = true;
	}

	public CParticleMessage(int level, double x, double y, double z, Direction direction, float yaw, boolean grounded) {
		this.level = level;
		this.x = x;
		this.y = y;
		this.z = z;
		this.direction = direction.ordinal();
		this.yaw = yaw;
		this.grounded = grounded;
		this.enhanced = true;

		this.messageValid = true;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		try {
			this.level = buf.readInt();
			this.x = buf.readDouble();
			this.y = buf.readDouble();
			this.z = buf.readDouble();
			this.direction = buf.readInt();
			this.yaw = buf.readFloat();
			this.grounded = buf.readBoolean();
			this.enhanced = buf.readBoolean();

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
		buf.writeInt(level);
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeInt(direction);
		buf.writeFloat(yaw);
		buf.writeBoolean(grounded);
		buf.writeBoolean(enhanced);
	}

	public static class Handler implements IMessageHandler<CParticleMessage, IMessage> {

		@Override
		public IMessage onMessage(CParticleMessage message, MessageContext ctx) {
			if (!message.messageValid && ctx.side != Side.CLIENT) {
				return null;
			}
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler)
					.addScheduledTask(() -> processMessage(message, ctx));
			return null;
		}

		void processMessage(CParticleMessage message, MessageContext ctx) {


			EnumParticleTypes particleType = null;
			switch (message.level) {
			case 0:
				particleType = EnumParticleTypes.CLOUD;
				break;
			case 1:
				particleType = EnumParticleTypes.HEART;
				break;
			case 2:
				particleType = EnumParticleTypes.FLAME;
				break;
			case 3:
				break;
			case 4:
				break;
			case 5:
				particleType = EnumParticleTypes.END_ROD;
				break;
			default:
				break;
			}

			if (message.enhanced && particleType != null) {
				spawnDirectionalParticles(message, particleType);
				return;
			}
			
			if(particleType != null && message.level > 0) {
				for (int i = 0; i < 8; ++i) {
					double d0 = Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.02D;
					double d1 = Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.02D;
					double d2 = Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.02D;
					Minecraft.getMinecraft().player.world.spawnParticle(particleType,
							message.x + (double) (Minecraft.getMinecraft().player.world.rand.nextFloat() * 0.6f * 2.0F) - (double) 0.6f - d0 * 10.0D,
							message.y + 0.1,
							message.z + (double) (Minecraft.getMinecraft().player.world.rand.nextFloat() * 0.6f * 2.0F) - (double) 0.6f - d2 * 10.0D, d0, d1,
							d2);
				}
			} else if(particleType != null) {
				for (int i = 0; i < 8; ++i) {
					double d0 = Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.02D;
					double d1 = Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.02D;
					double d2 = Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.02D;
					Minecraft.getMinecraft().player.world.spawnParticle(particleType,
							message.x + (double) (Minecraft.getMinecraft().player.world.rand.nextFloat() * 0.6f * 2.0F) - (double) 0.6f - d0 * 10.0D,
							message.y + (double) (Minecraft.getMinecraft().player.world.rand.nextFloat() * 1.8f) - d1 * 10.0D,
							message.z + (double) (Minecraft.getMinecraft().player.world.rand.nextFloat() * 0.6f * 2.0F) - (double) 0.6f - d2 * 10.0D, d0, d1,
							d2);
				}
			}
			
			else {
			
			ParticleGenerator.generate(message.level, message.x, message.y, message.z);
			}
			
		}

		void spawnDirectionalParticles(CParticleMessage message, EnumParticleTypes particleType) {
			double forwardX = -MathHelper.sin(message.yaw / 180.0F * (float) Math.PI);
			double forwardZ = MathHelper.cos(message.yaw / 180.0F * (float) Math.PI);
			double sideX = MathHelper.cos(message.yaw / 180.0F * (float) Math.PI);
			double sideZ = MathHelper.sin(message.yaw / 180.0F * (float) Math.PI);
			double dirX = forwardX;
			double dirZ = forwardZ;

			if (message.direction == Direction.BACK.ordinal()) {
				dirX = -forwardX;
				dirZ = -forwardZ;
			} else if (message.direction == Direction.LEFT.ordinal()) {
				dirX = sideX;
				dirZ = -sideZ;
			} else if (message.direction == Direction.RIGHT.ordinal()) {
				dirX = -sideX;
				dirZ = sideZ;
			}

			int count = message.grounded ? 10 : 4;
			double height = message.grounded ? 0.08D : 0.55D;
			for (int i = 0; i < count; ++i) {
				double spread = Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.12D;
				double trail = Minecraft.getMinecraft().player.world.rand.nextDouble() * 0.75D;
				double d0 = -dirX * 0.035D + Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.015D;
				double d1 = Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.012D;
				double d2 = -dirZ * 0.035D + Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.015D;
				Minecraft.getMinecraft().player.world.spawnParticle(particleType,
						message.x - dirX * trail + sideX * spread,
						message.y + height + Minecraft.getMinecraft().player.world.rand.nextDouble() * 0.18D,
						message.z - dirZ * trail + sideZ * spread, d0, d1, d2);
			}

			if (message.grounded && message.level == 0) {
				for (int i = 0; i < 2; ++i) {
					Minecraft.getMinecraft().player.world.spawnParticle(EnumParticleTypes.CRIT,
							message.x - dirX * 0.25D + sideX * (Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.16D),
							message.y + 0.18D,
							message.z - dirZ * 0.25D + sideZ * (Minecraft.getMinecraft().player.world.rand.nextGaussian() * 0.16D),
							-dirX * 0.02D, 0.02D, -dirZ * 0.02D);
				}
			}
		}
	}
}
