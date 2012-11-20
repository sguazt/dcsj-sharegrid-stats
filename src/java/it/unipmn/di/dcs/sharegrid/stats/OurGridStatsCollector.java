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

import it.unipmn.di.dcs.sharegrid.middleware.ourgrid.stats.PeerInfo;
import it.unipmn.di.dcs.sharegrid.middleware.ourgrid.stats.PeerStatus;
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
 * Stats collector for the OurGrid v3 middleware.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class OurGridStatsCollector implements IMiddlewareStatsCollector
{
	//private static final int DEFAULT_PORT_OPT = 10009;
	private static final String PEER_TABLE_SQL = "og_peer";
	private static final String PEER_STATS_TABLE_SQL = "og_peer_stats";
	private static final String PEER_STATUS_TABLE_SQL = "og_peer_status";
	private static final String GUM_STATS_TABLE_SQL = "og_peer_gum_stats";
	private static final String GUM_STATUS_TABLE_SQL = "og_peer_gum_status";
	private static final String PEER_INSERT_SQL = "INSERT INTO " + PEER_TABLE_SQL + " (name) VALUES (?);";
	private static final String PEER_SELECT_ID_SQL = "SELECT id FROM " + PEER_TABLE_SQL + " WHERE UPPER(name)=UPPER(?);";
	private static final String PEER_STATS_INSERT_SQL = "INSERT INTO " + PEER_STATS_TABLE_SQL + " (peer,stats_ts,status) VALUES (?,?,?);";
	private static final String PEER_STATUS_SELECT_SQL = "SELECT id,name FROM " + PEER_STATUS_TABLE_SQL + ";";
	private static final String GUM_STATS_INSERT_SQL = "INSERT INTO " + GUM_STATS_TABLE_SQL + " (peer,name,stats_ts,status,uatype,os,proc_family,memory,environment) VALUES (?,?,?,?,?,?,?,?,?);";
	private static final String GUM_STATUS_SELECT_SQL = "SELECT id,name FROM " + GUM_STATUS_TABLE_SQL + ";";

	private Set<String> peersSet;
	private String dbDriver;
	private String dbDSN;
	private int rmiPort;
	private boolean verbose;

	public OurGridStatsCollector()
	{
		this.peersSet = new HashSet<String>();
	}

	public void setPeers(Collection<String> value)
	{
		this.peersSet.clear();
		this.peersSet.addAll(value);
	}

	public void setRMIPort(int value)
	{
		this.rmiPort = value;
	}

	//@{ IMiddlewareStats interface ////////////////////////////////////////////

	public MiddlewareType getType()
	{
		return MiddlewareType.OURGRID;
	}

	public void setDbDriver(String value)
	{
		this.dbDriver = value;
	}

	public void setDbDSN(String value)
	{
		this.dbDSN = value;
	}

	public void setVerbose(boolean value)
	{
		this.verbose = value;
	}

	public void collect() throws Exception
	{
		//peersSet.add("193.206.55.107:30800"); // TOPIX
		//peersSet.add("193.206.55.107:30801"); // UniPMN - DI
		//peersSet.add("130.192.157.16:3081"); // ourgrid.educ.di.unito.it - LabInfo
		//peersSet.add("130.192.157.16:3082"); // ourgrid.educ.di.unito.it - DSTF
		//peersSet.add("130.192.157.16:3083"); // ourgrid.educ.di.unito.it - LabInfoD
		//peersSet.add("130.192.157.16:3084"); // ourgrid.educ.di.unito.it - LabInfoT
		//peersSet.add("130.192.157.16:3085"); // ourgrid.educ.di.unito.it - LabInfoV
		//peersSet.add("130.192.157.16:3086"); // ourgrid.educ.di.unito.it - LabInfoP
		//peersSet.add("130.192.102.83:30080"); // eco83.econ.unito.it
		//peersSet.add("130.251.61.218:3081"); // DISI-UniGe
		//peersSet.add("194.116.23.2:6996"); // TIGrid - CSP's peer
		//peersSet.add("130.192.119.202:6333"); // centrorete.unito.it

		// Init the Registry using the port number specified by
		Registry registry = null;
		try
		{
			registry = LocateRegistry.createRegistry(this.rmiPort);
			//Naming.lookup(STATUS_URL);
		}
		catch (Exception e)
		{
			System.err.println("Unable to create the RMI Registry: " + e);
			System.exit(1);
		}

		// Connect to DB
		Class.forName(this.dbDriver);
		Connection conn;
		conn = DriverManager.getConnection("jdbc:" + this.dbDSN);

		Map<String,Integer> peerStatusMap = new HashMap<String,Integer>();
		Map<String,Integer> gumStatusMap = new HashMap<String,Integer>();

		ResultSet rs = null;
		Statement stmt = null;

		// Retrieve peer status and fill the map
		stmt = conn.createStatement();
		rs = stmt.executeQuery(PEER_STATUS_SELECT_SQL);
		while (rs.next())
		{
			peerStatusMap.put(rs.getString(2), rs.getInt(1));
		}
		rs.close();
		rs = null;
		stmt.close();
		stmt = null;

		// Retrieve GuM status and fill the map
		stmt = conn.createStatement();
		rs = stmt.executeQuery(GUM_STATUS_SELECT_SQL);
		while (rs.next())
		{
			gumStatusMap.put(rs.getString(2), rs.getInt(1));
		}
		rs.close();
		rs = null;
		stmt.close();
		stmt = null;

		PreparedStatement peerIdStmt = conn.prepareStatement(PEER_SELECT_ID_SQL);
		PreparedStatement peerInsStmt = conn.prepareStatement(PEER_INSERT_SQL);
		PreparedStatement peerStatsInsStmt = conn.prepareStatement(PEER_STATS_INSERT_SQL);
		PreparedStatement gumStatsInsStmt = conn.prepareStatement(GUM_STATS_INSERT_SQL);

		String dtNowUTC = GetUTCDateString(new Date());

		for (String peerName : this.peersSet)
		{
			// New peer
			String peerUrl = "rmi://" + peerName + "/";

			PeerInfo peerInfo;
			peerInfo = new PeerInfo(peerUrl);

			try
			{
				Integer peerId = null;

				// Check for peer existence and get its ID
				peerId = GetPeerIdFromNameSQL(peerIdStmt, peerName);
				if (peerId == null)
				{
					// Peer not found => insert a new one

					peerInsStmt.setString(1, peerName);
					peerInsStmt.execute();

					peerId = GetPeerIdFromNameSQL(peerIdStmt, peerName);
				}

				// Update peer stats
				peerStatsInsStmt.setInt(1, peerId);
				peerStatsInsStmt.setString(2, dtNowUTC);
				peerStatsInsStmt.setInt(3, peerStatusMap.get(peerInfo.getStatus().toString()));
				peerStatsInsStmt.execute();

				// Looks for GuMs
				for (StatusEntry gumEntry : peerInfo.getGums())
				{
					try
					{
						GumSpec gumSpec = gumEntry.getGumSpec();

						if (verbose)
						{
							System.out.println(
								peerName
								+ "," + gumSpec.getAttribute(GumSpec.ATT_NAME)
								+ "," + gumEntry.getStatus()
								+ "," + gumSpec.getAttribute(GumSpec.ATT_TYPE)
								+ "," + gumSpec.getAttribute(GumSpec.ATT_OS)
								+ "," + gumSpec.getAttribute(GumSpec.ATT_PROCESSOR_FAMILY)
								+ "," + gumSpec.getAttribute(GumSpec.ATT_MEM)
								+ "," + gumSpec.getAttribute(GumSpec.ATT_ENVIRONMENT)
							);
						}

						// Update GuM stats
						gumStatsInsStmt.setInt(1, peerId);
						gumStatsInsStmt.setString(2, gumSpec.getAttribute(GumSpec.ATT_NAME));
						gumStatsInsStmt.setString(3, dtNowUTC);
						gumStatsInsStmt.setInt(4, gumStatusMap.get(gumEntry.getStatus().toString()));
						gumStatsInsStmt.setString(5, gumSpec.getAttribute(GumSpec.ATT_TYPE));
						gumStatsInsStmt.setString(6, gumSpec.getAttribute(GumSpec.ATT_OS));
						gumStatsInsStmt.setString(7, gumSpec.getAttribute(GumSpec.ATT_PROCESSOR_FAMILY));
						gumStatsInsStmt.setString(8, gumSpec.getAttribute(GumSpec.ATT_MEM));
						gumStatsInsStmt.setString(9, gumSpec.getAttribute(GumSpec.ATT_ENVIRONMENT));
						gumStatsInsStmt.addBatch();
					}
					catch (Exception e2)
					{
						System.err.println("Unable to obtain status information for gum '" + gumEntry + "' ('" + peerName + "'): " + e2);
						e2.printStackTrace();
					}
				}
			}
			catch (Exception e1)
			{
				System.err.println("Unable to obtain status information for peer '" + peerName + "': " + e1);
				e1.printStackTrace();
			}
		}

		// Write to DB the collected GuMs stats
		conn.setAutoCommit(false);
		gumStatsInsStmt.executeBatch();
		conn.setAutoCommit(true);

		// Resources clean-up
		gumStatsInsStmt.close();
		gumStatsInsStmt = null;
		peerStatsInsStmt.close();
		peerStatsInsStmt = null;
		peerInsStmt.close();
		peerInsStmt = null;
		peerIdStmt.close();
		peerIdStmt = null;

		conn.close();
		conn = null;
	}

	//@} IMiddlewareStats interface ////////////////////////////////////////////

	private static String GetUTCDateString(Date date)
	{
		Date dtNow = new Date();
		DateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dtFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dtFmt.format(dtNow);
	}

	private static Integer GetPeerIdFromNameSQL(PreparedStatement prepStmt, String peerName) throws SQLException
	{
		Integer peerId = null;

		prepStmt.setString(1, peerName);

		ResultSet rs = null;
		rs = prepStmt.executeQuery();
		while (rs.next())
		{
			peerId = rs.getInt(1);
		}
		rs.close();
		rs = null;

		return peerId;
	}
}
