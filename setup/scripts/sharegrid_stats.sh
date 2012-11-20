#!/bin/sh

##
## Authors:
## - Marco Guazzone (marco.guazzone@gmail.com)
##
## ----------------------------------------------------------------------------
##
## Copyright (C) 2009       Marco Guazzone
##                          [Distributed Computing System (DCS) Group,
##                           Computer Science Institute,
##                           Department of Science and Technological Innovation,
##                           University of Piemonte Orientale,
##                           Alessandria (Italy)]
##
## This file is part of dcsj-sharegrid-stats.
##
## dcsj-sharegrid-stats is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published
## by the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## dcsj-sharegrid-stats is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with dcsj-sharegrid-stats.  If not, see <http://www.gnu.org/licenses/>.
##

## Known peers:
##   194.116.23.2:6996    ==> ganesh.csp.it (TIGrid - CSP's peer)
##   130.192.119.202:6333 ==> peersg.unito.it (centrorete.unito.it)
##   130.192.157.16:3081  ==> ourgrid.educ.di.unito.it (DipInfoUnito)
##   130.192.157.16:3082  ==> ourgrid.educ.di.unito.it (DipDstfUnito)
##   130.192.157.16:3083  ==> ourgrid.educ.di.unito.it (DipInfoUnito-Dijkstra)
##   130.192.157.16:3084  ==> ourgrid.educ.di.unito.it (DipInfoUnito-Turing)
##   130.192.157.16:3085  ==> ourgrid.educ.di.unito.it (DipInfoUnito-vonNeumann)
##   130.192.157.16:3086  ==> ourgrid.educ.di.unito.it (DipInfoUnito-Postel)
##   193.206.55.107:30800 ==> ramses.di.unipmn.it (TOPIX)
##   193.206.55.107:30801 ==> ramses.di.unipmn.it (UniPMN - DI)
##   130.192.102.83:30080 ==> eco83.econ.unito.it (DipEconUnito-Prato)
##   130.251.61.218:3081  ==> dst.disi.unige.it (UniGE - DISI)
##   95.140.135.211:3081  ==> 95.140.135.211 (UniPD - V-SIX)

app_ver="1.0.1"
app_jar="dcs-sgstats-$app_ver.jar"
app_path=$PWD
#dtnow=`date +"%Y%m%d_%H%M_%Z"`
dtnow=`date +"%Y%m%d"`

db_driver="org.sqlite.JDBC"
db_file="/tmp/sharegrid_stats.$dtnow.db"
db_dsn="sqlite:/$db_file"
middleware=ourgrid
ourgrid_rmi_port=10009
sqlite=sqlite3

if ! [ -e "$db_file" ];
then
	$sqlite $db_file < $app_path/setup/sql/sqlite/sharegrid_stats.sql
fi

java \
	-cp "$app_path/dist/$app_jar:$app_path/lib/ourgrid.jar:$app_path/lib/sqlitejdbc-v056.jar:$app_path/lib/jopt-simple-3.1.jar" \
	it.unipmn.di.dcs.sharegrid.stats.StatsCollectorApp \
	--middleware "$middleware" \
	--og-port $ourgrid_rmi_port \
	--db-driver "$db_driver" \
	--db-dsn "$db_dsn" \
	--og-peer "193.206.55.107:30800" \
	--og-peer "193.206.55.107:30801" \
	--og-peer "130.192.157.16:3081" \
	--og-peer "130.192.157.16:3082" \
	--og-peer "130.192.157.16:3083" \
	--og-peer "130.192.157.16:3084" \
	--og-peer "130.192.157.16:3085" \
	--og-peer "130.192.157.16:3086" \
	--og-peer "130.192.102.83:30080" \
	--og-peer "130.251.61.218:3081" \
	--og-peer "194.116.23.2:6996" \
	--og-peer "130.192.119.202:6333" \
	--og-peer "95.140.135.211:3081" \
	$*
