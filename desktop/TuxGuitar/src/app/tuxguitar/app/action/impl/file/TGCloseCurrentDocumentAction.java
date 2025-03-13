package app.tuxguitar.app.action.impl.file;

import java.util.ArrayList;
import java.util.List;

import app.tuxguitar.action.TGActionContext;
import app.tuxguitar.action.TGActionManager;
import app.tuxguitar.app.document.TGDocument;
import app.tuxguitar.app.document.TGDocumentListAttributes;
import app.tuxguitar.app.document.TGDocumentListManager;
import app.tuxguitar.editor.action.TGActionBase;
import app.tuxguitar.util.TGContext;

public class TGCloseCurrentDocumentAction extends TGActionBase {

	public static final String NAME = "action.file.close-current";

	public TGCloseCurrentDocumentAction(TGContext context) {
		super(context, NAME);
	}

	protected void processAction(final TGActionContext context){
		List<TGDocument> documents = new ArrayList<TGDocument>();
		documents.add(TGDocumentListManager.getInstance(this.getContext()).findCurrentDocument());
		context.setAttribute(TGDocumentListAttributes.ATTRIBUTE_DOCUMENTS, documents);

		TGActionManager tgActionManager = TGActionManager.getInstance(getContext());
		tgActionManager.execute(TGCloseDocumentsAction.NAME, context);
	}
}