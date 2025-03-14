package app.tuxguitar.community.browser;

import app.tuxguitar.app.tools.browser.base.TGBrowserSettings;

public class TGBrowserSettingsModel {

	public TGBrowserSettingsModel(){
		super();
	}

	public String getTitle() {
		return "TuxGuitar Community";
	}

	public TGBrowserSettings toBrowserSettings() {
		TGBrowserSettings settings = new TGBrowserSettings();
		settings.setTitle(this.getTitle());
		settings.setData(this.getTitle());
		return settings;
	}
}
