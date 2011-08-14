package jp.horimislime.sample.runkeeper.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point class
 */
public class RunkeeperClone implements EntryPoint {

	public void onModuleLoad() {
		RootPanel.get().add(new MapComponent());
	}
}
