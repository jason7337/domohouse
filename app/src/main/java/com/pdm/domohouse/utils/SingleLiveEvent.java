package com.pdm.domohouse.utils;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Una clase LiveData que se usa para eventos que se deben consumir una sola vez
 * como mostrar un Snackbar, navegar, etc.
 * 
 * Esto evita un problema común con eventos en configuraciones donde
 * el observador puede ejecutarse cuando no debería.
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {

    private final AtomicBoolean mPending = new AtomicBoolean(false);

    /**
     * Observa el LiveData y ejecuta el observador solo una vez por evento
     */
    @MainThread
    public void observe(LifecycleOwner owner, final Observer<? super T> observer) {
        super.observe(owner, t -> {
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t);
            }
        });
    }

    /**
     * Establece el valor. Si hay observadores activos, el valor será despachado inmediatamente.
     */
    @MainThread
    public void setValue(@Nullable T t) {
        mPending.set(true);
        super.setValue(t);
    }

    /**
     * Usado para casos donde T es Void, para hacer el código más limpio
     */
    @MainThread
    public void call() {
        setValue(null);
    }
}