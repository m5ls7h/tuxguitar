package app.tuxguitar.community.auth;

import app.tuxguitar.app.TuxGuitar;
import app.tuxguitar.app.ui.TGApplication;
import app.tuxguitar.app.view.main.TGWindow;
import app.tuxguitar.app.view.util.TGDialogUtil;
import app.tuxguitar.community.TGCommunitySingleton;
import app.tuxguitar.community.utils.TGCommunityWeb;
import app.tuxguitar.ui.UIFactory;
import app.tuxguitar.ui.event.UILinkEvent;
import app.tuxguitar.ui.event.UILinkListener;
import app.tuxguitar.ui.event.UISelectionEvent;
import app.tuxguitar.ui.event.UISelectionListener;
import app.tuxguitar.ui.layout.UITableLayout;
import app.tuxguitar.ui.widget.UIButton;
import app.tuxguitar.ui.widget.UILabel;
import app.tuxguitar.ui.widget.UILegendPanel;
import app.tuxguitar.ui.widget.UILinkLabel;
import app.tuxguitar.ui.widget.UIPanel;
import app.tuxguitar.ui.widget.UIPasswordField;
import app.tuxguitar.ui.widget.UITextField;
import app.tuxguitar.ui.widget.UIWindow;
import app.tuxguitar.util.TGContext;
import app.tuxguitar.util.TGException;

public class TGCommunityAuthDialog {

	private TGContext context;
	private TGCommunityAuth auth;
	private Runnable onSuccess;
	private Runnable onCancel;

	public TGCommunityAuthDialog(TGContext context, Runnable onSuccess, Runnable onCancel){
		this.context = context;
		this.onSuccess = onSuccess;
		this.onCancel = onCancel;
		this.auth = TGCommunitySingleton.getInstance(this.context).getAuth();
	}

	public void open() {
		this.open(TGWindow.getInstance(this.context).getWindow());
	}

	public void open(UIWindow parent) {
		final UIFactory uiFactory = TGApplication.getInstance(this.context).getFactory();
		final UITableLayout dialogLayout = new UITableLayout();
		final UIWindow dialog = uiFactory.createWindow(parent, true, false);

		dialog.setLayout(dialogLayout);
		dialog.setImage(TuxGuitar.getInstance().getIconManager().getAppIcon());
		dialog.setText(TuxGuitar.getProperty("tuxguitar-community.auth-dialog.title"));

		UITableLayout groupLayout = new UITableLayout();
		UILegendPanel group = uiFactory.createLegendPanel(dialog);
		group.setLayout(groupLayout);
		group.setText(TuxGuitar.getProperty("tuxguitar-community.auth-dialog.signin"));
		dialogLayout.set(group, 1, 1, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_FILL, true, true);

		//-------USERNAME------------------------------------
		UILabel usernameLabel = uiFactory.createLabel(group);
		usernameLabel.setText(TuxGuitar.getProperty("tuxguitar-community.auth-dialog.signin.username") + ":");
		groupLayout.set(usernameLabel, 1, 1, UITableLayout.ALIGN_RIGHT, UITableLayout.ALIGN_CENTER, false, false);

		final UITextField usernameText = uiFactory.createTextField(group);
		usernameText.setText( this.auth.getUsername() );
		groupLayout.set(usernameText, 1, 2, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_CENTER, true, false);

		//-------PASSWORD------------------------------------
		UILabel passwordLabel = uiFactory.createLabel(group);
		passwordLabel.setText(TuxGuitar.getProperty("tuxguitar-community.auth-dialog.signin.password") + ":");
		groupLayout.set(passwordLabel, 2, 1, UITableLayout.ALIGN_RIGHT, UITableLayout.ALIGN_CENTER, false, false);

		final UIPasswordField passwordText = uiFactory.createPasswordField(group);
		passwordText.setText( this.auth.getPassword() );
		groupLayout.set(passwordText, 2, 2, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_CENTER, true, false);

		//-------JOIN------------------------------------
		UITableLayout joinLayout = new UITableLayout();
		UILegendPanel join = uiFactory.createLegendPanel(dialog);
		join.setLayout(joinLayout);
		join.setText(TuxGuitar.getProperty("tuxguitar-community.auth-dialog.signup"));
		dialogLayout.set(join, 2, 1, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_FILL, true, true);

		final UILinkLabel joinLink = uiFactory.createLinkLabel(join);
		joinLink.setText(TuxGuitar.getProperty("tuxguitar-community.auth-dialog.signup.tip"));
		joinLink.addLinkListener(new UILinkListener() {
			public void onLinkSelect(final UILinkEvent event) {
				new Thread( new Runnable() {
					public void run() throws TGException {
						TGCommunityWeb.open(getContext(), event.getLink());
					}
				} ).start();
			}
		});
		joinLayout.set(joinLink, 1, 1, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_CENTER, false, false);
		joinLayout.set(joinLink, UITableLayout.PACKED_WIDTH, 320f);

		//------------------BUTTONS--------------------------
		UITableLayout buttonsLayout = new UITableLayout(0f);
		UIPanel buttons = uiFactory.createPanel(dialog, false);
		buttons.setLayout(buttonsLayout);
		dialogLayout.set(buttons, 3, 1, UITableLayout.ALIGN_RIGHT, UITableLayout.ALIGN_FILL, true, true);

		UIButton buttonOK = uiFactory.createButton(buttons);
		buttonOK.setText(TuxGuitar.getProperty("ok"));
		buttonOK.setDefaultButton();
		buttonOK.addSelectionListener(new UISelectionListener() {
			public void onSelect(UISelectionEvent event) {
				update(usernameText.getText(), passwordText.getText());

				onFinish(dialog, TGCommunityAuthDialog.this.onSuccess);
			}
		});
		buttonsLayout.set(buttonOK, 1, 1, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_FILL, true, true, 1, 1, 80f, 25f, null);

		UIButton buttonCancel = uiFactory.createButton(buttons);
		buttonCancel.setText(TuxGuitar.getProperty("cancel"));
		buttonCancel.addSelectionListener(new UISelectionListener() {
			public void onSelect(UISelectionEvent event) {
				onFinish(dialog, TGCommunityAuthDialog.this.onCancel);
			}
		});
		buttonsLayout.set(buttonCancel, 1, 2, UITableLayout.ALIGN_FILL, UITableLayout.ALIGN_FILL, true, true, 1, 1, 80f, 25f, null);
		buttonsLayout.set(buttonCancel, UITableLayout.MARGIN_RIGHT, 0f);

		TGDialogUtil.openDialog(dialog, TGDialogUtil.OPEN_STYLE_CENTER | TGDialogUtil.OPEN_STYLE_PACK);
	}

	public void onFinish(UIWindow dialog, Runnable runnable) {
		dialog.dispose();
		if( runnable != null ) {
			runnable.run();
		}
	}

	public void update( String username, String password){
		this.auth.setUsername(username);
		this.auth.setPassword(password);
	}

	public TGContext getContext() {
		return context;
	}
}
