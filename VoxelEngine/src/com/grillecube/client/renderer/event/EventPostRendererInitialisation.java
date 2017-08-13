package com.grillecube.client.renderer.event;

import com.grillecube.client.renderer.MainRenderer;

/** an event which is called after the MainRenderer being initialized */
public class EventPostRendererInitialisation extends EventRender {

	public EventPostRendererInitialisation(MainRenderer renderer) {
		super(renderer);
	}
}