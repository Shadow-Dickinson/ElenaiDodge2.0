package com.elenai.elenaidodge2.command;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

import com.elenai.elenaidodge2.ElenaiDodge2;
import com.elenai.elenaidodge2.util.Utils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

public class CommandElenaiReload extends CommandBase {

	@Override
	public String getName() {
		return "elenaiReload";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/elenaiReload";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		reloadCachedConfig();
		ConfigManager.sync(ElenaiDodge2.MODID, Type.INSTANCE);
		Utils.updateClientConfig();
		sender.sendMessage(new TextComponentString("Elenai Dodge 2 config reloaded."));
	}

	@SuppressWarnings("unchecked")
	private void reloadCachedConfig() throws CommandException {
		try {
			Field configsField = ConfigManager.class.getDeclaredField("CONFIGS");
			configsField.setAccessible(true);
			Map<String, Configuration> configs = (Map<String, Configuration>) configsField.get(null);
			File configFile = new File(Loader.instance().getConfigDir(), "Elenai Dodge 2.cfg");
			Configuration config = configs.get(configFile.getAbsolutePath());
			if (config == null) {
				config = new Configuration(configFile);
				configs.put(configFile.getAbsolutePath(), config);
			}
			config.load();
			markChanged(config);
		} catch (Exception exception) {
			throw new CommandException("Could not reload Elenai Dodge 2 config.");
		}
	}

	private void markChanged(Configuration config) {
		for (String categoryName : config.getCategoryNames()) {
			ConfigCategory category = config.getCategory(categoryName);
			for (Property property : category.getOrderedValues()) {
				if (property.isList()) {
					property.setValues(property.getStringList());
				} else {
					property.setValue(property.getString());
				}
			}
		}
	}
}
