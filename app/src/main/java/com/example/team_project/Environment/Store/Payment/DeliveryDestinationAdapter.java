// DeliveryDestinationAdapter.java
package com.example.team_project.Environment.Store.Payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DeliveryDestinationAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> deliveryDestinations;
    private FirebaseFirestore db;
    private FirebaseUser user;

    public DeliveryDestinationAdapter(@NonNull Context context, ArrayList<String> deliveryDestinations) {
        super(context, 0, deliveryDestinations);
        this.context = context;
        this.deliveryDestinations = deliveryDestinations;
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.fragment_environment_store_product_payment_delivery_destination_item, parent, false);
        }

        String deliveryDestination = deliveryDestinations.get(position);

        TextView deliveryDestinationTextView = convertView.findViewById(R.id.delivery_destination_text_view);
        Button deleteButton = convertView.findViewById(R.id.delete_button);
        Button editButton = convertView.findViewById(R.id.edit_button);
        Button selectButton = convertView.findViewById(R.id.select_button);

        deliveryDestinationTextView.setText(deliveryDestination);

        deleteButton.setOnClickListener(v -> {
            deliveryDestinations.remove(position);
            db.collection("users").document(user.getUid()).update("deliveryDestinations", deliveryDestinations).addOnSuccessListener(aVoid -> {
                notifyDataSetChanged();
                Toast.makeText(context, "배송지가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            });
        });

        editButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("배송지 수정");

            final EditText input = new EditText(context);
            input.setText(deliveryDestination);
            builder.setView(input);

            builder.setPositiveButton("수정", (dialog, which) -> {
                String newDeliveryDestination = input.getText().toString();
                if (!newDeliveryDestination.isEmpty()) {
                    deliveryDestinations.set(position, newDeliveryDestination);
                    db.collection("users").document(user.getUid()).update("deliveryDestinations", deliveryDestinations).addOnSuccessListener(aVoid -> {
                        notifyDataSetChanged();
                        Toast.makeText(context, "배송지가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    });
                }
            });

            builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        selectButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("deliveryDestination", deliveryDestination);
            ((Activity) context).setResult(Activity.RESULT_OK, resultIntent);
            ((Activity) context).finish();
            ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right);
        });

        return convertView;
    }
}