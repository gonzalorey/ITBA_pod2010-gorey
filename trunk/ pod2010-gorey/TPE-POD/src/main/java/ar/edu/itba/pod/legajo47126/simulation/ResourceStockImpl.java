package ar.edu.itba.pod.legajo47126.simulation;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;

import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;

public class ResourceStockImpl implements ResourceStock {
	
	private final Resource resource;
	
	private final AtomicInteger amount = new AtomicInteger();

	public ResourceStockImpl(Resource resource) {
		this.resource = resource;
	}
	
	@Override
	public void add(int size) {
		Preconditions.checkState(size > 0, "The size is lower or equal to zero");
		amount.addAndGet(size);
	}

	@Override
	public int current() {
		return amount.get();
	}

	@Override
	public String name() {
		return resource.name();
	}

	@Override
	public void remove(int size) {
		int newSize = amount.addAndGet(-size);
		if (newSize < 0) {
			amount.addAndGet(size);
			throw new IllegalArgumentException("Attempted to remove more items than available in this stock!");
		}
	}

	@Override
	public Resource resource() {
		return resource;
	}

}
