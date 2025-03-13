package app.tuxguitar.ui.widget;

import app.tuxguitar.ui.event.UIModifyListener;

public interface UITextArea extends UIControl {

	String getText();

	void setText(String text);

	void append(String text);

	Integer getTextLimit();

	void setTextLimit(Integer limit);

	void addModifyListener(UIModifyListener listener);

	void removeModifyListener(UIModifyListener listener);
}
