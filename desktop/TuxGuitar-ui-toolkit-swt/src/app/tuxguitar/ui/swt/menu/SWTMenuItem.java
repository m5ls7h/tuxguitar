package app.tuxguitar.ui.swt.menu;

import org.eclipse.swt.widgets.MenuItem;
import app.tuxguitar.ui.menu.UIMenuItem;
import app.tuxguitar.ui.resource.UIImage;
import app.tuxguitar.ui.resource.UIKeyCombination;
import app.tuxguitar.ui.swt.resource.SWTImage;
import app.tuxguitar.ui.swt.widget.SWTEventReceiver;
import app.tuxguitar.util.TGKeyBindFormatter;

public class SWTMenuItem extends SWTEventReceiver<MenuItem> implements UIMenuItem {

	private UIKeyCombination keyCombination;
	private UIImage image;
	private SWTMenu parent;

	public SWTMenuItem(MenuItem item, SWTMenu parent) {
		super(item);

		this.parent = parent;
	}

	public SWTMenu getParent() {
		return this.parent;
	}

	public void dispose() {
		this.getParent().dispose(this);
	}

	public boolean isDisposed() {
		return this.getControl().isDisposed();
	}

	public boolean isEnabled() {
		return this.getControl().isEnabled();
	}

	public void setEnabled(boolean enabled) {
		this.getControl().setEnabled(enabled);
	}

	public String getText() {
		return this.getControl().getText();
	}

	public void setText(String text) {
		String textWithAccelerator = text;
		if( this.getKeyCombination() != null ) {
			String accelerator = TGKeyBindFormatter.getInstance().format(this.getKeyCombination().getKeyStrings());
			textWithAccelerator += "\t" + accelerator;
		}
		this.getControl().setText(textWithAccelerator);
	}

	public UIKeyCombination getKeyCombination() {
		return keyCombination;
	}

	public void setKeyCombination(UIKeyCombination keyCombination) {
		if( this.keyCombination == null || !this.keyCombination.equals(keyCombination)) {
			this.keyCombination = keyCombination;

			String text = this.getText();
			if( text != null && text.length() > 0 ) {
				this.setText(text);
			}
		}
	}

	public UIImage getImage() {
		return this.image;
	}

	public void setImage(UIImage image) {
		this.image = image;
		this.getControl().setImage(this.image != null ? ((SWTImage) this.image).getHandle() : null);
	}
}
