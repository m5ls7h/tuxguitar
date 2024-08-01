package org.herac.tuxguitar.ui.qt.widget;

import org.herac.tuxguitar.ui.widget.UIScrollBar;
import io.qt.widgets.QScrollBar;

public class QTScrollBar extends QTAbstractSlider<QScrollBar> implements UIScrollBar {
	
	public QTScrollBar(QScrollBar scrollBar, QTContainer parent) {
		super(scrollBar, parent);
	}
}
