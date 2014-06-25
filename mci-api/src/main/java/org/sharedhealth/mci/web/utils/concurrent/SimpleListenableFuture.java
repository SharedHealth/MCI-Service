package org.sharedhealth.mci.web.utils.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.springframework.util.concurrent.FutureAdapter;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;

/**
 * It is necessary that the adapt method does not take long to execute since we are using the same thread executor.
 *
 * @param <T> Target type of the promised value
 * @param <S> Source type of the promised value
 */
public abstract class SimpleListenableFuture<T, S> extends FutureAdapter<T, S> implements org.springframework.util.concurrent.ListenableFuture<T> {

    protected SimpleListenableFuture(ListenableFuture<S> adaptee) {
        super(adaptee);
    }

    @Override
    public void addCallback(final ListenableFutureCallback<? super T> callback) {
        final ListenableFuture adaptee = (ListenableFuture) getAdaptee();
        adaptee.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onSuccess(get());
                } catch (InterruptedException | ExecutionException e) {
                    callback.onFailure(e);
                }
            }
        }, MoreExecutors.sameThreadExecutor());
    }
}
