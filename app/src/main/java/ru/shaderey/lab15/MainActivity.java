package ru.shaderey.lab15;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private EditText taskTitleEditText;
    private EditText taskDescriptionEditText;
    private Button addTaskButton;
    private Button updateTaskButton;
    private ListView taskListView;
    private ArrayList<Task> taskList;
    private ArrayAdapter<Task> taskAdapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference tasksRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tasksRef = db.collection("tasks");

        taskTitleEditText = findViewById(R.id.taskTitleEditText);
        taskDescriptionEditText = findViewById(R.id.taskDescriptionEditText);
        addTaskButton = findViewById(R.id.addTaskButton);
        updateTaskButton = findViewById(R.id.editTaskButton);
        taskListView = findViewById(R.id.taskListView);

        taskList = new ArrayList<>();
        taskAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
        taskListView.setAdapter(taskAdapter);

        addTaskButton.setOnClickListener(v -> addTask());
        taskListView.setOnItemLongClickListener(this::deleteTask);
        taskListView.setOnItemClickListener(this::editTask);
        updateTaskButton.setOnClickListener(v -> updateTask());

        loadTasksFromFirebase();
    }

    private void addTask() {
        String title = taskTitleEditText.getText().toString().trim();
        String description = taskDescriptionEditText.getText().toString().trim();
        if (!title.isEmpty() && !description.isEmpty()) {
            Task task = new Task(title, description);
            tasksRef.add(task)
                    .addOnSuccessListener(documentReference -> {
                        task.setId(documentReference.getId());
                        taskList.add(task);
                        taskAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {

                    });

            taskTitleEditText.setText("");
            taskDescriptionEditText.setText("");
        }
    }

    private void loadTasksFromFirebase() {
        tasksRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            taskList.clear();
            if (queryDocumentSnapshots != null) {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Task task = doc.toObject(Task.class);
                    task.setId(doc.getId());
                    taskList.add(task);
                }
            }
            taskAdapter.notifyDataSetChanged();
        });
    }

    private boolean deleteTask(AdapterView<?> parent, View view, int position, long id) {
        Task task = taskList.get(position);
        tasksRef.document(task.getId()).delete().addOnSuccessListener(documentReference -> {
            taskList.remove(task);
            taskAdapter.notifyDataSetChanged();
        });
        return true;
    }

    private Task editiousTask;
    private void editTask(AdapterView<?> parent, View view, int position, long id) {
        Task task = taskList.get(position);
        if (task == null) {
            return;
        }
        editiousTask = task;
        String title = task.getTitle();
        String description = task.getDescription();
        taskTitleEditText.setText(title);
        taskDescriptionEditText.setText(description);
        addTaskButton.setVisibility(View.GONE);
        updateTaskButton.setVisibility(View.VISIBLE);
    }

    private void updateTask() {
        tasksRef.document(editiousTask.getId())
                .update("title", taskTitleEditText.getText().toString(), "description", taskDescriptionEditText.getText().toString())
                .addOnSuccessListener(documentReference -> {
                    for (int i = 0; i < taskList.size(); i++) {
                        if (!taskList.get(i).getId().equals(editiousTask.getId())) {
                            return;
                        }
                        editiousTask.setTitle(taskTitleEditText.getText().toString());
                        editiousTask.setDescription(taskDescriptionEditText.getText().toString());
                        taskList.set(i, editiousTask);
                    }
        });
        addTaskButton.setVisibility(View.VISIBLE);
        updateTaskButton.setVisibility(View.GONE);
    }
}