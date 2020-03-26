package com.feathersjs.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.feathersjs.client.service.FeathersService.FeathersCallback;
import com.feathersjs.client.plugins.authentication.AuthResponse;
import com.feathersjs.client.service.Result;

public class FeathersTools {
	static class Callback<N> implements FeathersCallback<N> {
		private CountDownLatch latch;
		private String error = null;
		private N result = null;

		public Callback(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void onError(String errorMessage) {
			error = errorMessage;
			latch.countDown();
		}

		@Override
		public void onSuccess(N t) {
			result = t;
			latch.countDown();
		}
		
	}
	
	public static volatile int timeoutMillis = 5000;

	public static <N> N await(Consumer<FeathersCallback<N>> fn, Class<N> _clazz) throws InterruptedException, FeathersException {
		CountDownLatch latch = new CountDownLatch(1);
		Callback<N> cb = new Callback<>(latch);
		fn.accept(cb);
		if (!latch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
			throw new Timeout("Server did not respond within " + timeoutMillis + "ms.");
		};
		if (cb.error != null) throw new FeathersException(cb.error);
		return cb.result;
	}

	public static <N> Result<N> awaitResult(Consumer<FeathersCallback<Result<N>>> fn, Class<N> _clazz) throws InterruptedException, FeathersException {
		CountDownLatch latch = new CountDownLatch(1);
		Callback<Result<N>> cb = new Callback<>(latch);
		fn.accept(cb);
		latch.await();
		if (cb.error != null) throw new FeathersException(cb.error);
		return cb.result;
	}

	public static <N> AuthResponse<N> awaitAuthResponse(Consumer<FeathersCallback<AuthResponse<N>>> fn, Class<N> _clazz) throws InterruptedException, FeathersException {
		CountDownLatch latch = new CountDownLatch(1);
		Callback<AuthResponse<N>> cb = new Callback<>(latch);
		fn.accept(cb);
		latch.await();
		if (cb.error != null) throw new FeathersException(cb.error);
		return cb.result;
	}
	
}
