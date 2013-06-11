package no.runsafe.essentialsimport;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.api.IOutput;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.RunsafeWorld;
import no.runsafe.warpdrive.database.WarpRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class WarpDriveImporter extends DataImporter
{
	public WarpDriveImporter(IScheduler scheduler, IOutput output, RunsafeServer server)
	{
		super(scheduler, output);
		warpRepository = ((RunsafePlugin) server.getPlugin("WarpDrive")).getComponent(WarpRepository.class);
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
				{
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
			}
			count++;
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{
			}
		}
		console.write(String.format("Completed import of %d players into the WarpDrive database.", count));
	}

	WarpRepository warpRepository;
	EssentialsFileReader essentials;
}
