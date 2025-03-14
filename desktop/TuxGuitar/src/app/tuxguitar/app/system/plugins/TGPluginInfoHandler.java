package app.tuxguitar.app.system.plugins;

import app.tuxguitar.app.system.properties.TGResourcePropertiesReader;
import app.tuxguitar.util.TGContext;

public class TGPluginInfoHandler extends TGResourcePropertiesReader {

	private static final String RESOURCE_PREFIX = "META-INF/";
	private static final String RESOURCE_SUFFIX = ".info";

	public TGPluginInfoHandler(TGContext context) {
		super(context, RESOURCE_PREFIX, RESOURCE_SUFFIX);
	}
}
