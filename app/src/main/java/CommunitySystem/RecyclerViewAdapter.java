//package CommunitySystem;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.onwheelsapi28android90.R;
//import com.google.android.material.button.MaterialButton;
//
//import java.util.ArrayList;
//
//
//public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
//
//    private ArrayList<String> profileImages = new ArrayList<>();
//    private ArrayList<String> usernames = new ArrayList<>();
//    private ArrayList<String> messageTexts = new ArrayList<>();
//    private Context context;
//
//
//    //get all the data that need to be placed in the recycler view
//    public RecyclerViewAdapter(Context context, ArrayList<String> profileImages, ArrayList<String> usernames, ArrayList<String> messageTexts){
//        this.profileImages = profileImages;
//        this.usernames = usernames;
//        this.messageTexts = messageTexts;
//        this.context = context;
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_message, parent, false);
//        ViewHolder holder = new ViewHolder(view);
//        return holder;
//    }
//
//    //!! Insert the data in the layout
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//
//        //TODO --------------------Update profile image
//        holder.usernameTextView.setText(usernames.get(position));
//        holder.messageBubbleButton.setText(messageTexts.get(position));
//
//        holder.messageContainerRelativeLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(context, "Message clicked !", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    //List how many elements are in the RecyclerView
//    @Override
//    public int getItemCount() {
//        return usernames.size();
//    }
//
//
//
//
//
//
//    //Holds each entry in memory
//    public class ViewHolder extends RecyclerView.ViewHolder{
//
//        ImageView profileImageImageView;
//        TextView usernameTextView;
//        MaterialButton messageBubbleButton;
//        RelativeLayout messageContainerRelativeLayout;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            profileImageImageView = itemView.findViewById(R.id.profileImageImageView);
//            usernameTextView = itemView.findViewById(R.id.usernameTextView);
//            messageBubbleButton = itemView.findViewById(R.id.messageBubbleButton);
//            messageContainerRelativeLayout = itemView.findViewById(R.id.messageContainerRelativeLayout);
//        }
//    }
//}
