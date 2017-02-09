# OGE
Orthographic Graphic Embedder (OGE) v1.0 by Santiago Ontañón (2016)  

This tool computes an orthographic embedding of a plannar input graph. Although the tool was originally designed to be part of a procedural-content generation (PCG) module for a game, it is designed to be usable to find orthographic embeddings for any planar input graphs by using st-numberings to construct a weak-visibility representation, and then PQ-trees to generate the orthographic embeddings. If the input graph is plannar, this procedure guarantees that an orthographic embedding without any edges crossing over any other vertices will be found.  

The algorihtms implemented in this package were taken from the following papers:  
- S. Even and R. E. Tarjan, "Computing an st-numbering", Theoret. Comput. Sci. 2, (1976), 339-344.  
- Tamassia, Roberto, and Ioannis G. Tollis. "Planar Grid Embedding in Linear Time"  
- Tamassia, Roberto, and Ioannis G. Tollis. "A unified approach to visibility representations of planar graphs." Discrete & Computational Geometry 1.4 (1986): 321-341.  

Example usage: java -classpath OGE.jar Main data/graph1 oe1.txt -png:oe1.png  

parameters: input-file output-file options   
- input-file: a file containing the adjacency matrix of a graph  
- output-file: the desired output filename  
- Options:  
  -  -output:[type] : the type of output desired, which can be:  
    - txt (default): a text file with the connectivity matrix, and then a list of vertices, with their mapping to the original vertices, and their coordinates in the orthographic embedding. 
    (more output types might be added in the future)  
  - -png:filename : saves a graphical version of the output as a .png file  
  - -simplify:true/false : defaults to true, applies a filter to try to reduce unnecessary auxiliary vertices.  
  - -optimize:true/false : defaults to true, postprocesses the output to try to make it more compact.  
  - -rs:XXX : specifies the random seed for the random number generator.
  
For example, providing this input graph (included as an example in the "examples" folder as "graph2.txt"), where a graph is represented as the adjacency matrix between each pair of vertices ("0" is no edge and "1" is edge):  

0,1,0,0,0,0,0,0,0,1,1  
1,0,1,1,0,0,0,0,0,0,0  
0,1,0,0,0,0,0,0,0,0,0  
0,1,0,0,1,1,1,0,0,0,0  
0,0,0,1,0,1,0,0,0,0,0  
0,0,0,1,1,0,0,0,0,0,0  
0,0,0,1,0,0,0,1,1,0,0  
0,0,0,0,0,0,1,0,1,0,0  
0,0,0,0,0,0,1,1,0,1,0  
1,0,0,0,0,0,0,0,1,0,1  
1,0,0,0,0,0,0,0,0,1,0  

The program generates an orthographic embedding that looks like this (where the large vertices with numbers in them represent the original vertices of the input graph, and the smaller vertices are auxiliary vertices that had to be added in order to generate the projection, assuming that edges cannot have "elbows"):  

![graph2 png output](https://raw.githubusercontent.com/santiontanon/OGE/master/examples/oe2.png)

The output embedding is saved in a text file that contains:
- First the adjacency matrix of the graph (including any auxiliary vertices that might have had to be added)
- Second a list of vertices of the new (projected) graph, with the index of the original node in the input graph they correspond to (or -1 if the vertex is a new auxiliary vertex), and the x, y coordinates of the vertex in the orthographic embedding.

Other examples are found in the "examples" folder. Precompiled binaries can be downloaded from the releases tab: https://github.com/santiontanon/OGE/releases

