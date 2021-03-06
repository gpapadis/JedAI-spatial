package dit.anonymous.webapp.utilities;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import dit.anonymous.webapp.utilities.events.EventMessage;


/**
 * Send SSE to the front-end
 * */

@Component
public  class SSE_Manager implements ApplicationListener<EventMessage>{
	private static SseEmitter emitter = null ;
	
	 
	public void setEmitter(SseEmitter emitter) { SSE_Manager.emitter = emitter;	}
	
	/**
	 * Listen to an emitted event and send the SSE to the front-end
	 * @param event the emitted event containing the SSE name and the message
	 * */
	@Override
	public void onApplicationEvent(EventMessage event) {
	    try {
	    	
	    	
	    	String message = event.getMessage();
	    	
	    	// multiline message
	    	if (message.contains("\n")) {
	    		if(message.charAt(0)!= '\n')
	    			message = "data:" + message;
	    		message = message.replace("\n", "\ndata:");
	    	}
	    	
	    	SseEventBuilder builder = SseEmitter.event().name(event.getEventName()).data(message);
	    	SSE_Manager.emitter.send(builder);

	      }
	      catch (Exception e) {
	    	  e.printStackTrace();
	    	  
	      }
	}
	

}
