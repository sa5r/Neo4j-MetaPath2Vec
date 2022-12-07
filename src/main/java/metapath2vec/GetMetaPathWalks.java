package metapath2vec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Label;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.ResourceIterator;
// import java.lang.Iterable;
import org.neo4j.graphdb.Entity;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;


/**
 * This is an example showing how you could expose Neo4j's full text indexes as
 * two procedures - one for updating indexes, and one for querying by label and
 * the lucene query language.
 */
public class GetMetaPathWalks {
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    // custom
    @Context
    public Transaction transaction;

    /**
     * This procedure takes a ...
     *
     * @param pattern  The pattern
     * @return  A RelationshipTypes instance with the relations (incoming and outgoing) for a given node.
     */
    @Procedure(value = "metapath2vec.GetMetaPathWalks")
    // @Description("Get the different relationships going in and out of a node.")
    @Description("Generates meta-path walks.")
    public Stream<Walks> getMetaPathWalks(@Name("pattern") String pattern, @Name("walks") Long walks, @Name("length") Long walkLength) {

        Long walksNumber = walks;
        Long walkLengthInt = walkLength;
        String[] patternArr = pattern.split(" ");
        List<String> walkLabels = new LinkedList<String>( Arrays.asList(patternArr) );

        ResourceIterator<Node> startNodes = transaction.findNodes(Label.label( walkLabels.get(0) ));
        walkLabels.remove(0);
        Walks wks = new Walks();
        wks.walks =  new ArrayList<>();
        while (startNodes.hasNext()) {
            Node firstNode = startNodes.next();
            String walksString = firstNode.getProperty("originalId") + " ";
            // this should run in a thread to allow multiple parallel walks
            for(int i = 0;i < walksNumber; i++) {
                walksString += getNodeWalks(firstNode, walkLengthInt - 1, walkLabels, firstNode);
            }
            walksString = walksString.substring(0, walksString.length() - 1);
            wks.walks.add(walksString);
        }

        return Stream.of(wks);
    }

    /**
     * Performs
     *
     * @param Node author
     * @param int walkLength
     */
     private String getNodeWalks(Node node, Long walkLength, List<String> walkLabels, Node previousNode) {
        if(walkLength < 1) {
            return "";
        }
        // if trimming works, this condition is not necessary
        if(walkLabels.size() == 0) {
            return "";
        }

        Iterable<Relationship> relationships = node.getRelationships();
        List<Node> foundNodes = new ArrayList<Node> ();
        // iterate relations
        for (Relationship rel : relationships) {
            Node otherNode = rel.getOtherNode(node);
            // see if matches the first label in the pattern
            // and not the previous node in the walk
            if(otherNode.hasLabel( Label.label( walkLabels.get(0) ) )) {
                if( !previousNode.getProperty("originalId").equals( otherNode.getProperty("originalId") )) 
                    foundNodes.add(otherNode);
            }
        }

        // choose random node
        int size = foundNodes.size();
        // if no node found of that label then it is the end
        // but the walks should be trimmed to the last valid label
        if(size == 0)
            return "";
        // at this stage we found node of the required label
        // so remove that label
        walkLabels.remove(0);

        Random rand = new Random();
        int min = 0;
        int max = size - 1;
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;
        Node chosenNode = foundNodes.get(randomNum);
        return chosenNode.getProperty("originalId") + " " + getNodeWalks(chosenNode, walkLength - 1, walkLabels, node);
    }

    public class Walks {
        public List<String> walks;
    }
}
