package au.com.jobinhood.android.app.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import android.util.Log;
import android.util.SparseArray;
import au.com.jobinhood.android.app.modal.AbstractParticipant;
import au.com.jobinhood.android.app.modal.Conversation;
import au.com.jobinhood.android.app.modal.ConversationMessage;
import au.com.jobinhood.android.app.modal.ConversationParticipant;
import au.com.jobinhood.android.app.modal.ConversationParticipantMeta;

/**
 * Container class for conversations;
 *
 */
public class ConversationList {
	
	private ArrayList<Conversation> conversations;

	private static final int HASH_CAPACITY = 4;

	public static final String POSITION = "position_clicked";
	
	public ConversationList()
	{
		conversations = new ArrayList<Conversation>();
		
		//testing code
		//conversations = (ConversationBuilder.buildConversation());
		
		// try to load conversations from db
		
		//generate empty list if db is null
	}
	
	//GET/SET METHODS
	public ArrayList<Conversation> getConversations()
	{
		return conversations;
	}

	public Conversation getConversation(int id)
	{
		return conversations.get(id);
	}
	
	public void addConversation(Conversation c)
	{
		conversations.add(c);
		return;
	}

	/**
	 * Singlar conversation from JSON response
	 */
	public void addConversationFromJson(JsonObject jsonCon) {
		//if conversation exist in DB then load from db
		
		//if conversation does not exist - create new conversation
		Conversation con = new Conversation();
		
		//temp holder of participants to reduce operational run-time (this increases operational memory as a result)
		Log.d("santiago", "building sparsearray");
		SparseArray<AbstractParticipant> sparseParArray = new SparseArray<AbstractParticipant>(HASH_CAPACITY);
		for (AbstractParticipant p : con.getParticipants()) {
			sparseParArray.put(p.getId(), p);
		}
		
		con.setId(jsonCon.get("id").getAsInt());
		con.setSubject(jsonCon.get("subject").getAsString());
		Log.d("santiago", "ConvoId:"+con.getId() + ", ConvoSubject:" + con.getSubject());
		
		Log.d("santiago", "iterating participants");
		for (JsonElement p : jsonCon.get("participants").getAsJsonArray()) {
			JsonObject jParticipant = p.getAsJsonObject();
			//check participant doenst already exist - if true add new participant
			if (sparseParArray.get(jParticipant.get("id").getAsInt()) == null) {
				Log.d("santiago", "adding new participant");
				AbstractParticipant participant = getParticipantFromJson(jParticipant);
				sparseParArray.put(participant.getId(), participant);
				con.addParticipant(participant);
				Log.d("santiago", "added new participant, id:" + participant.getId());
			}
		}
		
		/**
		 * @precondition each message_data has an attached 'participant' data which already
		 * EXISTS in the list of participants attached to this conversation.
		 * Note: an if check is made to ensure the code does not crash. HOWEVER this precondition MUST
		 * be met to ensure app integrity.
		 * This precondition should be met at the server-level (host.jobinhood.com.au)
		 */
		Log.d("santiago", "iterating messages");
		for (JsonElement message : jsonCon.get("messages").getAsJsonArray()) {
			//build msg
			JsonObject messageObj = message.getAsJsonObject();
			ConversationMessage msg = getMessageFromJson(messageObj);
			
			//set the message owner
			int ownerId = messageObj.get("sender").getAsJsonObject().get("id").getAsInt();
			Log.d("santiago", "jMessage ownerId:" + ownerId);
			if (sparseParArray.get(ownerId) != null)
				msg.setMessageOwner(sparseParArray.get(ownerId));
			
			//set the message meta
			//TODO this version (1.0) does not use message_meta_data
			
			//attach links (bi-directional)
			msg.setConversation(con);
			con.addMessage(msg);
		}
		
		//set the conversation meta
		for (JsonElement metadata : jsonCon.get("metadata").getAsJsonArray()) {
			//TODO this version (1.0) does not use conversation_meta_data
		}
		
		addConversation(con);
	}
	
	private ConversationParticipantMeta getMetaFromJson(JsonObject jsonMeta) {
		return null;
	}

	private ConversationMessage getMessageFromJson(JsonObject jsonMsg) {
		ConversationMessage msg = new ConversationMessage();
		msg.setId(jsonMsg.get("id").getAsInt());
		msg.setBody(jsonMsg.get("body").getAsString());
		msg.setTimestamp(getDateFromJson(jsonMsg.get("createdAt").getAsString()));
		
		return msg;
	}

	private AbstractParticipant getParticipantFromJson(JsonObject jParticipant) {
		ConversationParticipant p = new ConversationParticipant();
		p.setId(jParticipant.get("id").getAsInt());
		p.setUsername(jParticipant.get("username").getAsString());
		p.setFirstname(jParticipant.get("firstName").getAsString());
		p.setUrl(getUserProfilePicture(jParticipant.get("picture").getAsJsonObject()));
		
		return p;
	}
	
	/**
	 * Need to double check api and then complete this method, for now using dummy picture as backup
	 * @param jPicture json object which contains the url needed to download said pictures
	 * @return the url to download the profile picture
	 */
	private String getUserProfilePicture(JsonObject jsonPicture) {
		return "pusspuss.jpg";
	}
	
	public static Date getDateFromJson(String timestamp) {
		Log.d("santiago", "jsonTimestamp: " + timestamp);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		Date date = null;
		
		try {
			date = sdf.parse(timestamp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Log.d("santiago", "reverse-date-parse: " + sdf.format(date));
		return date;
	}
}
