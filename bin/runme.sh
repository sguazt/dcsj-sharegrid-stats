#!/bin/sh

cp="build/classes"
for j in lib/*.jar; do
    cp+=":$j"
done

java	-cp "$cp" \
		it.unipmn.di.dcs.sharegrid.stats.StatsCollectorApp \
		--middleware "ourgrid" \
		--og-port 10009 \
		--db-driver "org.sqlite.JDBC" \
		--db-dsn "sqlite:/tmp/sharegrid_stats.db" \
		--og-peer "193.206.55.107:30800" \
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
