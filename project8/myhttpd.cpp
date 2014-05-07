
const char * usage =
"                                                               \n"
"myhttpd:                                               		\n"
"                                                               \n"
"Simple http server for cs 252 lab 5.							\n"
"																\n"
"The format of the command should be: 							\n"
"	myhttpd [-f|-t|-p] [<port>]									\n"
"																\n"
"																\n"
"	-f		Create a new process for each request				\n"
"																\n"
"	-t		Create a new thread for each request				\n"
"																\n"
"	-p		Draw from a pool of threads for each request		\n"
"																\n"
"	-h		Display help message and exit						\n"
"			--help												\n";




#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <time.h>
#include <signal.h>
#include <sys/wait.h>
#include <pthread.h>
#include <dirent.h>
#include <sys/stat.h>





int QueueLength = 5;

int docTypeCount = 0;
char* docExt[100];
char* docType[100];

// Lock for thread pool
pthread_mutex_t lock;

const char* error404html = "<body style=\"background-color:rgb(29,29,29);\"><center><FONT FACE=\"Courier New\" COLOR=\"white\"><h2>404 - File Not Found</h2><p>Opps... Looks like I don't have that</p></FONT><img src=\"http://fc01.deviantart.net/fs70/f/2010/291/f/2/404_page_by_prax_08-d3107j5.png\" align=\"middle\" alt=\"The Cake Is 404\"></center></body>";

const char* httpVersion = "HTTP/1.1";

#define SERVER_DEFAULT 0
#define SERVER_F 1
#define SERVER_T 2
#define SERVER_P 3

#define PATH_DEFAULT 0
#define PATH_ICONS 1
#define PATH_HTDOCS 2
#define PATH_CGIBIN 3
#define PATH_NONE 4
#define PATH_FORBID 5

#define REQ_ERROR 0
#define REQ_OPTIONS 1
#define REQ_GET 2
#define REQ_HEAD 3
#define REQ_POST 4
#define REQ_PUT 5
#define REQ_DELETE 6
#define REQ_TRACE 7
#define REQ_CONNECT 8

#define REQUEST_BUFFER_SIZE 5000

struct request
{
	char   buffer[REQUEST_BUFFER_SIZE];
	int    bufferSize;
	int    recSize;
	int    requestType;
	char*  uri;
	char*  options;
	int    option_count;
	char*  body;
};







// Processes request
void processRequest( int socket );

// Read in the request string
void readRequest(int fd, request* dest);

// Get real file path from request string
int getFilePath(char* dest, char* requestString);

// Get extension off of a path in all lowercase
void getExtension(char* dest, char* path);

// Check if the start of a string is equal to b
bool checkStartEqual(char* a, const char* b);

// Check if the end of a string is equal to b
bool checkEndEqual(char* a, const char* b);

// Gets the document type from the extension
char* getDocType(char* ext);

// Register a document type with a specific extension
void registerDocType(const char* ext, const char* type);

// Register all the document types
void genDocTypes();

// Returns a lowercase version of c
char toLower(char c);

// Construct a response header for a specific file
void constructResponseHeader(char* dest, char* path);

// Determin if a string is numeric
bool isNumeric(char* c);

// Default Server Type
void serverDefault(int masterSocket);

// Server Type F
void serverF(int masterSocket);

// Server Type T
void serverT(int masterSocket);

// Server Type P
void serverP(int masterSocket);

// Kill zombies with a crowbar
void killzombie( int sig );

// Version of ProcessRequest that is ment to be used as a thread
void processRequestThread(int socket);

// Thread slaves for the thread pool server
void poolSlave(int masterSocket);

// Construct a response header for cgi-bin
void constructCgiBinHeader(char* dest);

// Execute a cgi-bin script
bool runCgiBin(char* path, int socket);

// Send a 404 signal to client
void send404(int socket);

// Check if a file exists
bool file_exists(char* filename);

// Get the type of a request and verify that it is a request 
int getRequestType(char* req);

// Returns a pointer to the begin of the first occurence of match, or null
char* strMatch(char* src, char* match);

// Process Get Request
void processGet(int fd, request rec);

// Process Post Request
void processPost(int fd, request rec);

// Expands a path
bool expandPath(char* dest, char* path);

// Sends an index of a directory to the client
void sendIndex(char* path, int fd, request req);

// Construct a response header for a generated index
void constructIndexHeader(char* dest, char* path);











				/*******
				* Main *
				*******/
int main( int argc, char ** argv )
{
	// Zombie Catcher
	struct sigaction signalAction; 
	signalAction.sa_handler = killzombie; 
	sigemptyset(&signalAction.sa_mask); 
	signalAction.sa_flags = SA_RESTART; 
	 
	int error = sigaction(SIGCHLD, &signalAction, NULL ); 
	if ( error ) 
	{ 
		perror( "sigaction" ); 
		exit( -1 ); 
	} 

	
	// Generate docType list
	genDocTypes();
	
	int port = 5000;
	int serverType = SERVER_DEFAULT; 

	
	// Process args
	for(int i = 1; i < argc; i++)
	{
		if(isNumeric(argv[i])) 
		{
			port = atoi(argv[i]);
		}
		else if(strcmp(argv[i],"-f") == 0)
		{
			serverType = SERVER_F;
		}
		else if(strcmp(argv[i],"-t") == 0)
		{
			serverType = SERVER_T;
		}
		else if(strcmp(argv[i],"-p") == 0)
		{
			serverType = SERVER_P;
		}
		else if(strcmp(argv[i],"-h") == 0 || strcmp(argv[i],"--help") == 0) // help 
		{
			puts(usage); // print help and exit
			exit(0);
		}
	}
	
	
	


	// Set the IP address and port for this server
	struct sockaddr_in serverIPAddress; 
	memset( &serverIPAddress, 0, sizeof(serverIPAddress) );
	serverIPAddress.sin_family = AF_INET;
	serverIPAddress.sin_addr.s_addr = INADDR_ANY;
	serverIPAddress.sin_port = htons((u_short) port);

	// Allocate a socket
	int masterSocket =  socket(PF_INET, SOCK_STREAM, 0);
	if ( masterSocket < 0) 
	{
		perror("socket");
		exit( -1 );
	}

	// Set socket options to reuse port. Otherwise we will
	// have to wait about 2 minutes before reusing the sae port number
	int optval = 1; 
	error = setsockopt(masterSocket, SOL_SOCKET, SO_REUSEADDR, (char *) &optval, sizeof( int ) );
	if ( error ) 
	{
		perror("setsockopt");
		exit( -1 );
	}
	
	// Bind the socket to the IP address and port
	error = bind( masterSocket, (struct sockaddr *)&serverIPAddress, sizeof(serverIPAddress) );
	if ( error ) 
	{
		perror("bind");
		exit( -1 );
	}

	// Put socket in listening mode and set the 
	// size of the queue of unprocessed connections
	error = listen( masterSocket, QueueLength);
	if ( error ) 
	{
		perror("listen");
		exit( -1 );
	}
	
	
	
	
	
	
	// Start Server
	printf("\nStarting Server\n");
	printf("Port = %d\n", port);
	printf("Server Type = ");
	if(serverType == SERVER_DEFAULT)
	{
		// Iterative Server
		printf("SERVER_DEFAULT\n");
		serverDefault(masterSocket);
	}
	else if(serverType == SERVER_F)
	{
		// Create a new process for each request
		printf("SERVER_F\n");
		serverF(masterSocket);
	}
	else if(serverType == SERVER_T)
	{
		// Create a new thread for each request
		printf("SERVER_T\n");
		serverT(masterSocket);
	}
	else if(serverType == SERVER_P)
	{
		// Pool of threads
		printf("SERVER_P\n");
		serverP(masterSocket);
	}
}
				/***********
				* End Main *
				***********/


				
				
				
				
				
				
// Default Server Type
void serverDefault(int masterSocket)
{
	while ( 1 ) 
	{

		// Accept incoming connections
		struct sockaddr_in clientIPAddress;
		int alen = sizeof( clientIPAddress );
		int slaveSocket = accept( masterSocket, (struct sockaddr *)&clientIPAddress, (socklen_t*)&alen);

		if ( slaveSocket < 0 ) 
		{
			perror( "accept" );
			continue;
		}

		// Process request
		processRequest( slaveSocket );

		// Close socket
		close( slaveSocket );
	
	}
}

// Server Type F
void serverF(int masterSocket)
{
	while ( 1 ) 
	{

		// Accept incoming connections
		struct sockaddr_in clientIPAddress;
		int alen = sizeof( clientIPAddress );
		int slaveSocket = accept( masterSocket, (struct sockaddr *)&clientIPAddress, (socklen_t*)&alen);

		if ( slaveSocket < 0 ) 
		{
			perror( "accept" );
			continue;
		}

		
		pid_t slave = fork(); 
		if(slave == 0) // Child
		{ 
			// Process request
			processRequest( slaveSocket ); 
			// Close socket
			close(slaveSocket);
			// Exit ok
			exit(EXIT_SUCCESS); 
		} 

		// Close socket
		close( slaveSocket );
	}
}

// Server Type T
void serverT(int masterSocket)
{
	while ( 1 ) 
	{

		// Accept incoming connections
		struct sockaddr_in clientIPAddress;
		int alen = sizeof( clientIPAddress );
		int slaveSocket = accept( masterSocket, (struct sockaddr *)&clientIPAddress, (socklen_t*)&alen);

		if ( slaveSocket < 0 ) 
		{
			perror( "accept" );
			continue;
		}
		
		// Create a thread for the request to be handeled in
		pthread_t thread;
		pthread_attr_t attr;

		pthread_attr_init( &attr );
		pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);

		pthread_create( &thread, &attr, (void * (*)(void *))processRequestThread, (void *)slaveSocket);
	}
}

// Server Type P
void serverP(int masterSocket)
{
	
	if (pthread_mutex_init(&lock, NULL) != 0)
    {
        perror("mutex");
		exit( -1 );
    }
	
	pthread_t tid[5]; 
	for( int i = 0; i < 5; i++ )
	{ 
		// Create a thread for the request to be handeled in
		pthread_attr_t attr;

		pthread_attr_init( &attr );
		pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);
		
		pthread_create(&tid[i], &attr, (void *(*)(void *))poolSlave, (void *)masterSocket); 
	} 
	
	pthread_join(tid[0], NULL); 
}
			



			
// Processes request
void processRequest( int fd )
{

	// Read request from client 
	//char req[1024] = {0};
	
	request req;
	
	readRequest(fd, &req);
	
	//printf("\n-Request-\n");
	//printf("Type:%d\n",req.requestType);

	// Check if it is a proper http request
	if( req.requestType == REQ_ERROR ) 
	{	
		printf("Not a proper request\n");
		printf("Buffer:%s\n",req.buffer);
		return;
	}
	
	
	if(req.requestType == REQ_GET)
	{
		processGet(fd, req);
	}
	else if(req.requestType == REQ_POST)
	{
		processPost(fd, req);
	}
}





void readRequest(int fd, request* dest)
{
	// Typical request
	//
	// GET <sp> <Document Requested> <sp> HTTP/1.0 <crlf> 
	// {<Other Header Information> <crlf>}* 
	// <crlf>
	// [Request-body]
	//
	
	
	
	int reqLength = 0;
	int n;
	unsigned char newChar;
	unsigned char lastChar = 0;
	int crlf_count = 0;
	

	//
	// Read in the request string from client
	//
	while (reqLength != REQUEST_BUFFER_SIZE && ( n = read( fd, &newChar, sizeof(newChar))) > 0 ) 
	{
		if ( lastChar == 13 && newChar == 10 ) 
		{
			crlf_count++;
			if(crlf_count == 2)
			{
				dest->buffer[reqLength] = newChar;
				reqLength++;
				break;
			}
		}
		
		if(lastChar == 10 && newChar != 13) crlf_count = 0;
		
		lastChar = newChar;
		dest->buffer[reqLength] = newChar;
		reqLength++;
	}
	
	dest->buffer[reqLength] = 0;
	if(reqLength == 0 || reqLength >= REQUEST_BUFFER_SIZE)
	{
		dest->requestType = REQ_ERROR;
		return;
	}
	
	
	
	
	//
	// Get request type
	//
	char* firstCrlf = strMatch(dest->buffer,"\015\012");
	firstCrlf[0] = 0;
	firstCrlf++;
	firstCrlf[0] = 0;
	firstCrlf++;
	
	int reqType = getRequestType(dest->buffer);
	dest->requestType = reqType;

	if(reqType == REQ_ERROR)
		return;
	
		
	
	//
	// Get the uri alone
	//
	char* uri = strchr(dest->buffer,' ');
	uri[0] = 0;
	uri++;
	
	char* ver = strchr(uri,' ');
	ver[0] = 0;
	ver++;
	
	dest->uri = uri;
	
	

	//
	// Get options
	// Count up the options and check for Content-Length
	//
	dest->options = firstCrlf;
	dest->option_count = 0;
	int bodyLen = 0;
	char* curOption = firstCrlf;
	
	while(!checkStartEqual(curOption,"\015\012")) // End is when we hit two crlf's in a row
	{
		if(checkStartEqual(curOption,"Content-Length: ")) // Found body length
		{
			// printf("FoundContentLen\n");
			// Parse out body len
			char numBuf[20] = {0};
			int numBufLen = 0;
			for(int i = 16; numBufLen < 19; i++)
			{
				if(curOption[i] < '0' || curOption[i] > '9') break;
				numBuf[numBufLen] = curOption[i];
				numBufLen++;
			}
			
			numBuf[numBufLen] = 0;
			bodyLen = atoi(numBuf);
		}
		
		dest->option_count++;
		curOption = strMatch(curOption,"\015\012") + 2;
	}
	
	curOption[0] = 0;
	
	
	
	//
	// Read body from client if we recived a Content-Length greater that 0
	// Will only read request body if a Content-Length is found, other wise it is assumed that it does not exist 
	//
	dest->body = &(dest->buffer[reqLength]);
	while (bodyLen != 0 && reqLength != REQUEST_BUFFER_SIZE && (( n = read( fd, &newChar, sizeof(newChar))) > 0)) 
	{
		dest->buffer[reqLength] = newChar;
		reqLength++;
		bodyLen--;
	}
	
	dest->buffer[reqLength] = 0;
	dest->recSize = reqLength;
	
	return;
}







int getFilePath(char* dest, char* path)
{
	
	char uri[1000];
	if(!expandPath(uri, path))
		return PATH_FORBID;
		
		

	dest = getcwd(dest,(size_t)256); 
	if(checkStartEqual(uri,"/icons"))
	{
		// cwd+”http-root-dir/”+docpath 
		strcat(dest, "/http-root-dir/");
		strcat(dest, uri);
		return PATH_ICONS;
	}
	else if(checkStartEqual(uri,"/htdocs”"))
	{
		// cwd+”http-root-dir/”+docpath 
		strcat(dest, "/http-root-dir/");
		strcat(dest, uri);
		return PATH_HTDOCS;
	}
	else if(checkStartEqual(uri,"/cgi-bin"))
	{
		// cwd+”http-root-dir/”+docpath 
		strcat(dest, "/http-root-dir/");
		strcat(dest, uri);
		return PATH_CGIBIN;
	}
	else if(strcmp(uri,"/") == 0)
	{
		// cwd+”http-root-dir/htdocs/index.html”
		strcat(dest, "/http-root-dir/htdocs/index.html");
		return PATH_DEFAULT;
	}
	else
	{
		// cwd+”http-root-dir/htdocs”+docpath 
		strcat(dest, "/http-root-dir/htdocs");
		strcat(dest, uri);
		return PATH_NONE;
	}
}

void getExtension(char* dest, char* path)
{
	int count = 0;
	int len = strlen(path);
	for( int i = len; i >= 0; i-- )
	{
		if(path[i] == '.') break; // found the end of the extension
		if(path[i] == '/') // there is no extension
		{
			count = 0;
		}
		
		count++;
	}
	
	for( int i = 0; i < count; i++)
	{
		dest[i] = toLower(path[len - count + 1 + i]);
	}
	
	dest[count] = 0;
}

bool checkStartEqual(char* a, const char* b)
{
	int len = strlen(b);
	for(int i = 0; i < len; i++)
	{
		if(a[i] != b[i]) return false;
	}
	return true;
}

bool checkEndEqual(char* a, const char* b)
{
	int lena = strlen(a);
	int lenb = strlen(b);
	for(int i = 0; i < lenb; i++)
	{
		if(a[lena - lenb + i] != b[i]) return false;
	}
	return true;
}


void registerDocType(const char* ext, const char* type)
{
	/*
	int docTypeCount = 0;
	char* docExt[100];
	char* docType[100];
	*/
	
	docExt[docTypeCount] = (char*)ext;
	docType[docTypeCount] = (char*)type;
	docTypeCount++;
}

void genDocTypes()
{
	registerDocType("html"	, "text/html");
	registerDocType("txt"	, "text/plain");
	registerDocType("gif"	, "image/gif");
}

char* getDocType(char* ext)
{
	for(int i = 0; i < docTypeCount; i++)
	{
		if(strcmp(docExt[i], ext) == 0) return docType[i];
	}
	return (char*)"text/plain"; // defult doc type...  dont know what else to give it... 
}

char toLower(char c)
{
	if(c >= 'A' && c <= 'Z') // is an uppercase
	{
		c = c - 'A' + 'a';
	}
	return c;
}

void constructResponseHeader(char* dest, char* path)
{
	// Typical response
	//
	// HTTP/1.1 <sp> 200 <sp> Document <sp> follows <crlf> 
	// Server: <sp> <Server-Type> <crlf> 
	// Content-type: <sp> <Document-Type> <crlf> 
	// {<Other Header Information> <crlf>}* 
	// <crlf> 
	// <Document Data>
	//
	
	if(path == NULL)
	{
		dest[0] = 0;
		strcat(dest, "HTTP/1.1 404 File Not Found\r\n");
		strcat(dest, "Server: CS-252-lab5\r\n");
		strcat(dest, "Content-type: ");
		strcat(dest, getDocType((char*)"html"));
		strcat(dest, "\r\n\r\n");
		return;
	}
	
	char ext[20];
	getExtension(ext,path);
	dest[0] = 0;
	strcat(dest, "HTTP/1.1 200 Document follows\r\n");
	strcat(dest, "Server: CS-252-lab5\r\n");
	strcat(dest, "Content-type: ");
	strcat(dest, getDocType(ext));
	strcat(dest, "\r\n\r\n");
}

bool isNumeric(char* c)
{
	for(int i = 0; c[i] != 0; i++)
	{
		if(!(c[i] >= '0' && c[i] <= '9')) return false;
	}
	return true;
}

void killzombie( int sig )
{
	int status;
	while(waitpid(-1, &status, 0) > 0){};
}

void processRequestThread(int socket)
{ 
	processRequest(socket); 
	close(socket); 
} 

void poolSlave(int masterSocket)
{ 
	while ( 1 ) 
	{
		// Accept incoming connections
		struct sockaddr_in clientIPAddress;
		int alen = sizeof( clientIPAddress );
		
		pthread_mutex_lock(&lock);
		int slaveSocket = accept( masterSocket, (struct sockaddr *)&clientIPAddress, (socklen_t*)&alen);
		pthread_mutex_unlock(&lock);
		
		if ( slaveSocket < 0 ) 
		{
			perror( "accept" );
			continue;
		}

		// Process request
		processRequest( slaveSocket );

		// Close socket
		close( slaveSocket );
	}
} 

void constructCgiBinHeader(char* dest)
{
	dest[0] = 0;
	strcat(dest, "HTTP/1.1 200 Document follows\r\n");
	strcat(dest, "Server: CS-252-lab5\r\n");
}

void send404(int socket)
{
	// file does not exist
	char responseHeader[1000] = {0};
	printf("404-file not found\n");
	
	
	constructResponseHeader(responseHeader,NULL);
	// Write 404 file header out
	write(socket,responseHeader,strlen(responseHeader));
	// Write 404 html out
	write(socket,error404html,strlen(error404html));
}

bool runCgiBin(char* path, int socket)
{
	char scriptPath[1000] = {0};
	char scriptArgs[1000] = {0};
	
	strcpy(scriptPath, path);
	char* argStarter = strchr(scriptPath,'?');
	if(argStarter != NULL)
	{
		argStarter[0] = 0;
		argStarter++;
		strcpy(scriptArgs,argStarter);
	}
	
	
	// Return false if there is no script to run
	if(!file_exists(scriptPath)) 
	{
		return false;
	}
	
		
	// Open new process to execute script in
	pid_t slave = fork(); 
	if(slave == 0) // Child
	{ 
		char buffer[100] = {0};
		strcpy(buffer, "QUERY_STRING=");
		strcat(buffer, scriptArgs);
		
		// Generate Header and Send
		char responseHeader[1000] = {0};
		constructCgiBinHeader(responseHeader);
		write(socket,responseHeader,strlen(responseHeader));
		
		// Set Enviroment Vars
		putenv((char*)"REQUEST_METHOD=GET");
		putenv(buffer);
		
		// Forward output to client
		dup2(socket, 1);
		
		// Execute Script
		char* argv[2];
		argv[0] = scriptPath;
		argv[1] = 0;
		execv(scriptPath, argv); 
		exit(0); // Close child if execv faild
	}
	
	
	return true;
}

bool file_exists(char * filename)
{
    if (FILE * file = fopen(filename, "r"))
    {
        fclose(file);
        return true;
    }
    return false;
}

int getRequestType(char* req)
{
	char buf[1000] = {0};
	strcpy(buf,req);
	
	char* uri = strchr(buf,' ');
	uri[0] = 0;
	uri++;
	
	char* ver = strchr(uri,' ');
	ver[0] = 0;
	ver++;
	
	if(strcmp(ver, httpVersion) != 0)
		return REQ_ERROR;
		
	int type = REQ_ERROR;
	
	if(strcmp(buf,"GET") == 0) // First because it is the most common
	{
		type = REQ_GET;
	}
	else if(strcmp(buf,"OPTIONS") == 0)
	{
		type = REQ_OPTIONS;
	}
    else if(strcmp(buf,"HEAD") == 0)
	{
		type = REQ_HEAD;
	}
    else if(strcmp(buf,"POST") == 0)
	{
		type = REQ_POST;
	}
    else if(strcmp(buf,"PUT") == 0)
	{
		type = REQ_PUT;
	}
    else if(strcmp(buf,"DELETE") == 0)
	{
		type = REQ_DELETE;
	}
    else if(strcmp(buf,"TRACE") == 0)
	{
		type = REQ_TRACE;
	}
    else if(strcmp(buf,"CONNECT") == 0)
	{
		type = REQ_CONNECT;
	}
	
	return type;
}

char* strMatch(char* src, char* match)
{
	int lenb = strlen(match);
	
	for(int i = 0; src[i] != 0; i++)
	{
		int j;
		for( j = 0; j < lenb; j++)
		{
			if(src[i+j] != match[j]) break;
		}
		
		if(j == lenb) return &src[i];
	}
	return NULL;
}

void processGet(int fd, request req)
{
	//printf("URI:%s\n",req.uri);
	char filePath[1000] = {0};
	int pathType = getFilePath(filePath, req.uri);
	
	
	if(pathType == PATH_FORBID)
	{
		send404(fd);
		return;
	}
	
	struct stat s;
	if( stat(filePath,&s) == 0 )
	{
		if( s.st_mode & S_IFDIR )
		{
			// it's a directory
			sendIndex(filePath, fd, req);
			return;
		}
	}
	
	
	
	// CGI-BIN
	if(pathType == PATH_CGIBIN)
	{
		if(!runCgiBin(filePath, fd))
		{
			// cgibin-404
			send404(fd);
			return;
		}
		else
		{
			return;
		}
	}
	
	
	// Generate a response header
	char responseHeader[1000] = {0};
	constructResponseHeader(responseHeader, filePath);
	
	
	
	// 404 ERROR
	FILE* in = fopen(filePath,"r");
	if(in == NULL)
	{
		send404(fd);
		return;
	}
	
	
	
	// Write file header out
	write(fd,responseHeader,strlen(responseHeader));
	
	// Write the file out
	unsigned char buf[100];
	while(feof(in) == 0)
	{
		size_t count = fread((void*)buf, sizeof(unsigned char), 100, in);
		write(fd, buf, count);
	}
	
	fclose(in);
}

void processPost(int fd, request req)
{
	printf("URI:%s\n",req.uri);
	char filePath[1000] = {0};
	int pathType = getFilePath(filePath, req.uri);

	if(pathType != PATH_CGIBIN) // Post is for scripts so it does not make sense to allow it to run on other files
		return;
		
	if(pathType == PATH_FORBID)
		return;
		
	char scriptArgs[1000] = {0};

	if(req.body != NULL)
	{
		strcpy(scriptArgs,req.body);
	}
	
	
	// Return if there is no script to run
	if(!file_exists(filePath)) 
	{
		return;
	}
	
		
	// Open new process to execute script in
	pid_t slave = fork(); 
	if(slave == 0) // Child
	{ 
		char buffer[100] = {0};
		strcpy(buffer, "QUERY_STRING=");
		strcat(buffer, scriptArgs);
		
		// Generate Header and Send
		char responseHeader[1000] = {0};
		 (responseHeader);
		write(fd,responseHeader,strlen(responseHeader));
		
		// Set Enviroment Vars
		putenv((char*)"REQUEST_METHOD=GET");
		putenv(buffer);
		
		// Forward output to client
		dup2(fd, 1);
		
		// Execute Script
		char* argv[2];
		argv[0] = filePath;
		argv[1] = 0;
		execv(filePath, argv); 
		exit(0); // Close child if execv faild
	}
	
}

bool expandPath(char* dest, char* path)
{
	strcpy(dest,path);
	int len = strlen(dest);
	int slashCount = 0;
	
	for(int i = 0; i < len; i++)
	{
		if(dest[i] == '?') break;
		if(dest[i] == '/' && dest[i + 1] == '.' && dest[i + 2] == '.' && (dest[i + 3] == '/' || dest[i + 3] == 0 || dest[i + 3] == '?') )
		{
			char* fwrd = &dest[i + 3];
			char* back = NULL;
			for(int j = i - 1; j >= 0; j--)
			{
				if(dest[j] == '/')
				{
					back = &dest[j];
					i = j - 1;
					break;
				}
			}
			
			if(back == NULL) return false;
			
			back[0] = 0;
			strcat(back,fwrd);
		}
	}
	

	return true;
}

void sendIndex(char* path, int fd, request req)
{
	char uri[1000] = {0};
	strcpy(uri,req.uri);
	if(uri[strlen(uri) - 1] != '/') strcat(uri, "/");

	// Generate a response header
	char responseHeader[1000] = {0};
	constructIndexHeader(responseHeader, path);
	
	// Write file header out
	write(fd,responseHeader,strlen(responseHeader));
	
	char buffer[5000] = {0};
	strcat(buffer,"<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\"><html><head><title>Index</title></head><body><h1>Index</h1><h4>");
	strcat(buffer, uri);
	strcat(buffer,"</h4><hr><ul>");
	
	strcat(buffer,"<li><A HREF=\"");
	strcat(buffer,uri);
	strcat(buffer,"..\"> .. </A>");
	
	DIR *dir;
	struct dirent *ent;
	if ((dir = opendir(path)) != NULL) 
	{
		/* print all the files and directories within directory */
		while ((ent = readdir (dir)) != NULL) 
		{
			if(ent->d_name[0] == '.') continue;
			strcat(buffer,"<li><A HREF=\"");
			strcat(buffer,uri);
			strcat(buffer,ent->d_name);
			strcat(buffer,"\"> ");
			strcat(buffer,ent->d_name);
			strcat(buffer,"</A>");
		}
		closedir (dir);
	} 

    	strcat(buffer,"</ul></body></html>");
	write(fd,buffer,strlen(buffer));

}

void constructIndexHeader(char* dest, char* path)
{
	dest[0] = 0;
	strcat(dest, "HTTP/1.1 200 Document follows\r\n");
	strcat(dest, "Server: CS-252-lab5\r\n");
	strcat(dest, "Content-type: ");
	strcat(dest, getDocType("html"));
	strcat(dest, "\r\n\r\n");
}
