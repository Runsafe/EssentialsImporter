package no.runsafe.essentialsimport;

import no.runsafe.framework.RunsafeConfigurablePlugin;

public class Plugin extends RunsafeConfigurablePlugin
{
	@Override
	protected void PluginSetup()
	{
		addComponent(UserControlImporter.class);
		addComponent(WarpDriveImporter.class);
	}
}
