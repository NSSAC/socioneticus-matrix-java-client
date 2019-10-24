package org.bii.nssac.matrix_client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.logging.Logger;

/**
 * The Store Thread is responsible for maintianing the overall system state.
 */
public class BluePillStoreThread implements Runnable {
    private static final Logger LOG = Logger.getLogger(BluePillStoreThread.class.getName());

    public int storeproc_id = -1;
    public BluePillStore store = null;
    public RPCProxy proxy = null;
    
    public BluePillStoreThread(int storeproc_id, BluePillStore store, String address, int port) {
        LOG.info(String.format("Creating store thread: %d", storeproc_id));
        
        this.storeproc_id = storeproc_id;
        this.store = store;
        this.proxy = new RPCProxy(address, port);
    }

    @Override
    public void run() {
        JsonObject params;
        JsonElement response;
        Gson gson = new Gson();
        
        while (true) {
            params = new JsonObject();
            params.addProperty("storeproc_id", storeproc_id);
            response = proxy.call("get_events", params);
            String code = response.getAsJsonObject().get("code").getAsString();
            if ("EVENTS".equals(code)) {
                JsonArray updates = response.getAsJsonObject().get("events").getAsJsonArray();
                LOG.info(String.format("Store thread %d: Received %d updates", storeproc_id, updates.size()));
                for (JsonElement update_json: updates) {
                    BluePillUpdate update = gson.fromJson(update_json, BluePillUpdate.class);
                    this.store.handle_update(update);
                }
            } else if ("FLUSH".equals(code)) {
                LOG.info(String.format("Store thread %d: Received flush", storeproc_id));
                this.store.flush();
            } else if ("SIMEND".equals(code)) {
                LOG.info(String.format("Store thread %d: Received simend", storeproc_id));
                return;
            } else {
                throw new RuntimeException(String.format("Received unknown code: %s", code));
            }
        }
    }
}
