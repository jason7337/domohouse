package com.pdm.domohouse.ui.temperature.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.pdm.domohouse.data.model.RoomTemperature;
// import com.pdm.domohouse.databinding.ItemRoomTemperatureBinding;

/**
 * Adaptador para mostrar las temperaturas de las habitaciones en un RecyclerView
 */
public class RoomTemperatureAdapter extends ListAdapter<RoomTemperature, RoomTemperatureAdapter.RoomTemperatureViewHolder> {
    
    public RoomTemperatureAdapter() {
        super(DIFF_CALLBACK);
    }
    
    private static final DiffUtil.ItemCallback<RoomTemperature> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<RoomTemperature>() {
            @Override
            public boolean areItemsTheSame(@NonNull RoomTemperature oldItem, @NonNull RoomTemperature newItem) {
                return oldItem.getRoomId().equals(newItem.getRoomId());
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull RoomTemperature oldItem, @NonNull RoomTemperature newItem) {
                return oldItem.getCurrentTemperature() == newItem.getCurrentTemperature() &&
                       oldItem.getHumidity() == newItem.getHumidity() &&
                       oldItem.getStatus() == newItem.getStatus() &&
                       oldItem.getIsOnline() == newItem.getIsOnline() &&
                       oldItem.getSensorCount() == newItem.getSensorCount();
            }
        };
    
    @NonNull
    @Override
    public RoomTemperatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Temporarily commented out due to missing layout
        // ItemRoomTemperatureBinding binding = ItemRoomTemperatureBinding.inflate(
        //     LayoutInflater.from(parent.getContext()), parent, false);
        // return new RoomTemperatureViewHolder(binding);
        
        // Temporary placeholder
        android.view.View view = LayoutInflater.from(parent.getContext())
            .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new RoomTemperatureViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RoomTemperatureViewHolder holder, int position) {
        RoomTemperature roomTemperature = getItem(position);
        holder.bind(roomTemperature);
    }
    
    /**
     * ViewHolder para los items de temperatura por habitación
     */
    static class RoomTemperatureViewHolder extends RecyclerView.ViewHolder {
        
        // private final ItemRoomTemperatureBinding binding;
        
        public RoomTemperatureViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            // this.binding = binding;
        }
        
        /**
         * Vincula los datos de temperatura con la vista
         */
        public void bind(RoomTemperature roomTemperature) {
            // Temporary implementation using simple text view
            if (itemView instanceof android.widget.TextView) {
                ((android.widget.TextView) itemView).setText(
                    roomTemperature.getRoomName() + " - " + 
                    String.format("%.1f°C", roomTemperature.getCurrentTemperature())
                );
            }
            
            // binding.setRoomTemperature(roomTemperature);
            
            // Configurar click listener para la habitación
            itemView.setOnClickListener(v -> {
                // Aquí se podría navegar a detalles de la habitación
                // o mostrar más información en un dialog
            });
            
            // binding.executePendingBindings();
        }
    }
}