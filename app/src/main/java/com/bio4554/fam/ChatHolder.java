package com.bio4554.fam;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by bio4554 on 6/19/2016.
 */

public class ChatHolder extends RecyclerView.ViewHolder{
    View mView;
    public ChatHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setName(String name) {
        TextView field = (TextView)mView.findViewById(R.id.textViewName);
        field.setText(name);
    }

    public void setMessage(String message) {
        TextView field = (TextView)mView.findViewById(R.id.textViewMessage);
        field.setText(message);
    }
}