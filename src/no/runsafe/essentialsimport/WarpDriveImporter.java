package no.runsafe.essentialsimport;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.plugin.PluginResolver;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.database.WarpRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class WarpDriveImporter extends DataImporter
{
	public WarpDriveImporter(IScheduler scheduler, IOutput output, PluginResolver resolver)
	{
		super(scheduler, output);
		warpRepository = ((RunsafePlugin) resolver.getPlugin("WarpDrive")).getComponent(WarpRepository.class);
		essentials = new EssentialsFileReader(output);
	}

	@Override
	String getName()
	{
		return "warpdrive";
	}

	@Override
	void Import()
	{
		if (!essentials.hasNext())
		{
			console.write(String.format("Essentials userdata not found, skipping!"));
			return;
		}
		console.outputToConsole("Importing user data from Essentials into Runsafe WarpDrive..");
		int count = 0;
		for (YamlConfiguration playerData : essentials)
		{
			if (playerData == null)
				continue;

			String playerName = essentials.getFile().getName().replace(".yml", "");

			ConfigurationSection homes = playerData.getConfigurationSection("homes");
			if (homes == null)
				continue;

			for (String home : homes.getKeys(false))
			{
				ConfigurationSection homeDetails = homes.getConfigurationSection(home);
				RunsafeWorld world = RunsafeServer.Instance.getWorld(homeDetails.getString("world"));
				if (world == null)
				{
					console.write(
						String.format(
							"Not importing home %s for %s in unknown world '%s'.",
							home,
							playerName,
							homeDetails.getString("world")
						)
					);
				}
				else
					warpRepository.Persist(
						playerName,
						home,
						false,
						new RunsafeLocation(
							world,
							homeDetails.getDouble("x"),
							homeDetails.getDouble("y"),
							homeDetails.getDouble("z"),
							(float) homeDetails.getDouble("yaw"),
							(float) homeDetails.getDouble("pitch")
						)
					);
			}
			count++;
		}
		console.write(String.format("Completed import of %d players into the WarpDrive database.", count));
	}

	WarpRepository warpRepository;
	EssentialsFileReader essentials;
}