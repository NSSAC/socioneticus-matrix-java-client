package org.bii.nssac.matrix_client;

/**
 * The BluePull Update is a a container for updates sent from
 * the agent to the store.
 * 
 * BluePillUpdate implements Comparable to ensure they can be sorted.
 */
public class BluePillUpdate implements Comparable {
    public String order_key;
    public String agent_id;
    public String state;

    public BluePillUpdate(String order_key, String agent_id, String state) {
        this.order_key = order_key;
        this.agent_id = agent_id;
        this.state = state;
    }

    @Override
    public int compareTo(Object t) {
        BluePillUpdate t1 = (BluePillUpdate) t;
        return order_key.compareTo(t1.order_key);
    }
}
