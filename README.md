# Neo4j Procedure Template

Graph representation methods, such as Deepwalk, Node2Vec and GraphSAGE, are suitable for homogeneous graphs. MetaPath2Vec was presented as model with a significant performance on heterogeneous graphs based on node types sequence. Neo4j is a popular graph database management system. Nevertheless, Neo4j misses heterogeneous graph algorithms to encode nodes into float embeddings. I developed a Neo4j plugin that generates node walks within a graph based on MetaPath2Vec approach. Walks generation is an essential phase in computing node embeddings.
