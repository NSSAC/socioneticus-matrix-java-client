// RPC Proxy class for communicating with the Matrix
package org.bii.nssac.matrix_client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * RPCProxy provides a simple JSONRPC over TCP/IP connection service.
 * 
 * This is how the program connects to the Matrix.
 */
public class RPCProxy
{
    private static final Logger LOG = Logger.getLogger(RPCProxy.class.getName());
    
    // initialize socket and input output streams
    private Socket socket = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    // constructor to put ip address and port
    public RPCProxy(String address, int port)
    {
        // establish a connection
        try
        {
            socket = new Socket(address, port);
            LOG.info(String.format("Connected to %s:%d", address, port));

	    reader = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream(),
                        Charset.forName("US-ASCII")
                        )
                    );
            
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                        socket.getOutputStream(),
                        Charset.forName("US-ASCII")            
                        )
                    );
	}
        catch(UnknownHostException u)
	{
            LOG.severe(u.toString());
            throw new RuntimeException("Error connecting to controller");
	}
        catch(IOException e)
	{
            LOG.severe(e.toString());
            throw new RuntimeException("Error connecting to controller");
	}
    }
    
    
    public void close() throws IOException
    {
        writer.close();
        reader.close();
        socket.close();
    }
    
    public JsonElement call(String method, JsonObject params)
    {
        JsonObject iobj = new JsonObject();
        iobj.addProperty("jsonrpc", "2.0");
        iobj.addProperty("id", UUID.randomUUID().toString());
        iobj.addProperty("method", method);
        iobj.add("params", params);
        
        Gson gson = new Gson();
        String ijson = gson.toJson(iobj);
        ijson = ijson + "\n"; // NOTE: The newline is important
        try {
            writer.write(ijson);
            writer.flush();
        } catch (IOException ex) {
            LOG.severe(ex.toString());
            throw new RuntimeException("Error Sending RPC Request");
        }
                
        String ojson;
        try {
            ojson = reader.readLine();
        } catch (IOException ex) {
            LOG.severe(ex.toString());
            throw new RuntimeException("Error Receiving RPC Response");
        }
        JsonObject oobj = JsonParser.parseString(ojson).getAsJsonObject();
        
        if (!oobj.has("jsonrpc") || !"2.0".equals(oobj.get("jsonrpc").getAsString())) {
            throw new RuntimeException("Invalid RPC Response: " + ojson);
        }
        if (oobj.has("error")) {
            throw new RuntimeException("RPC Exception: " + ojson);
        }
        
        return oobj.get("result");
    }
}
