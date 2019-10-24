package org.bii.nssac.matrix_client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

/**
 * An Agent Thread is responsible for producing events on behalf of a specific set of agents.
 */
public class BluePillAgentThread implements Runnable {
    private static final Logger LOG = Logger.getLogger(BluePillAgentThread.class.getName());
    
    public int agentproc_id = -1;
    public RPCProxy proxy = null;
    public Random random = null;
    public BluePillStore store = null;
    public ArrayList<BluePillAgent> agents = null;
    
    BluePillAgentThread(int agentproc_id, int start_agent_id, int num_agents, BluePillStore store, String address, int port) {
        LOG.info(String.format("Creating agent thread: %d", agentproc_id));
                
        this.agentproc_id = agentproc_id;
        this.proxy = new RPCProxy(address, port);
        this.random = new Random();
        
        this.store = store;
        this.agents = new ArrayList<>();
        
        JsonObject params = new JsonObject();
        params.addProperty("agentproc_id", agentproc_id);
        JsonElement response = this.proxy.call("get_agentproc_seed", params);
        int seed = response.getAsInt();
        LOG.info(String.format("Agent thread %d: Got seed: %d", agentproc_id, seed));
        this.random.setSeed(seed);
        
        LOG.info(String.format("Agent thread %d: Creating agents", agentproc_id));
        for (int i = 0; i < num_agents; i++) {
            String agent_id = String.format("agent-%d", start_agent_id + i);
            BluePillAgent agent = new BluePillAgent(agent_id, this.store, this.random);
            this.agents.add(agent);
        }
        LOG.info(String.format("Agent thread %d: Created %d agents", agentproc_id, this.agents.size()));
    }
    
    @Override
    public void run() {
        JsonObject params;
        JsonElement response;
        Gson gson = new Gson();
        
        while (true) {
            params = new JsonObject();
            params.addProperty("agentproc_id", agentproc_id);
            response = proxy.call("can_we_start_yet", params);
            int cur_round = response.getAsJsonObject().get("cur_round").getAsInt();
            if (cur_round == -1) {
                LOG.info(String.format("Agent thread %d: stopping", agentproc_id));
                return;
            }
            LOG.info(String.format("Agent thread %d: starting round %d", agentproc_id, cur_round));
            
            int update_count = 0;
            for (BluePillAgent agent: agents) {
                BluePillUpdate update = agent.step(cur_round);
                JsonElement update_json = gson.toJsonTree(update);
                
                JsonArray updates = new JsonArray();
                updates.add(update_json);
                
                params = new JsonObject();
                params.addProperty("agentproc_id", agentproc_id);      
                params.add("events", updates);
                proxy.call("register_events", params);
                
                update_count += 1;
            }
            LOG.info(String.format("Agent thread %d: produced %d events on round %d", agentproc_id, update_count, cur_round));
        }
    }   
}
