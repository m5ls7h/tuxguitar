package org.herac.tuxguitar.editor.action.effect;

import org.herac.tuxguitar.action.TGActionContext;
import org.herac.tuxguitar.document.TGDocumentContextAttributes;
import org.herac.tuxguitar.editor.action.TGActionBase;
import org.herac.tuxguitar.song.models.TGNote;
import org.herac.tuxguitar.util.TGContext;
import org.herac.tuxguitar.util.TGNoteRange;

public class TGChangeVibratoNoteAction extends TGActionBase{
	
	public static final String NAME = "action.note.effect.change-vibrato";
	
	public TGChangeVibratoNoteAction(TGContext context) {
		super(context, NAME);
	}
	
	protected void processAction(TGActionContext context){
		TGNoteRange noteRange = context.getAttribute(TGDocumentContextAttributes.ATTRIBUTE_NOTE_RANGE);

		if (noteRange!=null && !noteRange.isEmpty()) {
			boolean newValue = true;
			if (noteRange.getNotes().stream().allMatch(n -> n.getEffect().isVibrato())) {
				newValue = false;
			}
			for (TGNote note : noteRange.getNotes()) {
				getSongManager(context).getMeasureManager().setVibrato(note, newValue);
			}
		}
	}
}
