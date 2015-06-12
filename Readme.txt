
*Advanced Computer Networks - ScaleFree Topology Generation*
------------------------------------------------------------
					

A scale-free network is constructed has ultra-small diameter (~ln ln(n)), where n is the network size. Diameter of a network is defined as the largest shortest path length between all pairs of nodes in the network. The link between two nodes in the network corresponds to a TCP connection. Created a scalable way of creating TCP connections between nodes using Barabasi-Albert model.


1. Initially the program starts by creating TCP connections among m0 nodes by reading the config file.
2. Upon arrival of the new node it sends UDP message request to one of the m0(initial) nodes .
3. It obtains the information about node with highest probability and sets up TCP connection by randomly selecting the node with highest probability.
3. As the Preferential attachment mechanism states,the node with highest probability has a higher chance of getting selected among other nodes.
4. Nodes with highest probability are identified by implementing distance vector routing protocol.
5. After running distance vector protocol each node will have the information about shortest path to all other nodes in the network.
6. The Query system is also built for sample querying purposes like fetching the overall routing table, knowing the state of the system.


How to run the project
----------------------
1. Import the project and compile it
2. Run the "TCPServer" file which contains the "main" method, this class is the start of execution of system.

Example
a) Compile and run the program
java TCPServer

b) Enter the arguments in the given order when prompted
Node number,IP address,TCP Port number,UDP Port number  
Example input : 4,net31,1232,5454


