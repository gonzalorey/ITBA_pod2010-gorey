package ar.edu.itba.pod.legajo47126.simul;

import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.units.SimpleProducer;

public class AgentFactory {
	public static Agent CreateAgent(){
		Resource pigIron = new Resource("Mineral", "Pig Iron");
		
		return SimpleProducer.named(String.valueOf(new DateTime().getMillis())).
			producing(2).of(pigIron).every(12, TimeUnit.HOURS).build();
	}
}
