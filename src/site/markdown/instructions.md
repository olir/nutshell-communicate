## Instructions

__under construction__

### Server 

To create the server daemon, the easiest way is to
 
* create a class that extends SimpleServerDaemon,
* read the application protocol by a method createProtocolReader() 
  (see [Application Protocol](#Application_Protocol),
* create a MessageListener. We add a subclass MyCom that implements it.
* add a bind() method where we add the application protocol and then
call the bind method from the superclass to bind the server daemon 
to a port pair (TCP 10000 and UDP 10001), register the session listener
(the super class is providing a default) and our message listener.

``` java
public class TestServer3 extends SimpleServerDaemon {
	public void bind() throws UnknownHostException {
		addApplicationProtocol(createProtocolReader());
		bind(new InetSocketAddress(InetAddress.getLocalHost(), 10000), this,
				new MyCom());
	}

	class MyCom implements MessageListener {
		@Override
		public void messageReceived(Session s, Message m) {
		}
	}

	private Reader createProtocolReader() {
		return new InputStreamReader(getClass().getResourceAsStream(
				"/de/serviceflow/nutshell/cl/test/myprotocol.xml"));
	}
}
```

The bind method will bind UDP on the next port (10001).

### Client

The client is created in the same fashion. Instead of a bind method a
connect method is required. 

* We connect with TCP so we use port 10001.
* "InetAddress.getLocalHost()" is just for test and should be replaced by the remote host later.
* We also need to select an application protocol by providing it's name ("test3/v1").
(see [Application Protocol](#Application_Protocol)

``` java
public class TestClient3 extends SimpleClient{
	public void connect() throws UnknownHostException, IOException {
		addApplicationProtocol(createProtocolReader());
		connect(NetworkProtocolType.TCP,
				new InetSocketAddress(InetAddress.getLocalHost(), 10000),
				"test3/v1", "".getBytes(), this, new MyCom());
	}
	
	class MyCom implements MessageListener {
		@Override
		public void messageReceived(Session s, Message m) {
		}
	}

	private Reader createProtocolReader() {
		return new InputStreamReader(getClass().getResourceAsStream(
				"/de/serviceflow/nutshell/cl/test/myprotocol.xml"));
	}
}
```

### Application Protocol

The application Protocol is XML file that declares protocol states
and message classes.

* On the top-level element, Protocol, we need to specify some attributes
    * messagepackage: the java package where our Message classes will be located must be specified
	* name: a uri for your protocol, including a version, so the server may handle older versions
* Below we add state elements
	* isInitial: exactly one must be the initial state (isInitial="true")
	* name: useful states may be "initializing" and "running".
* Below each state add message elements
	* classname: each message must refer to a class within the messagepackage (see above)
	* client: if the client is allowed to send the message this must be specified as client="true" 
	* reliable: if protocol is TCP_UDP and the value is set to "true" the message is send via UDP.
	* WARNING: UDP is not reliable - use TCP. 
		* You have no guarantee for order (messages may overtook other)
		* You have no guarantee for delivery (under a little more load most of them are lost!)
		* You may get doubles. You send one, but may get it two times or more.
		* You may have data corruption.
	
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<tns:Protocol xmlns:xsd="http://www.w3.org/2001/XMLSchema"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
	xmlns:tns="http://www.screenflow.de/nutshell/2015/protocol.xsd"
	messagepackage="de.serviceflow.nutshell.cl.test2" name="test3/v1">
	<State name="ReadyForTest" isInitial="true">
			<Message classname="TestRoundStarting" client="true"  reliable="true"/>
			<Message classname="TestPing" client="true"  reliable="true"/>
			<Message classname="TestRoundCompleted" client="true"  reliable="true"/>
			<Message classname="TestAcknowledge" reliable="true"/>
	</State>
	<State name="Terminated" isInitial="false">
	</State>
</tns:Protocol>
```

### Messages

The message classes declared in the protocol must extends the superclass Message.

``` java
public class TestRequest extends Message {
	public int factor1;
	public int factor2;
	public final NioObjectContainer expected = new NioObjectContainer();
}
```

___Serialization:___

* Message extends EncodedNioStruct, a class which expects that each field we do not want to transfer is explicitly marked with the annotation @NoTransfer. 
* These field types can be primitives or the corresponding classes or any class that
implements Transferable. 
* EncodedNioStruct is a Transferable, so you can create substructures. It is based on rge kryo library and uses variable length encoding for integers.
* The class NioObjectContainer is a Transferable that can be used to transfer Objects using Kryo.
You can register classes to kryo by calling KryoFactory.register(Class<?> c, int id). 
Please read Kryo documentation for details.
* NVarchar is a String replacement based on ByteBuffer.


