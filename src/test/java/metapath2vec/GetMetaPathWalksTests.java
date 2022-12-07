package metapath2vec;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetMetaPathWalksTests {

    private Driver driver;
    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(GetMetaPathWalks.class)
                .build();

        this.driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());
    }

    @AfterAll
    void closeDriver(){
        this.driver.close();
        this.embeddedDatabaseServer.close();
    }

    @AfterEach
    void cleanDb(){
        try(Session session = driver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    @Test
    public void shouldReturnSomething() {
        final String param1 = "INCOMING";
        final String param2 = "OUTGOING";

        try(Session session = driver.session()) {
            //Create our data in the database.
            // session.run(String.format("CREATE (:Person)-[:%s]->(:Movie {id:1})-[:%s]->(:Person)", param1, param2));
            session.run("CREATE (:Author {originalId:'a1'})-[:PUBLISHED]->(:Paper {originalId:'p1'})-[:PUBLISHED_IN]->(:Conference {originalId:'c1'})<-[:PUBLISHED_IN]-(:Paper {originalId:'p2'})<-[:PUBLISHED]-(:Author {originalId:'a2'})");

            //Execute our procedure against it.
            Value v = session.run("CALL metapath2vec.GetMetaPathWalks('Author Paper Conference Paper Author', 1, 5)").single().get(0);
            List<String> walks = v.asList(x -> x.asString());
            String firstWalk = walks.get(0);

            //Get 
            // assertThat(record.get("incoming").asList(x -> x.asString())).containsOnly(expectedIncoming);
            assertThat(firstWalk).isEqualTo("a1 p1 c1 p2 a2");

        }


    }

}
