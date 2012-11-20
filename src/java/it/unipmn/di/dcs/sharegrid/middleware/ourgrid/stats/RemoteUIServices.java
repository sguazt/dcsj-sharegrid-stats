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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

import org.ourgrid.common.config.Configuration;
import org.ourgrid.common.config.PeerConfiguration;
import org.ourgrid.common.id.ObjectID;
import org.ourgrid.common.spec.RequestSpec.RequestSource;
import org.ourgrid.common.url.PeerURLProvider;
import org.ourgrid.peer.manager.allocation.AllocationStatus;
import org.ourgrid.peer.manager.allocation.AllocationEntry.GumSource;
import org.ourgrid.peer.manager.status.ConsumerStatus;
import org.ourgrid.peer.manager.status.StatusEntry;
import org.ourgrid.peer.manager.status.StatusProvider;

/**
 * This is the class responsible to access the peer's remote
 * services used to get the status information.
 *
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class RemoteUIServices {

	/**
	 * The peer to be inspected.
	 */
	private String objectURL;

	/**
	 * Responsible to provide GuM related info.
	 */
	private StatusProvider statusProvider;


	/**
	 * Creates a UIService to access a peer status information remotely.
	 * 
	 * @param objectURL The peer URL
	 * @throws MalformedURLException If the peer URL is invalid.
	 * @throws RemoteException If the peer could not be contacted.
	 * @throws NotBoundException If the peer is off-line.
	 */
	public RemoteUIServices( String objectURL ) throws MalformedURLException, RemoteException, NotBoundException {

		this.objectURL = objectURL;

		initializeRemoteObjects();

	}


	/**
	 * Try to get the remote objects needed to obtain the peer status.
	 * 
	 * @throws NotBoundException If the peer is offline.
	 * @throws RemoteException If the peer could not be contacted
	 * @throws MalformedURLException If the peer URL is invalid.
	 */
	private synchronized void initializeRemoteObjects() throws MalformedURLException, RemoteException,
		NotBoundException {

		Configuration.getInstance( Configuration.PEER ).setProperty( PeerConfiguration.PROP_NAME, getPeerHostname() );
		Configuration.getInstance().setProperty( Configuration.PROP_PORT, getPeerPort() );

		try {
			this.statusProvider = (StatusProvider) Naming.lookup( PeerURLProvider.statusProvider() );
		} catch ( Exception e ) {
			throw new RemoteException( "Cannot lookup " + PeerURLProvider.statusProvider() + " object" );
		}

	}


	/**
	 * @return the peer's port.
	 */
	private String getPeerPort() {

		return this.objectURL.substring( this.objectURL.lastIndexOf( ':' ) + 1, this.objectURL.lastIndexOf( '/' ) );
	}


	/**
	 * @return the peer's host name
	 */
	private String getPeerHostname() {

		return this.objectURL.substring( 6, this.objectURL.lastIndexOf( ':' ) );
	}


	public String getVersion() {

		try {
			return this.statusProvider.getVersion();
		} catch ( RemoteException e ) {
			return "";
		}
	}


	public String getDescription() {

		try {
			return this.statusProvider.getDescription();
		} catch ( RemoteException e ) {
			return "";
		}
	}


	public String getEmail() {

		try {
			return this.statusProvider.getEmail();
		} catch ( RemoteException e ) {
			return "";
		}
	}


	public String getUptime() {

		try {
			return this.statusProvider.getUptime();
		} catch ( RemoteException e ) {
			return "";
		}
	}


	public String getLatitude() {

		try {
			return String.valueOf( this.statusProvider.getLatitude() );
		} catch ( RemoteException e ) {
			return "";
		}
	}


	public String getLongitude() {

		try {
			return String.valueOf( this.statusProvider.getLongitude().toString() );
		} catch ( RemoteException e ) {
			return "";
		}
	}


	public Collection<StatusEntry> getLocalGums() {

		try {
			return this.statusProvider.getAllGums( GumSource.LOCAL );
		} catch ( RemoteException e ) {
			e.printStackTrace();
			return null;
		}
	}


	public Collection<StatusEntry> getRemoteGums() {

		try {
			return this.statusProvider.getAllGums( GumSource.REMOTE );
		} catch ( RemoteException e ) {
			return null;
		}
	}


	public Collection<StatusEntry> getDonatedGums() {

		try {
			return this.statusProvider.getGums( AllocationStatus.DONATED, RequestSource.FROM_REMOTE_PEER );
		} catch ( RemoteException e ) {
			return null;
		}
	}


	public Collection<StatusEntry> getOwnerGums() {

		try {
			return this.statusProvider.getGums( AllocationStatus.OWNER, RequestSource.FROM_LOCAL_BROKER );
		} catch ( RemoteException e ) {
			return null;
		}
	}


	public Collection<ConsumerStatus> getLocalConsumers() {

		try {
			return this.statusProvider.getConsumersStatus( RequestSource.FROM_LOCAL_BROKER );
		} catch ( RemoteException e ) {
			return null;
		}
	}


	public Collection<ConsumerStatus> getRemoteConsumers() {

		try {
			return this.statusProvider.getConsumersStatus( RequestSource.FROM_REMOTE_PEER );
		} catch ( RemoteException e ) {
			return null;
		}
	}


	public Map<ObjectID,Double> getNetworkOfFavors() {

		try {
			return this.statusProvider.getNetworkOfFavors();
		} catch ( RemoteException e ) {
			return null;
		}
	}

}
