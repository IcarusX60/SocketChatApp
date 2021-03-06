package com.example.manug.peerchat;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
public class MessageAdapter extends ArrayAdapter<Message> {
    public MessageAdapter(Activity context,ArrayList<Message> arr) {
        super(context, 0,arr);
    }
    @Override
    public View getView(int position, View converView, ViewGroup parent){
        View listItemView=converView;
        if(listItemView==null){
            listItemView= LayoutInflater.from(getContext()).inflate(R.layout.messagelist,parent,false);
        }
        Message currentMessage=getItem(position);
        String message=currentMessage.getMessage();
        if(currentMessage.isSent()){
            TextView sent=listItemView.findViewById(R.id.list_sent);
            sent.setText(message);
            sent.setVisibility(View.VISIBLE);
        }
        else{
            TextView received=listItemView.findViewById(R.id.list_received);
            received.setText(message);
            received.setVisibility(View.VISIBLE);
        }
        return listItemView;
    }
}
