package com.aditiyaeka.tdl.Adapter;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

import android.app.assist.AssistStructure;
import android.content.Context;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aditiyaeka.tdl.AddNewTask;
import com.aditiyaeka.tdl.MainActivity;
import com.aditiyaeka.tdl.R;
import com.aditiyaeka.tdl.Utils.DataBaseHelper;

import java.util.List;

import com.aditiyaeka.tdl.Model.ToDoModel;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {
    private List<ToDoModel> mlist;
    private MainActivity activity;
    private DataBaseHelper myDB;

    public ToDoAdapter(DataBaseHelper myDB, MainActivity activity) {
        this.activity = activity;
        this.myDB = myDB;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final ToDoModel item = this.mlist.get(position);
        holder.checkBox.setText(item.getTask());
        holder.checkBox.setChecked(toBoolean(item.getStatus()));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    myDB.updateStatus(item.getId(), 1);
                } else {

                    myDB.updateStatus(item.getId(), 0);

                }
            }

        });


    }

    public boolean toBoolean(int num) {
        return num != 0;
    }

    public Context getContext() {
        return activity;
    }


    @Override
    public int getItemCount() {
        return mlist.size();
    }
    public void setTask(List<ToDoModel>mlist) {
        this.mlist = mlist;
        notifyDataSetChanged();
    }

    public void deleteTask(int position){
        ToDoModel item = this.mlist.get(position);
        myDB.deleteTask(item.getId());

        this.mlist.remove(position);
        notifyItemRemoved(position);

    }
    public void editItems (int position){
        ToDoModel item=this.mlist.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id",item.getId());
        bundle.putString("task",item.getTask());

        AddNewTask task= new AddNewTask();
        task.setArguments(bundle);
        task.show(activity.getSupportFragmentManager(), task.getTag());
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        CheckBox checkBox;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}
