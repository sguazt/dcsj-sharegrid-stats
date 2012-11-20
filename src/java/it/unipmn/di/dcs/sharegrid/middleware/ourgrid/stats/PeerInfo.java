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

package it.unipmn.di.dcs.sharegrid.middleware.ourgrid.stats;

//import java.rmi.RemoteException;
//import java.rmi.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import org.ourgrid.common.id.ObjectID;
import org.ourgrid.peer.manager.status.StatusEntry;

/**
 * Holds information related to an OurGrid peer.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class PeerInfo
{
	private ObjectID peerId;
	private RemoteUIServices uiServices;
	private PeerStatus peerStatus;

	public PeerInfo(String peerHost)
	{
		this.peerId = new ObjectID(peerHost, new Long(Long.MAX_VALUE), peerHost);
		try
		{
			this.uiServices = new RemoteUIServices(peerId.getObjectURL());
			this.peerStatus = PeerStatus.UP;
		}
		catch (Exception re)
		{
			this.peerStatus = PeerStatus.DOWN;
		}
	}

	public PeerStatus getStatus()
	{
		return this.peerStatus;
	}

	public Collection<StatusEntry> getGums()
	{
		Collection<StatusEntry> gums = null;

		if (this.peerStatus == PeerStatus.UP)
		{
			try
			{
				gums = uiServices.getLocalGums();
			}
			catch (Exception e)
			{
				System.err.println("Unable to get information about peer '" + peerId.getName() + "': " + e);
				e.printStackTrace();
			}
		}

		if (gums == null)
		{
			// We're here if either peer is DOWN or a RemoteException happened

			// Return an empty list
			gums = new ArrayList<StatusEntry>();
		}

		return gums;
	}
}
