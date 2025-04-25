package com.sinergise.geopedia.client.ui.panels;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.NativeAPI;
import com.sinergise.geopedia.client.core.events.ClientSessionEvent;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.geopedia.core.entities.WebLink;
import com.sinergise.geopedia.core.entities.WebLink.LinksCollection;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.SGParagraph;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;
import com.sinergise.gwt.ui.maingui.Breaker;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;

public class LoginWidget extends FlowPanel {


	private static class LoginDialog extends AbstractDialogBox {

		private FlowPanel contentPanel = null;
		private TextBox usernameBox;
		private PasswordTextBox passwordBox;
		
		private SGPushButton actionLogin;
		private NotificationPanel errorHolder;
		private LinksCollection webLinks;
		private LoadingIndicator loadingIndicator = null;
		private Anchor closeButton;
		public LoginDialog(LinksCollection webLinks) {
			super(false, true, true, false);
			this.webLinks = webLinks;
			addStyleName("loginDialog");
			setWidget(getContent());
			setWidth("600px");
		}
		
		
		private FlowPanel getContent() {
			if (contentPanel!=null)
				return contentPanel;
			contentPanel = new FlowPanel();
			closeButton = createCloseButton();
			contentPanel.add(closeButton);
			usernameBox =  new TextBox();
			passwordBox = new PasswordTextBox();
			
			FlowPanel formPanel = new FlowPanel();
			formPanel.setStyleName("loginForm");

			passwordBox.addKeyPressHandler(new KeyPressHandler() {
				
				@Override
				public void onKeyPress(KeyPressEvent event) {
					int charCode = event.getUnicodeCharCode();
					if (charCode==0) {
						int keyCode = event.getNativeEvent().getKeyCode();
						if (keyCode == KeyCodes.KEY_ENTER) {
							onLogin();
							return;
						}
					}
					if (event.getCharCode() == KeyCodes.KEY_ENTER) {
						onLogin();
					}
				}
			});
			errorHolder = new NotificationPanel();
			
			actionLogin = new SGPushButton(Messages.INSTANCE.LoginWidget_LoginAction());
			actionLogin.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					onLogin();
				}
			});
			
			Anchor actionPasswordReset = new Anchor(Messages.INSTANCE.LoginWidget_DialogPasswordReset());
			actionPasswordReset.setStyleName("actionPasswordReset");
			final WebLink resetLink = webLinks.getSystemGroup().get("passwordReset");
			if (resetLink!=null) {
				actionPasswordReset.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.open(resetLink.URL, "_blank", null);
					}
				});
			}
			SGPushButton actionRegister = new SGPushButton(Messages.INSTANCE.loginRegister());
			actionRegister.addStyleName("blue");
			final WebLink registerLink = webLinks.getSystemGroup().get("registerUser");
			if (registerLink!=null) {
				actionRegister.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.open(registerLink.URL, "_blank", null);
					}
				});
			}
			formPanel.add(new Heading.H2(StandardUIConstants.STANDARD_CONSTANTS.buttonLogin()));
			formPanel.add(new Label(Messages.INSTANCE.LoginWidget_Username()));
			formPanel.add(usernameBox);
			formPanel.add(new Label(Messages.INSTANCE.LoginWidget_Password()));
			formPanel.add(passwordBox);
			formPanel.add(new Breaker());
			formPanel.add(errorHolder);
			errorHolder.hide();
			formPanel.add(actionLogin);
			formPanel.add(new Breaker());
			formPanel.add(actionPasswordReset);
			
			
					
			
			FlowPanel registerPanel = new FlowPanel();
			registerPanel.setStyleName("registerForm");
			registerPanel.add(new Image(GeopediaCommonStyle.INSTANCE.loginShadow()));
			registerPanel.add(new Heading.H2(Messages.INSTANCE.loginRegister()));
			registerPanel.add(new SGParagraph(Messages.INSTANCE.loginRegisterText()));
			registerPanel.add(actionRegister);
			registerPanel.add(new Breaker());
			
			contentPanel.add(formPanel);
			contentPanel.add(registerPanel);
			return contentPanel;
		}
		
		public void setFocus() {
			if (usernameBox!=null)
				usernameBox.setFocus(true);
		}
		
		private void loginInProgress(boolean inProgress){
			if (inProgress) {
				actionLogin.setEnabled(false);
				if (loadingIndicator==null) {
					loadingIndicator = new LoadingIndicator(true, true);
					contentPanel.add(loadingIndicator);					
				}
				closeButton.setVisible(false);
				
			} else {
				actionLogin.setEnabled(true);
				if (loadingIndicator!=null) {					
					loadingIndicator.removeFromParent();
					loadingIndicator=null;
				}
				closeButton.setVisible(true);
			}
		}
		private void onLogin() {
			String username = usernameBox.getText();
			String password = passwordBox.getText();
			errorHolder.hide();
			loginInProgress(true);
			ClientSession.login(username, password,new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String result) {
					loginInProgress(false);
					LoginDialog.this.hide();
					loginSucceeded();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					loginInProgress(false);
					errorHolder.showErrorMsg(Messages.INSTANCE.LoginWidget_DialogLoginError());
					errorHolder.setIconBig(false);
				}
			});
		}
		@Override
		public void hide() {
			super.hide();
			loginDialog = null;
		}
		

		@Override
		public void setPopupPositionAndShow(PositionCallback callback) {
			super.setPopupPositionAndShow(callback);
			usernameBox.setFocus(true); //focus on login panel when showing
		}
		
		protected void loginSucceeded() {}
	}
	
	private static LoginDialog loginDialog;
	public static void showLoginDialog(LinksCollection webLinks) {
		if (loginDialog!=null)
			return;
		loginDialog = new LoginDialog(webLinks);
		loginDialog.show();
		loginDialog.setFocus();
	}
	
	private Anchor btnLogin;
	private Anchor btnLogout;
	private HTML userInfoLabel;
	
	private LinksCollection webLinks;
	public LoginWidget(LinksCollection webLinks) {
		this.webLinks=webLinks;
		
		setStyleName("panelLogin");
		btnLogin = new Anchor(StandardUIConstants.STANDARD_CONSTANTS.buttonLogin());
		btnLogin.setStyleName("btnLogin");
		btnLogin.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				showLoginDialog(LoginWidget.this.webLinks);
			}
		});
		
		btnLogout = new Anchor();
		btnLogout.setStyleName("btnLogout");
		btnLogout.setTitle(StandardUIConstants.STANDARD_CONSTANTS.buttonLogout());
		btnLogout.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ClientSession.logout(new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
					
					}

					@Override
					public void onSuccess(Void result) {
						
					}
				});
			}			
		});
		userInfoLabel = new HTML();
		
		updateUI();
		
		ClientSessionEvent.register(ClientGlobals.eventBus, new ClientSessionEvent.Handler() {

			@Override
			public void onAutoLoginEvent(ClientSessionEvent event) {
				
			}

			@Override
			public void onLogin(ClientSessionEvent event) {
				updateUI();
			}

			@Override
			public void onLogout(ClientSessionEvent event) {
				updateUI();
			}
			@Override
			public void onSessionChanged(ClientSessionEvent event) {
			}
			
		});
		
		ClientSessionEvent.register(ClientGlobals.eventBus, new ClientSessionEvent.Handler() {

			@Override
			public void onAutoLoginEvent(final ClientSessionEvent event) {
				if (loginDialog!=null)
					return;
				loginDialog = new LoginDialog(LoginWidget.this.webLinks) {
					@Override
					protected void loginSucceeded() {
						if (event.getParameterString()!=null) {
							NativeAPI.processLink(event.getParameterString());
						}
					}
				};
				loginDialog.show();
				loginDialog.setFocus();
			}

			@Override
			public void onLogin(ClientSessionEvent event) {
			}

			@Override
			public void onLogout(ClientSessionEvent event) {
			}
			@Override
			public void onSessionChanged(ClientSessionEvent event) {
			}
			
		});
	}

	
	public void updateUI() {
		if (ClientSession.isLoggedIn()) {
			clear();
			userInfoLabel.setHTML(Messages.INSTANCE.LoginWidget_Greeting()+"<b>"+ClientSession.getUser()+"</b>");
			add(userInfoLabel);
			add(btnLogout);
		} else {
			clear();
			add(btnLogin);
		}
		
	}

	
}
