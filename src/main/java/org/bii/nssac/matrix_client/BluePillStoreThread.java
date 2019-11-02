package org.bii.nssac.matrix_client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.logging.Logger;
import org.javatuples.Pair;

/**
 * The Store Thread is responsible for maintaining the overall system state.
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
            Pair<String, JsonArray> code_updates = this.proxy.get_events(storeproc_id);
            String code = code_updates.getValue0();
            if ("EVENTS".equals(code)) {
                JsonArray updates = code_updates.getValue1();
                for (JsonElement update_json: updates) {
                    BluePillUpdate update = gson.fromJson(update_json, BluePillUpdate.class);
                    this.store.handle_update(update);
                }
            } else if ("FLUSH".equals(code)) {
                this.store.flush();
            } else if ("SIMEND".equals(code)) {
                return;
            } else {
                throw new RuntimeException(String.format("Received unknown code: %s", code));
            }
        }
    }
}
