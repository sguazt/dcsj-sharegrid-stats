#!/bin/env perl

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

use strict;
use warnings;

use DBI ();
use IO::Dir ();

my $dh = new IO::Dir(".");
unless (ref($dh))
{
	die "Cannot read data dir: $!";
}

my @dbfiles = ();
while (my $dbfile = $dh->read())
{
	chomp($dbfile);

	next unless $dbfile =~ m/^sharegrid_stats\.\d+\.db$/oi;

	push(@dbfiles, $dbfile);
}

my %gum_status = (
	CONTACTING => 1,
	DONATED => 2,
	IDLE => 3,
	IN_USE => 4,
	OWNER => 5,
);
my $quote = '"';
my $sep = ',';

my @peers = ('194.116.23.2:6996');

my %counts_all = ();
my %counts_contacting = ();
my %counts_donated = ();
my %counts_idle = ();
my %counts_inuse = ();
my %counts_owner = ();


# Print header
print	$quote, 'Day', $quote,
	$sep, $quote, '# GuMs', $quote,
	$sep, $quote, '# GuMs contacting', $quote,
	$sep, $quote, '# GuMs donated', $quote,
	$sep, $quote, '# GuMs idle', $quote,
	$sep, $quote, '# GuMs in use', $quote,
	$sep, $quote, '# GuMs owned', $quote,
	"\n";
# Print body
for my $dbfile (sort(@dbfiles))
{
	$dbfile =~ m/^sharegrid_stats\.(\d{4})(\d{2})(\d{2})\.db$/oi;

	my $date_yyyy = $1;
	my $date_mm = $2;
	my $date_dd = $3;

	my $dbh = DBI->connect("dbi:SQLite:dbname=$dbfile");
	for my $peer (@peers)
	{
		my @count_all = $dbh->selectrow_array("SELECT COUNT(*) FROM og_peer_gum_stats G INNER JOIN og_peer P ON G.peer=P.id WHERE UPPER(P.name)=UPPER(" . $dbh->quote($peer) . ")");
		my @count_contacting = $dbh->selectrow_array("SELECT COUNT(*) FROM og_peer_gum_stats G INNER JOIN og_peer P ON G.peer=P.id WHERE UPPER(P.name)=UPPER(" . $dbh->quote($peer) . ") AND status=$gum_status{CONTACTING}");
		my @count_donated = $dbh->selectrow_array("SELECT COUNT(*) FROM og_peer_gum_stats G INNER JOIN og_peer P ON G.peer=P.id WHERE UPPER(P.name)=UPPER(" . $dbh->quote($peer) . ") AND status=$gum_status{DONATED}");
		my @count_idle = $dbh->selectrow_array("SELECT COUNT(*) FROM og_peer_gum_stats G INNER JOIN og_peer P ON G.peer=P.id WHERE UPPER(P.name)=UPPER(" . $dbh->quote($peer) . ") AND status=$gum_status{IDLE}");
		my @count_inuse = $dbh->selectrow_array("SELECT COUNT(*) FROM og_peer_gum_stats G INNER JOIN og_peer P ON G.peer=P.id WHERE UPPER(P.name)=UPPER(" . $dbh->quote($peer) . ") AND status=$gum_status{IN_USE}");
		my @count_owner = $dbh->selectrow_array("SELECT COUNT(*) FROM og_peer_gum_stats G INNER JOIN og_peer P ON G.peer=P.id WHERE UPPER(P.name)=UPPER(" . $dbh->quote($peer) . ") AND status=$gum_status{OWNER}");
		$dbh->disconnect();
		undef $dbh;

		print	$quote, "$date_yyyy-$date_mm-$date_dd", $quote,
			$sep, $quote, "$peer", $quote,
			$sep, ($count_all[0] || 0),
			$sep, ($count_contacting[0] || 0),
			$sep, ($count_donated[0] || 0),
			$sep, ($count_idle[0] || 0),
			$sep, ($count_inuse[0] || 0),
			$sep, ($count_owner[0] || 0),
			"\n";

		unless (exists($counts_all{$peer}))
		{
			$counts_all{$peer} = 0;
		}
		$counts_all{$peer} += ($count_all[0] || 0);
		unless (exists($counts_contacting{$peer}))
		{
			$counts_contacting{$peer} = 0;
		}
		$counts_contacting{$peer} += ($count_contacting[0] || 0);
		unless (exists($counts_donated{$peer}))
		{
			$counts_donated{$peer} = 0;
		}
		$counts_donated{$peer} += ($count_donated[0] || 0);
		unless (exists($counts_idle{$peer}))
		{
			$counts_idle{$peer} = 0;
		}
		$counts_idle{$peer} += ($count_idle[0] || 0);
		unless (exists($counts_inuse{$peer}))
		{
			$counts_inuse{$peer} = 0;
		}
		$counts_inuse{$peer} += ($count_inuse[0] || 0);
		unless (exists($counts_owner{$peer}))
		{
			$counts_owner{$peer} = 0;
		}
		$counts_owner{$peer} += ($count_owner[0] || 0);
	}
}
$dh->close();
undef $dh;

for my $peer (@peers)
{
	print	$quote, "Overall", $quote,
		$sep, $quote, "$peer", $quote,
		$sep, $counts_all{$peer},
		$sep, $counts_contacting{$peer},
		$sep, $counts_donated{$peer},
		$sep, $counts_idle{$peer},
		$sep, $counts_inuse{$peer},
		$sep, $counts_owner{$peer},
		"\n";
}

__END__
