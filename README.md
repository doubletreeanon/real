# doubletree_real

Double tree real system has the following three modules,

- Server
- Client
- Master


Server
------

Due to hardware restrictions, all the servers are simulated in a single hardware by using different port numbers. Source files related to server are,

- Server.java
- ServerRequestHandler.java
- ServerrequestProvider.java
- UtilFuncs.java
- Stats.java
- Node.java
- Request.java

The servers are simulated using the script.sh

Client
------

Clients are simulated in a single hardware by using different port number for each client. Source files related to clients are,

- Client.java
- ClientFileReceiver.java
- RequestClient.java

Master
------

All the requests from the master are initially received by the master and it forwards the request to the randomly chosen leaf node of the tree for that particular file.

- Master.java
- MasterRequestHandler.java
- UtilFuncs.java
- Request.java
- RequestClient.java

Master requires files to be stored in the cluster and the list of server ID. For server list, please refer servers.txt

How to Run
----------

- Copy the files related to master, server and client to different machines. Update the code accordingly for the name of machines.
- Copy all the files to the master machine.
- Start server simulation by executing the script "sh script.sh".
- Start the master "java Master". It stores the files in the cluster and will be ready to receive the request.
- Start the client "java Client". 
- Results will be displayed in the master and client after certain tick values.


idGen.py is used to generate the list of server IDs.
fileCopy.sh is used to generate the required number of files with name starting from 1.txt to the required number.