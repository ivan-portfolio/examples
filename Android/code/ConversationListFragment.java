package au.com.jobinhood.android.app.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import au.com.jobinhood.android.app.R;
import au.com.jobinhood.android.app.activity.ConversationActivity;
import au.com.jobinhood.android.app.core.ConversationList;
import au.com.jobinhood.android.app.core.SantiagoAndroid;
import au.com.jobinhood.android.app.modal.Conversation;

public class ConversationListFragment extends ListFragment {
	private ArrayList<Conversation> conversations;
	
    private SwipeRefreshLayout mSwipeRefreshLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		conversations = SantiagoAndroid.get().getConversationList().getConversations();

		ArrayAdapter<Conversation> adapter = new ConversationAdapter(conversations);
		
		setListAdapter(adapter);
		
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		//start conversation_activity
		Intent i = new Intent(getActivity(), ConversationActivity.class);
		i.putExtra(ConversationFragment.EXTRA_CONVO_POSITION, position);
		startActivity(i);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((ConversationAdapter)getListAdapter()).notifyDataSetChanged();
	}
	
	/**
	 * ConversationAdapter is used to dynamically generate layout elements
	 * inside the FrameLayout. This particular adapter generates the list view
	 * for each conversation which the user has access to (can see from their phone).
	 */
	private class ConversationAdapter extends ArrayAdapter<Conversation> {
		
		public ConversationAdapter(ArrayList<Conversation> conversations) {
			super(getActivity(), 0, conversations);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater()
						.inflate(R.layout.list_view_conversation, null);
			}
			
			/**
			 * Configure the view for this conversation
			 * dynamically configure view elements here with conversation data
			 */
			Conversation conversation = conversations.get(position);
			
			TextView name = (TextView) convertView.findViewById(R.id.conversation_participant_label);
			name.setText(conversation.toString());
			
			TextView lastmessage = (TextView) convertView.findViewById(R.id.conversation_last_message);
			//TODO fix this
			lastmessage.setText(conversations.get(position).getMessagesAsArray().get(conversations.get(position).getMessages().size()-1).toString());
			
			ImageView avatar = (ImageView)convertView.findViewById(R.id.conversation_profile_image);
			
			//avatar.setImageResource(Integer.parseInt(conversations.get(position).getParticipantsAsArray().get(0).getUrl()));
			
			return convertView;
		}
	}
	
	

}
