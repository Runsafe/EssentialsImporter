package no.runsafe.essentialsimport;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.framework.timer.Worker;

public abstract class DataImporter extends Worker<String, Boolean> implements IConfigurationChanged
{
	public DataImporter(IScheduler scheduler, IOutput output)
	{
		super(scheduler);
		console = output;
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
			configuration.save();
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
	private IConfiguration configuration;

	static final Object sync = new Object();
}
