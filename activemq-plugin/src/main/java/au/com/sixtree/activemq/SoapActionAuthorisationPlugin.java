package au.com.sixtree.activemq;

import java.util.List;
import java.util.Map;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;

public class SoapActionAuthorisationPlugin implements BrokerPlugin{
	Map<String,List<String>> allowedUsers;
	public Broker installPlugin(Broker broker) throws Exception {
		return new SoapActionAuthorisationFilter(broker,allowedUsers);
	}
	
	public Map<String,List<String>> getAllowedUsers(){
		return this.allowedUsers;
	}
	
	public void setAllowedUsers(Map<String,List<String>> allowedUsers){
		this.allowedUsers = allowedUsers;
	}

}
