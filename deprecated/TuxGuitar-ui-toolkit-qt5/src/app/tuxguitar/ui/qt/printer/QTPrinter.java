package app.tuxguitar.ui.qt.printer;

import app.tuxguitar.ui.printer.UIPrinter;
import app.tuxguitar.ui.printer.UIPrinterJob;
import app.tuxguitar.ui.qt.QTComponent;
import app.tuxguitar.ui.qt.resource.QTResourceFactory;
import app.tuxguitar.ui.resource.UIRectangle;
import app.tuxguitar.ui.resource.UIResourceFactory;
import org.qtjambi.qt.core.QRect;
import org.qtjambi.qt.printsupport.QPrinter;

public class QTPrinter extends QTComponent<QPrinter> implements UIPrinter {

	private UIResourceFactory resourceFactory;

	public QTPrinter(QPrinter control) {
		super(control);

		this.resourceFactory = new QTResourceFactory();
	}

	public UIResourceFactory getResourceFactory() {
		return resourceFactory;
	}

	public Float getDpiScale() {
		return (this.getControl().logicalDpiX() / 100.0f);
	}

	public Float getDpiFontScale() {
		return 1.0f;
	}

	public UIRectangle getBounds() {
		UIRectangle bounds = new UIRectangle();
		QRect pageRect = this.getControl().pageRect();
		bounds.getPosition().setX(pageRect.x());
		bounds.getPosition().setY(pageRect.y());
		bounds.getSize().setWidth(pageRect.width());
		bounds.getSize().setHeight(pageRect.height());

		return bounds;
	}

	public Integer getStartPage() {
		int fromPage = this.getControl().fromPage();
		return (fromPage > 0 ? fromPage : null);
	}

	public Integer getEndPage() {
		int toPage = this.getControl().toPage();
		return (toPage > 0 ? toPage : null);
	}

	public UIPrinterJob createJob(String name) {
		return new QTPrinterJob(this);
	}
}
