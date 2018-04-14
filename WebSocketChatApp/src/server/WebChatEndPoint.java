package server;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chatServerEndpoint")
public class WebChatEndPoint {
	Set<Session> activeUsers = new HashSet<>();
	
	@OnOpen
	public void onConnectionOpen(Session userSession){
		synchronized (activeUsers) {
			activeUsers.add(userSession);
		}
	}
	
	@OnMessage
	public void onMessageRecieve(String message, Session userSession){
		String userName = (String)userSession.getUserProperties().get("userName");
		if(userName == null){
			userSession.getUserProperties().put("userName", message);
			try {
				userSession.getBasicRemote().sendText(buildJosnString("Server", "You are now connected as "+message));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			synchronized (activeUsers) {
				activeUsers.forEach((activeUser) ->{
					try {
						activeUser.getBasicRemote().sendText(buildJosnString(userName, message));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		}
	}
	
	@OnClose
	public void onConnectionClose(Session userSession){
		synchronized (activeUsers) {
			activeUsers.remove(userSession);
		}
	}
	
	private String buildJosnString(String userName, String message){
		JsonObject jsonObject = Json.createObjectBuilder().add("message", userName + ":" + message).build();
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter  = Json.createWriter(stringWriter);
		jsonWriter.writeObject(jsonObject);
		return jsonWriter.toString();
	}
}
