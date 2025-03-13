package app.tuxguitar.android.action.impl.storage;

import app.tuxguitar.action.TGActionContext;
import app.tuxguitar.android.action.TGActionBase;
import app.tuxguitar.android.storage.TGStorageManager;
import app.tuxguitar.util.TGContext;

public class TGSaveDocumentAction extends TGActionBase {

	public static final String NAME = "action.storage.save-document";

	public TGSaveDocumentAction(TGContext context) {
		super(context, NAME);
	}

	protected void processAction(final TGActionContext context) {
		TGStorageManager.getInstance(this.getContext()).saveDocument();
	}
}