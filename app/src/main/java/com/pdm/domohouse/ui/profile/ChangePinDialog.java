package com.pdm.domohouse.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.pdm.domohouse.R;

/**
 * Diálogo para cambiar el PIN de acceso del usuario
 * Solicita PIN actual y nuevo PIN con confirmación
 */
public class ChangePinDialog {
    
    private final Context context;
    private final ProfileViewModel viewModel;
    private AlertDialog dialog;
    
    public ChangePinDialog(Context context, ProfileViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
    }
    
    /**
     * Muestra el diálogo para cambiar PIN
     */
    public void show() {
        // Inflar el layout del diálogo
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_pin, null);
        
        // Obtener referencias a los campos
        EditText currentPinEditText = dialogView.findViewById(R.id.currentPinEditText);
        EditText newPinEditText = dialogView.findViewById(R.id.newPinEditText);
        EditText confirmPinEditText = dialogView.findViewById(R.id.confirmPinEditText);
        
        // Crear y mostrar el diálogo
        dialog = new AlertDialog.Builder(context)
            .setTitle("Cambiar PIN")
            .setView(dialogView)
            .setPositiveButton("Cambiar", null) // Se configurará después
            .setNegativeButton("Cancelar", null)
            .create();
        
        // Configurar el botón positivo después de mostrar el diálogo
        // para poder controlar cuándo se cierra
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String currentPin = currentPinEditText.getText().toString().trim();
                String newPin = newPinEditText.getText().toString().trim();
                String confirmPin = confirmPinEditText.getText().toString().trim();
                
                // Validar campos
                if (currentPin.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
                    Toast.makeText(context, "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (currentPin.length() != 4 || newPin.length() != 4 || confirmPin.length() != 4) {
                    Toast.makeText(context, "El PIN debe tener 4 dígitos", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!newPin.equals(confirmPin)) {
                    Toast.makeText(context, "Los nuevos PINs no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (currentPin.equals(newPin)) {
                    Toast.makeText(context, "El nuevo PIN debe ser diferente al actual", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Cambiar el PIN
                viewModel.changePin(currentPin, newPin, confirmPin);
                dialog.dismiss();
            });
        });
        
        dialog.show();
    }
}