package ar.edu.itba.pod.legajo47126.simulation.loadtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ar.edu.itba.pod.legajo47126.simul.ObjectFactoryAlternativeImpl;
import ar.edu.itba.pod.simul.ObjectFactoryAlternative;
import ar.edu.itba.pod.simul.market.Market;

public class ConnectToSingleNode {
	
	// command line parser
	private static CommandLineParser cmdParser;
	private static Options options;
	private static HelpFormatter helpFormatter;
	
	// console command options
	private static Option connect;
	
	public static void main(String[] args) {
		
		cmdParser = new GnuParser();
		options = new Options();
		helpFormatter = new HelpFormatter();
		
		connect = new Option("connect", "Connect to a node");
		connect.setArgs(1);
		connect.setArgName("nodeId");
		
		options.addOption(connect);
		
		helpFormatter.printHelp("-command_name [args]", options);
		
		try {
			ObjectFactoryAlternative ofa = new ObjectFactoryAlternativeImpl(args);
			
			// start the market
			ofa.getMarketManager().start();
			
			// register it
			ofa.getSimulationManager().register(Market.class, ofa.getMarketManager().market());
			
			// reader from the standard input stream
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
			while(true){
				try {
					
					System.out.print(">");
					
					String line = br.readLine();
					String cmdArgs[] = line.split(" ");
					
					// command line reader
					CommandLine cmd;
					cmd = cmdParser.parse(options, cmdArgs);

					if(cmd.hasOption(connect.getOpt())){
						String nodeId = cmd.getOptionValue(connect.getOpt());
						System.out.println("Connecting to [" + nodeId + "]...");
						try{
							ofa.connectToGroup(nodeId);
							System.out.println("Connected to group [" + ofa.getConnectionManager().getClusterAdmimnistration().getGroupId() + "]");

							// loop for ever
							while(true);

						} catch (Exception e) {
							System.out.println("There was an error during the connection to the node " + nodeId);
						}
					} else{
						System.out.println("Wrong command");	
					}
						
				} catch (ParseException e1) {
					System.out.println("Wrong command");
				}
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
}
