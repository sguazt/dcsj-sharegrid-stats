/*
 * Copyright (C) 2009  Distributed Computing System (DCS) Group, Computer
 * Science Department - University of Piemonte Orientale, Alessandria (Italy).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipmn.di.dcs.sharegrid.stats;

//import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
//import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.ourgrid.common.spec.GumSpec;
//import org.ourgrid.peer.manager.allocation.AllocationStatus;
import org.ourgrid.peer.manager.status.StatusEntry;

/**
 * Entry-point for stats collection application.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class StatsCollectorApp
{
	private static final int DEFAULT_PORT_OPT = 10009;
	private static final String DEFAULT_MIDDLEWARE_OPT = "ourgrid";


	public static void main(String[] args) throws Exception
	{
		// Define options
        OptionParser parser = new OptionParser();
        parser.accepts("db-driver", "DB driver class").withRequiredArg();
        parser.accepts("db-dsn", "DB connection string: <subprotocol:subname>").withRequiredArg();
        parser.accepts("help",  "Show this help message");
        parser.accepts("middleware", "The middleware identifier").withRequiredArg();
		parser.accepts("og-peer", "The name of a OurGrid peer: <host-name:port>").withRequiredArg();
		parser.accepts("og-port", "Local OurGrid RMI port").withRequiredArg().ofType(Integer.class);
        parser.accepts("verbose", "Show execution informations");

		// Parse options
        OptionSet options = parser.parse(args);

		// Check options
		if (
				options.has("help")
				|| !options.has("db-driver")
				|| !options.has("db-dsn")
		) {
			int retval = 0;
			if (!options.has("help"))
			{
				retval = 1;

				if (!options.has("db-driver"))
				{
					System.err.println("DB driver class not specified");
				}
				if (!options.has("db-dsn"))
				{
					System.err.println("DB DSN not specified");
				}
			}
			parser.printHelpOn(System.out);
			System.exit(retval);
		}

		String dbDriver;
		String dbDSN;
		Set<String> ogPeersSet = new HashSet<String>();
		int ogRMIPort;
		String middlewareId;
		boolean verbose;

		dbDriver = (String) options.valueOf("db-driver");
		dbDSN = (String) options.valueOf("db-dsn");
		if (options.has("og-peer"))
		{
			for (Object peer : options.valuesOf("og-peer"))
			{
				ogPeersSet.add((String) peer);
			}
		}
		ogRMIPort = options.has("port") ? (Integer) options.valueOf("port") : DEFAULT_PORT_OPT;
		middlewareId = options.has("middleware") ? (String) options.valueOf("middleware") : DEFAULT_MIDDLEWARE_OPT;
		verbose = options.has("verbose") ? true : false;

		IMiddlewareStatsCollector statsCollector;
		statsCollector = new MiddlewareStatsCollectorFactory().createFromMiddlewareId(middlewareId);

		statsCollector.setDbDriver(dbDriver);
		statsCollector.setDbDSN(dbDSN);
		statsCollector.setVerbose(verbose);
		if (statsCollector.getType() == MiddlewareType.OURGRID)
		{
			OurGridStatsCollector ogCollector = (OurGridStatsCollector) statsCollector;
			ogCollector.setRMIPort(ogRMIPort);
			ogCollector.setPeers(ogPeersSet);
		}

		statsCollector.collect();
	}
}
