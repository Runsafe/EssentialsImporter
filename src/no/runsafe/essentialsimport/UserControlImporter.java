package no.runsafe.essentialsimport;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserControlImporter extends DataImporter
{
	public UserControlImporter(IScheduler scheduler, IOutput output, IDatabase database)
	{
		super(scheduler, output);
		this.database = database;
		essentials = new EssentialsFileReader(output);
	}

	@Override
	String getName()
	{
		return "usercontrol";
	}

	@Override
	void Import()
	{
		PreparedStatement update = database.prepare(
			"INSERT INTO player_db (`name`,`joined`,`login`,`logout`,`ip`,`banned`,`ban_reason`) VALUES (?,?,?,?,INET_ATON(?),?,?)" +
				"ON DUPLICATE KEY UPDATE " +
				"`joined`=VALUES(`joined`)," +
				"`login`=VALUES(`login`), " +
				"`logout`=VALUES(`logout`)," +
				"`ip`=VALUES(`ip`)," +
				"`banned`=VALUES(`banned`)," +
				"`ban_reason`=VALUES(`ban_reason`)"
		);
		if (!essentials.hasNext())
		{
			console.write(String.format("Essentials userdata not found, skipping!"));
			return;
		}
		console.outputToConsole("Importing user data from Essentials into Runsafe UserControl..");
		int count = 0;
		for (YamlConfiguration playerData : essentials)
		{
			if (playerData == null)
				continue;

			String playerName = essentials.getFile().getName().replace(".yml", "");

			Timestamp login = new Timestamp(playerData.getLong("timestamps.login"));
			Timestamp logout = null;
			if (playerData.contains("timestamps.logout"))
				logout = new Timestamp(playerData.getLong("timestamps.logout"));
			String ip = playerData.getString("ipAddress");

			String banReason = null;
			if (playerData.contains("ban.reason"))
			{
				banReason = playerData.getString("ban.reason");
				if (banReason != null)
				{
					RunsafePlayer player = RunsafeServer.Instance.getPlayer(playerName);
					if (player != null && !player.isBanned())
						banReason = null;
				}
			}
			try
			{
				update.setString(1, playerName);
				update.setTimestamp(2, login);
				update.setTimestamp(3, login);
				update.setTimestamp(4, logout);
				update.setString(5, ip);
				update.setTimestamp(6, banReason == null ? null : logout);
				update.setString(7, banReason);
				update.executeUpdate();
				count++;
			}
			catch (SQLException e)
			{
				console.write(String.format("Failed importing %s: %s", playerData.getName(), ExceptionUtils.getFullStackTrace(e)));
				e.printStackTrace();
			}
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{
			}
		}
		console.write(String.format("Completed import of %d players into the UserControl database.", count));
	}

	private IDatabase database;
	private EssentialsFileReader essentials;
}
