package com.sinergise.gwt.ui.upload;

import static com.sinergise.common.ui.upload.UploadItem.SESSION_DEFAULT_UPLOAD_TOKEN;
import static com.sinergise.gwt.ui.upload.Uploader.Status.BEFORE_SUBMIT;
import static com.sinergise.gwt.ui.upload.Uploader.Status.FETCHING_TOKEN;
import static com.sinergise.gwt.ui.upload.Uploader.Status.FINISHED;
import static com.sinergise.gwt.ui.upload.Uploader.Status.SUBMITTED_POSTPROC_CALLED;
import static com.sinergise.gwt.ui.upload.Uploader.Status.SUBMITTED_EXTERNAL_POSTPROC_CALLED;
import static com.sinergise.gwt.ui.upload.Uploader.Status.SUBMITTING;
import static com.sinergise.gwt.ui.upload.Uploader.Status.SUBMITTING_GOTINFO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Hidden;
import com.sinergise.common.ui.upload.IUploadItemStatusService;
import com.sinergise.common.ui.upload.IUploadItemStatusServiceAsync;
import com.sinergise.common.ui.upload.UploadItem;
import com.sinergise.common.util.string.StringUtil;

public class Uploader extends Timer implements SubmitCompleteHandler, HasHandlers, SubmitHandler {
	public static enum Status {
		CLEAN, FETCHING_TOKEN, BEFORE_SUBMIT, SUBMITTING, SUBMITTING_GOTINFO, SUBMITTED_POSTPROC_CALLED, SUBMITTED_EXTERNAL_POSTPROC_CALLED, FINISHED;

		public boolean isBefore(Status other) {
			return ordinal() < other.ordinal();
		}
		
		public boolean isAfter(Status other) {
			return ordinal() > other.ordinal();
		}
	}
	
	protected static final int NUM_TRIES = 3;
	
	UploadItem                    uploadingItem;
	FormPanel                     targetForm;
	IUploadItemStatusServiceAsync service;
	HandlerManager                handlerManager;
	Hidden                        hiddenField;
	String                        originalFormAction;
	
	int             firstDelayMilis = 500;  //< ms to wait before sending first status request
	int             stepDelayMilis  = 1500;  //< ms to wait before sending each subsequent request
	int[]           errorDelayMilis = {1500, 1500, 5000, 10000}; //< ms to wait for next request after n-1-th consecutive error 

	int             numberOfErrors    = 0;
	List<Throwable> errors            = null;
	Throwable		fatalError		  = null;
	
	Status			status;
	
	boolean         useDefaultSessionToken = false; //< whether there should be an initial request before submit to get the upload token;
	
	boolean         postProcRequired = false; //< whether there should be a "last request" after submit is done (to read metadata of upload item)
	boolean         externalPostProcRequired = false;
	int             postProcCallMilis    = 200;   //< ms to wait before sending the last request

	Logger			logger = LoggerFactory.getLogger(Uploader.class);
	
	//-------------------------------------------------------------------------
	
	public UploadItem getUploadingItem() {
		return uploadingItem;
	}

	public FormPanel getTargetForm() {
		return targetForm;
	}

	public int getFirstRequestDelayMs() {
		return firstDelayMilis;
	}

	public void setFirstRequestDelayMs(int firstDelayMilis) {
		this.firstDelayMilis = firstDelayMilis;
	}

	public int getStepDelayMs() {
		return stepDelayMilis;
	}

	public void setStepDelayMs(int stepDelayMilis) {
		this.stepDelayMilis = stepDelayMilis;
	}

	public void setErrorDelayMs(int[] errorDelayMilis) {
		this.errorDelayMilis = errorDelayMilis;
	}

	public int getPostProcessingDelayMs() {
		return postProcCallMilis;
	}

	public void setPostProcessingDelayMs(int lastOneMilis) {
		this.postProcCallMilis = lastOneMilis;
	}

	public boolean isPostProcessingRequired() {
		return postProcRequired;
	}

	public void setPostProcessingRequired(boolean required) {
		this.postProcRequired = required;
	}
	
	public boolean isExternalPostProcessingRequired() {
		return externalPostProcRequired;
	}

	public void setExternalPostProcessingRequired(boolean required) {
		this.externalPostProcRequired = required;
	}

	public List<Throwable> getErrors() {
		return errors;
	}

	public void setErrors(List<Throwable> errors) {
		this.errors = errors;
	}

	public boolean isFullyComplete() {
		return status == Status.FINISHED;
	}

	public Uploader(Hidden hidden, FormPanel form) {
		this(hidden, form, IUploadItemStatusService.Util.Instance());
	}
	
	Uploader(Hidden hiddenField, FormPanel form, IUploadItemStatusServiceAsync service) {
		this.targetForm      = form;
		this.service         = service;
		this.handlerManager  = new HandlerManager(this);
		this.hiddenField     = hiddenField;
		this.originalFormAction = form.getAction();
		
		targetForm.addSubmitCompleteHandler(this);
		targetForm.addSubmitHandler(this);
		
		if (hiddenField != null)
			hiddenField.setName("fieldName");
		
		reset();
	}
	
	public void reset() {
		clearErrors();
		uploadingItem     = null;
		status			  = useDefaultSessionToken ? Status.BEFORE_SUBMIT : Status.CLEAN;
		cancel();
	}
	
	public void fetchInitialItem() {
		if (status != Status.CLEAN) throw new IllegalStateException("Cannot fetch initial item when not in CLEAN state");
		changeStatus(FETCHING_TOKEN);
		fetchItem();
	}
	
	public boolean isReadyToUpload() {
		return status == Status.BEFORE_SUBMIT; 
	}
	
	public void setUseDefaultSessionToken(boolean useDefaultSessionToken) {
		if (this.useDefaultSessionToken == useDefaultSessionToken) return;
		this.useDefaultSessionToken = useDefaultSessionToken;
		if (useDefaultSessionToken && status == Status.CLEAN) {
			status = Status.BEFORE_SUBMIT;
		}
	}
	
	public boolean isUseDefaultSessionToken() {
		return useDefaultSessionToken;
	}

	public void beginUpload() {
		if (status != Status.BEFORE_SUBMIT) {
			throw new IllegalStateException("Not ready to submit (status = "+status+")");
		}
		if (!useDefaultSessionToken && this.uploadingItem == null) {
			throw new IllegalStateException("UploadingItem null and can't use default");
		}
		String fieldName = getCurrentToken();
		if (hiddenField != null) {
			hiddenField.setValue(fieldName);
		}
		targetForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		targetForm.setMethod(FormPanel.METHOD_POST);
		targetForm.setAction(originalFormAction + fieldName + ".upload");
		targetForm.submit();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void run() {
		fetchItem();
	}
	
	public String getCurrentToken() {
		return (uploadingItem == null)
		? (useDefaultSessionToken ? SESSION_DEFAULT_UPLOAD_TOKEN : null)
		: uploadingItem.getToken();
	}
	
	protected void fetchItem() {
		final String itemName = getCurrentToken();
		service.getUploadItem(itemName, new AsyncCallback<UploadItem>() {
			public void onSuccess(UploadItem result) {
				if (result == null) {
					fetchItemError("Getting uploadItem returned null", new NullPointerException("Postprocessing upload item was null"));
					return;
				}
				uploadingItem  = result;
				clearErrors();
				
				if (status == Status.FETCHING_TOKEN) changeStatus(BEFORE_SUBMIT);
				else if (status == SUBMITTING || status == SUBMITTING_GOTINFO) {
					changeStatus(SUBMITTING_GOTINFO);
					if (result.getUploadFailedMessage() != null) {
						fatalError("Server reports failed upload request", new Exception(result.getUploadFailedMessage()));
					}
				} // Don't call completed unless submit request has finished
				else if (status == SUBMITTED_POSTPROC_CALLED) {
					 // This response could be from an earlier info request, where the item was not yet complete
					 if (result.isUploadComplete()) completed();
				}
				if (status == Status.SUBMITTING_GOTINFO) schedule(stepDelayMilis);
			}
			
			public void onFailure(Throwable caught) {
				fetchItemError("Failed to get uploadItem", caught);
			}
		});
	}
	
	public void onSubmit(SubmitEvent event) {
		clearErrors();
		changeStatus(Status.SUBMITTING);
		schedule(firstDelayMilis);
	}
	
	protected void clearErrors() {
		numberOfErrors = 0;
		if (errors != null) errors.clear();
		fatalError = null;
	}

	public void onSubmitComplete(SubmitCompleteEvent event) {
		cancel();
		clearErrors();
		String results = event.getResults();
		if (results == null || !results.startsWith("OK")) {
			/* error */
			fatalError("Invalid submit result", new Exception("The upload has failed, please inform the site administrator. The response returned was: \n "+results));
			return;
		}
		String uploadedToken = results.substring(3);
		if (uploadingItem == null) {
			uploadingItem = new UploadItem();
		} else if (!uploadingItem.getToken().equals(uploadedToken)) {
			/* error */
			if (!useDefaultSessionToken) {
				fatalError("Invalid submit result token",new Exception("The upload seems to have succeeded, but the file key was different than expected; expected: "+uploadingItem.getToken()+" got: "+uploadedToken));
				return;
			}
		}
		uploadingItem.setToken(uploadedToken);
		if (postProcRequired && !uploadingItem.isUploadComplete()) {
			changeStatus(SUBMITTED_POSTPROC_CALLED);
			fetchItem();
		} else if(externalPostProcRequired) {
			changeStatus(SUBMITTED_EXTERNAL_POSTPROC_CALLED);
		} else {
			completed();
		}
	}
	
	protected void fetchItemError(String message, Throwable caught) {
		numberOfErrors++;
		if (status == Status.FETCHING_TOKEN || status == SUBMITTED_POSTPROC_CALLED) {
			if (numberOfErrors >= NUM_TRIES) {
				fatalError(message, caught);
			 	return;
			}
		}
		logger.error(message, caught);
		if (errors == null) errors = new ArrayList<Throwable>();
		errors.add(caught);
		
		final int delay = errorDelayMilis[numberOfErrors > errorDelayMilis.length ? errorDelayMilis.length-1 : numberOfErrors-1];
		schedule(delay);
		fireEvent(new UploadErrorEvent(this, errors));
	}
	
	protected void fatalError(String message, Throwable error) {
		cancel();
		clearErrors();
		fatalError = error;
		logger.error(message, error);
		fireEvent(new UploadErrorEvent(this, error, true));
	}
	
	protected void completed() {
		status = FINISHED;
		fireEvent(new UploadCompleteEvent(this));
	}
	
	protected void changeStatus(Status newStatus) {
		status = newStatus;
		fireEvent(new UploadStatusEvent(this, uploadingItem));
	}
	
	public static class UploadCompleteEvent extends GwtEvent<UploadCompleteHandler> {
		public static final Type<UploadCompleteHandler> TYPE = new Type<UploadCompleteHandler>();
		
		private Uploader uploader;
		
		public UploadCompleteEvent(Uploader uploader) {
			this.uploader = uploader;
		}
		
		public Uploader getUploader() {
			return uploader;
		}

		@Override
		protected void dispatch(UploadCompleteHandler handler) {
			handler.onUploadComplete(this);
		}

		@Override
		public Type<UploadCompleteHandler> getAssociatedType() {
			return TYPE;
		}
	}
	
	public static class UploadErrorEvent extends GwtEvent<UploadErrorHandler>  {
		public static final Type<UploadErrorHandler> TYPE = new Type<UploadErrorHandler>();
		
		private Uploader        uploader;
		private List<Throwable> errors;
		private boolean 		fatal;

		public UploadErrorEvent(Uploader uploader, List<Throwable> errors) {
			this.uploader = uploader;
			this.errors   = errors;
			this.fatal  = false;
		}

		public UploadErrorEvent(Uploader uploader, Throwable error, boolean fatal) {
			this.uploader = uploader;
			this.errors   = Collections.singletonList(error);
			this.fatal  = fatal;
		}
		
		public Uploader getUploader() {
			return uploader;
		}
		
		public boolean isFatalError() {
			return fatal;
		}
		
		public Throwable getFatalError() {
			if (!fatal) return null;
			return errors.get(0);
		}
		
		public List<Throwable> getErrors() {
			return errors;
		}

		@Override
		protected void dispatch(UploadErrorHandler handler) {
			handler.onUploadError(this);
		}

		@Override
		public Type<UploadErrorHandler> getAssociatedType() {
			return TYPE;
		}
	}
	
	public static class UploadStatusEvent extends GwtEvent<UploadStatusHandler> {
		public static final Type<UploadStatusHandler> TYPE = new Type<UploadStatusHandler>();
		
		Uploader   uploader;
		UploadItem item;
		Status	   newStatus;
		boolean	   error;
		
		public UploadStatusEvent(Uploader uploader, UploadItem item) {
			this.uploader = uploader;
			this.item     = item;
		}
		
		public Uploader getUploader() {
			return uploader;
		}

		public UploadItem getItem() {
			return item;
		}

		@Override
		protected void dispatch(UploadStatusHandler handler) {
			handler.onUploadStatusChange(this);
		}

		@Override
		public Type<UploadStatusHandler> getAssociatedType() {
			return TYPE;
		}
	}
	
	public interface UploadEventsHandler extends UploadCompleteHandler, UploadErrorHandler, UploadStatusHandler {
	}
	
	public interface UploadCompleteHandler extends EventHandler {
		void onUploadComplete(UploadCompleteEvent event);
	}
	public interface UploadErrorHandler extends EventHandler {
		void onUploadError(UploadErrorEvent event);
	}
	public interface UploadStatusHandler extends EventHandler {
		void onUploadStatusChange(UploadStatusEvent event);
	}
	
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}
	
	public HandlerRegistration addUploadCompleteHandler(UploadCompleteHandler handler) {
		return handlerManager.addHandler(UploadCompleteEvent.TYPE, handler);
	}
	
	public HandlerRegistration addUploadErrorHandler(UploadErrorHandler handler) {
		return handlerManager.addHandler(UploadErrorEvent.TYPE, handler);
	}
	
	public HandlerRegistration addUploadStatusHandler(UploadStatusHandler handler) {
		return handlerManager.addHandler(UploadStatusEvent.TYPE, handler);
	}

	public boolean shouldResetBeforeUpload() {
		if (uploadingItem  == null && !useDefaultSessionToken) return true;
		if (status != Status.BEFORE_SUBMIT) return true;
		return false;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	public Throwable getFatalError() {
		return fatalError;
	}
	
	public void updateInfoFromInput(FileUpload uploadElement) {
		if (uploadElement == null || uploadingItem == null) return;
		if (StringUtil.isNullOrEmpty(uploadingItem.getFileName())) {
			uploadingItem.setFileName(uploadElement.getFilename());
		}
	}

	public int getInfoErrorCount() {
		return numberOfErrors;
	}
}