package net.jtreemer.labolink;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class LaboLinkOrder extends NanoHTTPD {
	
	private static final String EXCHANGE_NAME = "LABOLINK_ORDER";
	private static ConnectionFactory connectionFactory;
	
	private void sendOrder(String dev,String hn) {
		 if(connectionFactory == null) {
			 String mqServer = Main.AppConfig.getProperty("rabbitmq.server","127.0.0.1");
			 connectionFactory = new ConnectionFactory();
			 connectionFactory.setUsername("admin");
			 connectionFactory.setPassword("123456");
			 connectionFactory.setVirtualHost("/");
			 connectionFactory.setHost(mqServer);
		 }
		 if(connectionFactory!=null) {
				try {
					
			        Connection connection = connectionFactory.newConnection();
			        Channel channel = connection.createChannel();
			        //ObjectMapper objString = new ObjectMapper();
			        
			        String message = hn;
			        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
		
			        String routingKey = dev;
			        
			        channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
			        System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
		
			        connection.close();
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
		}
	 }
	
	 @Override
     public Response serve(IHTTPSession session) {

		 String msg = "<html><body><h1>Hello server</h1>\n";
         if(Method.GET.equals(session.getMethod())){
        	 String uri = session.getUri();
        	 if(uri.startsWith("/labolink/order/")) {
        		 String[] params = uri.substring("/labolink/order/".length()).split("/",2);
        		 if(params.length>=2) {
        			 String devId = params[0];
        			 String hn = params[1].replaceAll("_", "/");
        			 if("auto".equals(devId)) {
        				String ip = session.getRemoteIpAddress();
        				if(Main.AppConfig.containsKey("order.map."+ip)) {
        					devId = Main.AppConfig.getProperty("order.map."+ip);
        				}
        			 }
        			 sendOrder(devId, hn);
        			 
        			 return newFixedLengthResponse(Status.OK, "application/json", "{\"status\":\"OK\"}");
        		 }
        	 }
        	 return newFixedLengthResponse(Status.BAD_REQUEST, "application/json", "{\"status\":\"BAD_REQUEST\"}");
        	 //System.out.println(uri);
         }
         /*Map<String, List<String>> parms = session.getParameters();
         if (parms.get("username") == null) {
             msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
         } else {
             msg += "<p>Hello, " + parms.get("username") + "!</p>";
         }*/
         return newFixedLengthResponse(msg + "</body></html>\n");
     }

	public LaboLinkOrder(int port) {
		super(port);
	}




}
