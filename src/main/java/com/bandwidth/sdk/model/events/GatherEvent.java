package com.bandwidth.sdk.model.events;

import org.json.simple.JSONObject;

public class GatherEvent extends EventBase{

	public GatherEvent(JSONObject json) {
		super(json);
		// TODO Auto-generated constructor stub
	}

	public void execute(Visitor visitor) {
		visitor.processEvent(this);
	}

}
