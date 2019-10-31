package org.bii.nssac.matrix_client;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Run the BluePillSimulation
 */
public class App
{
    private static final Logger LOG = Logger.getLogger(App.class.getName());

    public static void main( String[] args )
    {
        // Define the command line options
        Options options = new Options();
        options.addOption("help", false, "print this help message and exit");
        options.addOption("n", true, "number of agent threads: default 1");
        options.addOption("m", true, "number of agents per agent thread: default 1");
        options.addOption("s", true, "starting agent index: default 0");
        options.addOption("h", true, "ip/address of the matrix controller: default localhost");
        options.addOption("p", true, "port of the matrix controller: default 16001");
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            LOG.severe(String.format("Failed to parse arguments: %s", ex.toString()));
            formatter.printHelp("bluepill-client", options);
            return;
        }

        if (cmd.hasOption("help")) {
            formatter.printHelp("bluepill-client", options);
            return;
        }

        // Parse the options
        int num_agent_threads = Integer.parseInt(cmd.getOptionValue("n", "1"));
        int num_agents_per_thread = Integer.parseInt(cmd.getOptionValue("m", "1"));
        int start_agent_index = Integer.parseInt(cmd.getOptionValue("s", "0"));
        String address = cmd.getOptionValue("h", "localhost");
        int port = Integer.parseInt(cmd.getOptionValue("p", "16001"));

        // Chreate the data store
        BluePillStore store = new BluePillStore();

        // Create a thread list
        ArrayList<Thread> threads = new ArrayList<Thread>();

        // Create a the agent threads
        for (int agentproc_id = 0; agentproc_id < num_agent_threads; agentproc_id++) {
            int start_agent_id = start_agent_index + (agentproc_id) * num_agents_per_thread;
            BluePillAgentThread agent_thread = new BluePillAgentThread(agentproc_id, start_agent_id, num_agents_per_thread, store, address, port);
            threads.add(new Thread(agent_thread));
        }

        // Create the store thread
        BluePillStoreThread store_thread = new BluePillStoreThread(0, store, address, port);
        threads.add(new Thread(store_thread));

        // Start all threads
        for (Thread thread: threads) {
            thread.start();
        }

        // Wait for all the threads to finish
        try {
            for (Thread thread: threads) {
                thread.join();
            }
        } catch (InterruptedException ex) {
            LOG.severe(String.format("Interrupted %s", ex.toString()));
            throw new RuntimeException("Interrupted");
        }
    }
}
