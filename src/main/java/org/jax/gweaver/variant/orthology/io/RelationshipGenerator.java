package org.jax.gweaver.variant.orthology.io;


/**
 * 
 * This class replaces the shell script using apoc:
 * 
<pre>
#!/usr/bin/env bash

# 'CALL apoc.export.csv.query("MATCH (a:Gene) RETURN a.gene_id AS name", "genes.csv", {});'


# don't forget the semi-colon at the end of the cypher query!

# for the query below, don't worry that it contains double-quotes and it will
# be set inside of the double-quotes in the last line of this script. See:
# https://pubs.opengroup.org/onlinepubs/9699919799/utilities/V3_chap02.html#tag_18_02_03
#read -r -d '' QUERY <<- EOF
#CALL apoc.periodic.iterate(
#"LOAD CSV WITH HEADERS FROM url AS line
#WITH  apoc.coll.partition(collect(line),10000) AS batchesOfLines
#UNWIND batchesOfLines as batch
#RETURN batch",
#"UNWIND {batch} AS user
#MERGE (u:User {Email: user.Email})
#SET u += apoc.map.clean(user,['Email'],null)",
#{batchSize: 1, parallel: true});
#EOF

read -r -d '' QUERY <<- EOF
USING PERIODIC COMMIT 10000 LOAD CSV WITH HEADERS FROM "file://./data/orthologs.csv" as row
MATCH (f:Gene), (t:Gene)
WHERE f.gene_id = row.fromGene
AND t.gene_id = row.toGene
CALL apoc.create.relationship(f, 'IS_ORTHOLOG', {}, t) YIELD rel
REMOVE rel.no0b;

EOF

echo "$QUERY"


cypher-shell -u neo4j -p j4cks0nl4b --address localhost --format plain "$QUERY"
</pre>

 * @author gerrim
 *
 */
public class RelationshipGenerator {

	
	// TODO 
}
