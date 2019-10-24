package org.bii.nssac.matrix_client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * The BluePill store is a store implementation for demonstrating
 * how to state stores for the Matrix v1 ABM System.
 * 
 * The actual state is maintained in the HashMap state.
 * The update_cache is used to temporarily cache the updates.
 * The updates are applied to the state when flush is called.
 * To ensure the updates are applied in a consistent order
 * (since they can arrive in arbitrary order)
 * they are first sorted before they are applied.
 */
public class BluePillStore {
    private static final Logger LOG = Logger.getLogger(BluePillAgent.class.getName());
    // private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    
    HashMap<String,String> state = null;
    ArrayList<BluePillUpdate> update_cache = null;

    public BluePillStore() {
        state = new HashMap<>();
        update_cache = new ArrayList<>();
    }
    
    public String get_state(String agent_id) {
        //lock.readLock().lock();
        try {
            return state.get(agent_id);
        } finally {
            //lock.readLock().unlock();
        }
    }
    
    public void handle_update(BluePillUpdate update)
    {
        update_cache.add(update);
    }
    
    public void flush()
    {
        //lock.writeLock().lock();
        try {
            LOG.info(String.format("Sorting %d updates", update_cache.size()));
            Collections.sort(update_cache);
        
            LOG.info(String.format("Applying %d updates", update_cache.size()));
            for(BluePillUpdate update: update_cache) {
                state.put(update.agent_id, update.state);
            }
            
            update_cache.clear();
        } finally {
            //lock.writeLock().unlock();
        }
    }
}