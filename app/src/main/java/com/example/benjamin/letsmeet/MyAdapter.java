package com.example.benjamin.letsmeet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<Group> groups;
    private Context context;
    private OnGroupClickListerner listener;
    public List<String> checkedItems = new ArrayList<>();

    public MyAdapter(List<Group> groups, Context context, OnGroupClickListerner listener) {
        this.groups = groups;
        this.context = context;
        this.listener = listener;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final Group single_group = groups.get(position);
        holder.textViewGroup.setText(single_group.getTopic());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(single_group);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onItemLongClick(single_group);
                return true;
            }
        });

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.checkBox.isChecked()){
                    checkedItems.add(groups.get(position).getGroupID());
                }
                if (!holder.checkBox.isChecked()){
                    checkedItems.remove(groups.get(position).getGroupID());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewGroup, textViewMembers;
        public CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGroup = itemView.findViewById(R.id.textViewGroup);
            textViewMembers = itemView.findViewById(R.id.textViewMembers);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}

