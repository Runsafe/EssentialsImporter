package no.runsafe.essentialsimport;

import no.runsafe.framework.configuration.ConfigurationEngine;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.framework.timer.Worker;

public abstract class DataImporter extends Worker<String, Boolean> implements IConfigurationChanged
{
	public DataImporter(IScheduler scheduler, IOutput output, ConfigurationEngine engine)
	{
		super(scheduler);
		console = output;
		this.engine = engine;
	}

	@Override
	public void process(String key, Boolean value)
	{
		Import();
	}

	@Override
	protected void onWorkerDone()
	{
		synchronized (sync)
		{
			configuration.setConfigValue("imported.".concat(getName()), true);
			engine.save();
		}
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		this.configuration = configuration;
		if (!configuration.getConfigValueAsBoolean("imported.".concat(getName())))
			Push("import", true);
	}

	abstract String getName();

	abstract void Import();

	protected final IOutput console;
	private final ConfigurationEngine engine;
	private IConfiguration configuration;

	static final Object sync = new Object();
}
