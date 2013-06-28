package au.com.sixtree.activemq;
import java.util.List;
import java.util.Map;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.command.Message;
 
public class SoapActionAuthorisationFilter extends BrokerFilter {
	Map<String,List<String>> allowedUsers;
    public SoapActionAuthorisationFilter(Broker next, Map<String,List<String>> allowedUsers) {
        super(next);
        this.allowedUsers = allowedUsers;
    }
 
    /**
     * Authorise a message send against the provided username and SOAP Action header. 
     * @throws java.lang.SecurityException if the client is not authorised.
     */
    @Override
    public void send(ProducerBrokerExchange exch, Message msg) throws Exception {
    	//Check to see if this is a reply queue. 
    	String destinationName = msg.getDestination().getPhysicalName();
    	if(destinationName !=null && destinationName.toLowerCase().contains("response") == false){
    		String userName = exch.getConnectionContext().getUserName();
    		String soapAction = (String) msg.getProperty("SOAPJMS_soapAction");
    		if(soapAction != null && allowedUsers.containsKey(soapAction)){
    			if(isAuthorised(userName, soapAction)) {
    				super.send(exch, msg);
    			} else {
    				throw new SecurityException("User [" + userName + "] not authorised for SOAPAction " + soapAction);
    			}
    		}else{
    			throw new SecurityException("SOAPJMS_soapAction not found in JMS properties");
    		}
    	}
    }
    
 
    /** Perform the actual authorisation check. */
    private boolean isAuthorised(String userName, String soapAction) {
    	
    	if(allowedUsers.containsKey(soapAction)){
    		List<String> userString = allowedUsers.get(soapAction);
    		return userString.contains(userName);
    	}else{
    		return false;
    	}
    	
    }
}