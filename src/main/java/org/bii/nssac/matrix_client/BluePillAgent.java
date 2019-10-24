package org.bii.nssac.matrix_client;

import java.util.Random;
import java.util.logging.Logger;

/**
 * The BluePill agent is a agent implementation for demonstrating
 * how to write agents for the Matrix v1 ABM System.
 * 
 * The BluePillAgent has three states that it cycles through:
 * rock, paper, and scissors.
 * It doesn't keep track of its state,
 * but uses the store's state instance variable.
 * In the beginning when it chooses one of the states at random.
 */
public class BluePillAgent {
    private static final Logger LOG = Logger.getLogger(BluePillAgent.class.getName());
    
    public String agent_id = null;
    public BluePillStore store = null;
    public Random random = null;
    
    public BluePillAgent(String agent_id, BluePillStore store, Random random) {
        this.agent_id = agent_id;
        this.store = store;
        this.random = random;
    }

    public BluePillUpdate step(int cur_round) {
        String state = store.get_state(agent_id);
        String next_state;
        if (state == null) {
            String[] available_states = {"rock", "paper", "scissors"};
            int random_idx = random.nextInt(available_states.length);
            next_state = available_states[random_idx];
        } else if ("rock".equals(state)) {
            next_state = "paper";
        } else if ("paper".equals(state)) {
            next_state = "scissors";
        } else {
            next_state = "rock";
        }
        
        String order_key = String.format("%s-%d", agent_id, cur_round);
        BluePillUpdate update = new BluePillUpdate(order_key, agent_id, next_state);
        
        LOG.info(String.format("Round %d, Agent %s: %s -> %s", cur_round, agent_id, state, next_state));
        
        return update;
    }
}
