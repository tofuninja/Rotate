CXX = g++ -fPIC
NETLIBS= -lnsl

all: daytime-server myhttpd use-dlopen hello.so

daytime-server : daytime-server.o
	$(CXX) -o $@ $@.o $(NETLIBS)
	
myhttpd : myhttpd.o
	$(CXX) -o $@ $@.o $(NETLIBS) -lpthread

use-dlopen: use-dlopen.o
	$(CXX) -o $@ $@.o $(NETLIBS) -ldl

hello.so: hello.o
	ld -G -o hello.so hello.o

%.o: %.cc
	@echo 'Building $@ from $<'
	$(CXX) -o $@ -c -I. $<

clean:
	rm -f *.o use-dlopen hello.so
	rm -f *.o daytime-server

