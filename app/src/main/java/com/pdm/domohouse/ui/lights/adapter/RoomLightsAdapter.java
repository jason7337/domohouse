package com.pdm.domohouse.ui.lights.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.pdm.domohouse.R;
import com.pdm.domohouse.databinding.ItemRoomLightsBinding;
import com.pdm.domohouse.ui.lights.model.RoomWithLights;

/**
 * Adaptador para mostrar habitaciones con sus dispositivos de iluminación
 * Utiliza ListAdapter para optimización automática con DiffUtil
 */
public class RoomLightsAdapter extends ListAdapter<RoomWithLights, RoomLightsAdapter.RoomLightsViewHolder> {

    // Interface para manejar clicks en elementos
    public interface OnRoomLightClickListener {
        void onRoomToggle(String roomId, boolean turnOn);
        void onDeviceToggle(String deviceId);
        void onDeviceIntensityChange(String deviceId, float intensity);
    }

    private OnRoomLightClickListener clickListener;

    public RoomLightsAdapter() {
        super(DIFF_CALLBACK);
    }

    // DiffUtil callback para optimizar actualizaciones
    private static final DiffUtil.ItemCallback<RoomWithLights> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<RoomWithLights>() {
            @Override
            public boolean areItemsTheSame(@NonNull RoomWithLights oldItem, 
                                         @NonNull RoomWithLights newItem) {
                return oldItem.getRoom().getId().equals(newItem.getRoom().getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull RoomWithLights oldItem, 
                                            @NonNull RoomWithLights newItem) {
                return oldItem.getRoom().equals(newItem.getRoom()) &&
                       oldItem.getLightDevices().equals(newItem.getLightDevices());
            }
        };

    @NonNull
    @Override
    public RoomLightsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoomLightsBinding binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.getContext()),
            R.layout.item_room_lights,
            parent,
            false
        );
        return new RoomLightsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomLightsViewHolder holder, int position) {
        RoomWithLights roomWithLights = getItem(position);
        holder.bind(roomWithLights, clickListener);
    }

    /**
     * Establece el listener para clicks en elementos
     */
    public void setOnRoomLightClickListener(OnRoomLightClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * ViewHolder para elementos de habitación con luces
     */
    static class RoomLightsViewHolder extends RecyclerView.ViewHolder {
        private final ItemRoomLightsBinding binding;
        private LightDevicesAdapter devicesAdapter;

        public RoomLightsViewHolder(@NonNull ItemRoomLightsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            // Configurar adaptador para dispositivos
            devicesAdapter = new LightDevicesAdapter();
            binding.recyclerViewDevices.setAdapter(devicesAdapter);
        }

        /**
         * Vincula los datos de la habitación con la vista
         */
        public void bind(RoomWithLights roomWithLights, OnRoomLightClickListener clickListener) {
            binding.setRoomWithLights(roomWithLights);
            binding.executePendingBindings();

            // Configurar adaptador de dispositivos
            devicesAdapter.submitList(roomWithLights.getLightDevices());
            devicesAdapter.setOnDeviceClickListener(new LightDevicesAdapter.OnDeviceClickListener() {
                @Override
                public void onDeviceToggle(String deviceId) {
                    if (clickListener != null) {
                        clickListener.onDeviceToggle(deviceId);
                    }
                }

                @Override
                public void onIntensityChange(String deviceId, float intensity) {
                    if (clickListener != null) {
                        clickListener.onDeviceIntensityChange(deviceId, intensity);
                    }
                }
            });

            // Configurar listeners de la habitación
            binding.buttonRoomAllOn.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onRoomToggle(roomWithLights.getRoom().getId(), true);
                }
            });

            binding.buttonRoomAllOff.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onRoomToggle(roomWithLights.getRoom().getId(), false);
                }
            });

            // Actualizar estado visual del header
            updateRoomHeader(roomWithLights);
        }

        /**
         * Actualiza la información del header de la habitación
         */
        private void updateRoomHeader(RoomWithLights roomWithLights) {
            // Actualizar icono del tipo de habitación
            binding.imageRoomIcon.setImageResource(
                getRoomIconResource(roomWithLights.getRoom().getType())
            );

            // Actualizar estadísticas
            binding.textLightsCount.setText(
                String.format("%d luces", roomWithLights.getTotalLights())
            );

            binding.textLightsStatus.setText(roomWithLights.getLightingStatus());

            // Actualizar indicador de intensidad promedio
            int avgIntensity = roomWithLights.getAverageLightIntensity();
            binding.progressRoomIntensity.setProgress(avgIntensity);
            binding.textRoomIntensity.setText(String.format("%d%%", avgIntensity));

            // Mostrar indicador de luces desconectadas si las hay
            if (roomWithLights.hasDisconnectedLights()) {
                binding.textDisconnectedLights.setVisibility(android.view.View.VISIBLE);
                binding.textDisconnectedLights.setText(
                    String.format("%d desconectadas", roomWithLights.getDisconnectedLights())
                );
            } else {
                binding.textDisconnectedLights.setVisibility(android.view.View.GONE);
            }

            // Actualizar estado de los botones
            boolean hasLightsOn = roomWithLights.hasLightsOn();
            binding.buttonRoomAllOff.setEnabled(hasLightsOn);
            binding.buttonRoomAllOn.setEnabled(!roomWithLights.allLightsOn());
        }

        /**
         * Obtiene el recurso de icono para el tipo de habitación
         */
        private int getRoomIconResource(com.pdm.domohouse.data.model.RoomType roomType) {
            switch (roomType) {
                case LIVING_ROOM:
                    return R.drawable.ic_room_living;
                case KITCHEN:
                    return R.drawable.ic_room_kitchen;
                case BEDROOM:
                    return R.drawable.ic_room_bedroom;
                case BATHROOM:
                    return R.drawable.ic_room_bathroom;
                case OFFICE:
                    return R.drawable.ic_room_office;
                case GARAGE:
                    return R.drawable.ic_room_garage;
                default:
                    return R.drawable.ic_room_other;
            }
        }
    }
}