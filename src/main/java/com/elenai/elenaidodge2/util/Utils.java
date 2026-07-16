package com.elenai.elenaidodge2.util;

import com.elenai.elenaidodge2.ElenaiDodge2;
import com.elenai.elenaidodge2.ModConfig;
import com.elenai.elenaidodge2.api.DodgeEvent;
import com.elenai.elenaidodge2.api.DodgeEvent.Direction;
import com.elenai.elenaidodge2.api.MaxFeathersEvent;
import com.elenai.elenaidodge2.capability.absorption.AbsorptionProvider;
import com.elenai.elenaidodge2.capability.absorption.IAbsorption;
import com.elenai.elenaidodge2.capability.dodges.DodgesProvider;
import com.elenai.elenaidodge2.capability.dodges.IDodges;
import com.elenai.elenaidodge2.effects.ServerDodgeEffects;
import com.elenai.elenaidodge2.event.ClientTickEventListener;
import com.elenai.elenaidodge2.gui.DodgeGui;
import com.elenai.elenaidodge2.network.PacketHandler;
import com.elenai.elenaidodge2.network.message.CDodgeEffectsMessage;
import com.elenai.elenaidodge2.network.message.CFeatherFailureMessage;
import com.elenai.elenaidodge2.network.message.CInitPlayerMessage;
import com.elenai.elenaidodge2.network.message.CUpdateConfigMessage;
import com.elenai.elenaidodge2.network.message.CVelocityMessage;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

public class Utils {

	/**
	 * Allows player.setVelocity to be run from the server.
	 * @param x
	 * @param y
	 * @param z
	 * @param player
	 * @author Elenai
	 * @side Server
	 */
	public static void setPlayerVelocity(double x, double y, double z, EntityPlayer player) {
		PacketHandler.instance.sendTo(new CVelocityMessage(x, y, z), (EntityPlayerMP) player);
	}
	
	/**
	 * Tells the Client to flash the GUI white and show the GUI.
	 * 
	 * @param player
	 * @author Elenai
	 * @side Server
	 */
	public static void cancelledByFeathers(EntityPlayer player) {
		PacketHandler.instance.sendTo(new CFeatherFailureMessage(), (EntityPlayerMP) player);
	}
	
	/**
	 * Dodges the player in the given direction.
	 * @param direction
	 * @param player
	 * @author Elenai
	 * @side Server
	 */
	public static void handleDodge(Direction direction, DodgeEvent.ServerDodgeEvent event, EntityPlayerMP player) {
		
		double f = event.getForce();
		double motionX;
		double motionZ;

		switch (direction) {
		case LEFT:
			motionX = (double) (MathHelper.cos(player.rotationYaw / 180.0F * (float) Math.PI)
					* MathHelper.cos(1 / 180.0F * (float) Math.PI) * f);
			motionZ = (double) -(-MathHelper.sin(player.rotationYaw / 180.0F * (float) Math.PI)
					* MathHelper.cos(1 / 180.0F * (float) Math.PI) * f);
			break;
		case RIGHT:
			motionX = (double) -(MathHelper.cos(player.rotationYaw / 180.0F * (float) Math.PI)
					* MathHelper.cos(1 / 180.0F * (float) Math.PI) * f);
			motionZ = (double) (-MathHelper.sin(player.rotationYaw / 180.0F * (float) Math.PI)
					* MathHelper.cos(1 / 180.0F * (float) Math.PI) * f);
			break;
		case FORWARD:
			motionX = (double) (-MathHelper.sin(player.rotationYaw / 180.0F * (float) Math.PI)
					* MathHelper.cos(1 / 180.0F * (float) Math.PI) * f);
			motionZ = (double) (MathHelper.cos(player.rotationYaw / 180.0F * (float) Math.PI)
					* MathHelper.cos(1 / 180.0F * (float) Math.PI) * f);
			break;
		case BACK:
			motionX = (double) -(-MathHelper.sin(player.rotationYaw / 180.0F * (float) Math.PI)
					* MathHelper.cos(1 / 180.0F * (float) Math.PI) * f);
			motionZ = (double) -(MathHelper.cos(player.rotationYaw / 180.0F * (float) Math.PI)
					* MathHelper.cos(1 / 180.0F * (float) Math.PI) * f);
			break;
		default:
			motionX = 0;
			motionZ = 0;
			ElenaiDodge2.LOG.error("DodgeEvent Posted and Received but no direction given!");
		}
		setPlayerVelocity(motionX, ModConfig.common.balance.verticality, motionZ, player);
		ServerDodgeEffects.run(player, direction);
		IDodges d = player.getCapability(DodgesProvider.DODGES_CAP, null);
		IAbsorption a = player.getCapability(AbsorptionProvider.ABSORPTION_CAP, null);
		PacketHandler.instance.sendTo(new CDodgeEffectsMessage(player.getEntityId(), direction, d.getDodges(), a.getAbsorption()), (EntityPlayerMP) player);
		PacketHandler.instance.sendToAllTracking(new CDodgeEffectsMessage(player.getEntityId(), direction, -1, 0), (EntityPlayerMP) player);
	}
	
	/**
	 * Returns the Player's Dodge Force when all default calculations have been applied.
	 * @param player
	 * @return The Player's total Dodge Force
	 * @author Elenai
	 * @side Server
	 */
	public static double calculateForce(EntityPlayer player) {
		return ModConfig.common.balance.force;
	}

	
	/**
	 * A method to be run when the player first joins the world.
	 * @author Elenai
	 * @param player
	 */
	public static void initPlayer(EntityPlayer player) {
		int maxDodges = getMaxDodges(player);
		PacketHandler.instance.sendTo(new CInitPlayerMessage(maxDodges, maxDodges), (EntityPlayerMP) player);
		IDodges d = player.getCapability(DodgesProvider.DODGES_CAP, null);
		d.set(maxDodges);
	}
	
	/**
	 * Updates the Client Config for the given player.
	 * @author Elenai
	 * @param player
	 */
	public static void updateClientConfig(EntityPlayerMP player) {
		IDodges d = player.getCapability(DodgesProvider.DODGES_CAP, null);
		IAbsorption a = player.getCapability(AbsorptionProvider.ABSORPTION_CAP, null);
		PacketHandler.instance.sendTo(new CUpdateConfigMessage(ModConfig.common.feathers.rate, d.getDodges(), arrayToString(ModConfig.common.weights.weights),
				ModConfig.common.feathers.half, a.getAbsorption(), getMaxDodges(player), ModConfig.common.integration.toughAsNails.enabled,
				ModConfig.common.integration.reskillable.enabled, ModConfig.common.misc.enhancedDodgeEffects, ModConfig.common.misc.dodgeAnimation, ModConfig.common.misc.dodgeAnimationDuration,
				(float) ModConfig.common.misc.dodgeAnimationIntensity, ModConfig.common.misc.firstPersonDodgeAnimation,
				ModConfig.common.misc.firstPersonCameraDodgeAnimation, (float) ModConfig.common.misc.firstPersonCameraIntensity), player);
	}
	
	/**
	 * Updates the Client Config for all players.
	 * @author Elenai
	 */
	public static void updateClientConfig() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (server != null && server.getPlayerList() != null) {
			for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
				updateClientConfig(player);
			}
			return;
		}
		PacketHandler.instance.sendToAll(new CUpdateConfigMessage(ModConfig.common.feathers.rate, 9999, arrayToString(ModConfig.common.weights.weights),
				ModConfig.common.feathers.half, 9999, getMaxDodges(), ModConfig.common.integration.toughAsNails.enabled,
				ModConfig.common.integration.reskillable.enabled, ModConfig.common.misc.enhancedDodgeEffects, ModConfig.common.misc.dodgeAnimation, ModConfig.common.misc.dodgeAnimationDuration,
				(float) ModConfig.common.misc.dodgeAnimationIntensity, ModConfig.common.misc.firstPersonDodgeAnimation,
				ModConfig.common.misc.firstPersonCameraDodgeAnimation, (float) ModConfig.common.misc.firstPersonCameraIntensity));
	}

	public static int getMaxDodges() {
		return Math.max(1, ModConfig.common.feathers.maximum);
	}

	public static int getBaseDodges() {
		return Math.max(1, Math.min(ModConfig.common.feathers.base, getMaxDodges()));
	}

	public static int getMaxDodges(EntityPlayer player) {
		if (player == null) {
			return getBaseDodges();
		}
		MaxFeathersEvent event = new MaxFeathersEvent(player, getBaseDodges(), getMaxDodges());
		MinecraftForge.EVENT_BUS.post(event);
		return event.getMaximum();
	}
	
	/**
	 * Returns the cumulative total of an equipped enchantment type.
	 * @author Diesieben07
	 * @param enchantment
	 * @param entity
	 * @return
	 */
    public static int getTotalEnchantmentLevel(Enchantment enchantment, EntityLivingBase entity)
    {
        Iterable<ItemStack> iterable = enchantment.getEntityEquipment(entity);

        if (iterable == null)
        {
            return 0;
        }
        else
        {
            int i = 0;

            for (ItemStack itemstack : iterable)
            {
                int j = EnchantmentHelper.getEnchantmentLevel(enchantment, itemstack);
                i+=j;
            }

            return i;
        }
    }
    
    /**
     * Converts a String Array into a CSV String
     * @author Nico Huysamen
     * @author Adapted by Elenai
     * @param string
     * @return
     */
    public static String arrayToString(String[] string) {
    	if (string.length > 0) {
    	    StringBuilder stringBuilder = new StringBuilder();

    	    for (String n : string) {
    	    	stringBuilder.append("").append(n.replace("'", "\\'")).append(",");
    	    }

    	    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    	    return stringBuilder.toString();
    	} else {
    	    return "";
    	}
    }
    
	/**
	 * Shows the player's dodge bar if it is hidden
	 */
	public static void showDodgeBar() {
		if (DodgeGui.alpha < 1) {
			DodgeGui.alpha = 1f;
			ClientTickEventListener.alpha = ClientTickEventListener.alphaLen;
		}
	}
	
	/**
	 * Checks if the player has the Dodge trait added in Reskillable implementation.
	 * If Reskillable is not installed, this will simply return true.
	 * 
	 * @return Dodge Trait Unlocked
	 * @author Elenai
	 */
	public static boolean dodgeTraitUnlocked(EntityPlayer player) {
		if (Loader.isModLoaded("reskillable") && isReskillableIntegrationEnabled(player)) {
			codersafterdark.reskillable.api.skill.Skill agility = codersafterdark.reskillable.api.ReskillableRegistries.SKILLS
					.getValue(new ResourceLocation(codersafterdark.reskillable.lib.LibMisc.MOD_ID, "agility"));
			codersafterdark.reskillable.api.unlockable.Unlockable dodge = codersafterdark.reskillable.api.ReskillableRegistries.UNLOCKABLES
					.getValue(new ResourceLocation(ElenaiDodge2.MODID, "dodge"));
			if (agility == null || dodge == null) {
				return true;
			}
			return (codersafterdark.reskillable.api.data.PlayerDataHandler.get(player)
					.getSkillInfo(agility)
					.isUnlocked(dodge));
		}
		return true;
	}

	public static boolean isReskillableIntegrationEnabled(EntityPlayer player) {
		if (player != null && player.world != null && player.world.isRemote) {
			return ClientStorage.reskillableEnabled;
		}
		return ModConfig.common.integration.reskillable.enabled;
	}
	
	/**
	 * Checks if the player has the TAN implementation enabled and the mod is present on the Client.
	 * If Reskillable is not installed, this will simply return true.
	 * 
	 * @return Dodge Trait Unlocked
	 * @author Elenai
	 */
	public static boolean tanEnabled(EntityPlayerSP player) {
		if (Loader.isModLoaded("toughasnails") && ClientStorage.tanEnabled) {
			return true;
		}
		return false;
	}
	
}
