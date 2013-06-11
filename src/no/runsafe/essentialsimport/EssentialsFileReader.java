package no.runsafe.essentialsimport;

import no.runsafe.framework.api.IOutput;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class EssentialsFileReader implements Iterable<YamlConfiguration>, Iterator<YamlConfiguration>
{
	public EssentialsFileReader(IOutput output)
	{
		console = output;
	}

	@Override
	public Iterator<YamlConfiguration> iterator()
	{
		return this;
	}

	@Override
	public boolean hasNext()
	{
		if (!sourceDir.exists())
			return false;

		if (files == null)
			files = sourceDir.listFiles();

		return index < files.length;
	}

	@Override
	public YamlConfiguration next()
	{
		if (files == null)
			files = sourceDir.listFiles();

		try
		{
			playerData = new YamlConfiguration();
			playerData.load(files[index]);
			index++;
		}
		catch (IOException e)
		{
			console.write(String.format("Error reading file %s", files[index].getName()));
			index++;
			return null;
		}
		catch (InvalidConfigurationException e)
		{
			console.write(String.format("Error reading file %s - %s", files[index].getName(), e.getMessage()));
			index++;
			return null;
		}
		return playerData;
	}

	public File getFile()
	{
		if (index == 0 || index > files.length)
			return null;
		return files[index - 1];
	}

	@Override
	public void remove()
	{
	}

	IOutput console;
	YamlConfiguration playerData;
	File sourceDir = new File("plugins/Essentials/userdata");
	File[] files = null;
	int index = 0;
}
