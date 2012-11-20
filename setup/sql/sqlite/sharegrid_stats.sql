
--
-- Authors:
-- - Marco Guazzone (marco.guazzone@gmail.com)
--
-- ----------------------------------------------------------------------------
--
-- Copyright (C) 2009       Marco Guazzone
--                          [Distributed Computing System (DCS) Group,
--                           Computer Science Institute,
--                           Department of Science and Technological Innovation,
--                           University of Piemonte Orientale,
--                           Alessandria (Italy)]
--
-- This file is part of dcsj-sharegrid-stats.
--
-- dcsj-sharegrid-stats is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published
-- by the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- dcsj-sharegrid-stats is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with dcsj-sharegrid-stats.  If not, see <http://www.gnu.org/licenses/>.
--

DROP TABLE IF EXISTS og_peer;
CREATE TABLE og_peer
(
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL
);

DROP TABLE IF EXISTS og_peer_status;
CREATE TABLE og_peer_status
(
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL
);

DROP TABLE IF EXISTS og_peer_gum_status;
CREATE TABLE og_peer_gum_status
(
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL
);

DROP TABLE IF EXISTS og_peer_stats;
CREATE TABLE og_peer_stats
(
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	peer INTEGER NOT NULL,
	stats_ts TEXT NOT NULL,
	status INTEGER NOT NULL
);

DROP TABLE IF EXISTS og_peer_gum_stats;
CREATE TABLE og_peer_gum_stats
(
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	peer INTEGER NOT NULL,
	name TEXT NOT NULL,
	stats_ts TEXT,
	status INTEGER NOT NULL,
	uatype TEXT,
	os TEXT,
	proc_family TEXT,
	memory TEXT,
	environment TEXT
);

-- Data

INSERT INTO og_peer_status (name) VALUES ('UP');
INSERT INTO og_peer_status (name) VALUES ('DOWN');

INSERT INTO og_peer_gum_status (name) VALUES ('CONTACTING');
INSERT INTO og_peer_gum_status (name) VALUES ('DONATED');
INSERT INTO og_peer_gum_status (name) VALUES ('IDLE');
INSERT INTO og_peer_gum_status (name) VALUES ('IN_USE');
INSERT INTO og_peer_gum_status (name) VALUES ('OWNER');
