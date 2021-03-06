package com.feathersjs.client.plugins.providers;

import java.net.URISyntaxException;
import java.util.Map;

import org.json.JSONObject;

import com.feathersjs.client.Feathers;
import com.feathersjs.client.Log;
import com.feathersjs.client.plugins.authentication.AuthResponse;
import com.feathersjs.client.plugins.authentication.AuthenticationRequest;
import com.feathersjs.client.plugins.authentication.AuthenticationResult;
import com.feathersjs.client.plugins.authentication.FeathersAuthentication;
import com.feathersjs.client.service.FeathersService.FeathersCallback;
import com.feathersjs.client.service.OnCreatedCallback;
import com.feathersjs.client.service.OnEventCallback;
import com.feathersjs.client.service.OnPatchedCallback;
import com.feathersjs.client.service.OnRemovedCallback;
import com.feathersjs.client.service.OnUpdatedCallback;
import com.feathersjs.client.service.Result;
import com.feathersjs.client.service.ServiceEvent;
import com.feathersjs.client.utilities.Serialization;
import com.google.gson.GsonBuilder;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class FeathersSocketClient extends FeathersBaseClient {

    private final String TAG = "feathers-socket";
    private FeathersSocketIO.Options mOptions = new FeathersSocketIO.Options();
    private Socket mSocket;
    private AuthenticationRequest mAuthenticationRequest;

    public FeathersSocketClient(Feathers feathers, AuthenticationRequest authentication, Socket socket) {
        mFeathers = feathers;
        mAuthenticationRequest = authentication;
        gson = new GsonBuilder().create();
        mOptions = new FeathersSocketIO.Options();
        mSocket = socket;

        connectSocket();
    }

    private void connectSocket() {
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            mSocket.close();
        }
        IO.Options opts = new IO.Options();
        opts.forceNew = mOptions.forceNew;
        opts.reconnection = mOptions.reconnection;

        try {
            mSocket = IO.socket(mFeathers.getBaseUrl(), opts);
        } catch (URISyntaxException exception) {
            Log.e("FeathersSocket", "Error parsing URL", exception);
        }

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            	authenticate();
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("SOCKET:", "Disconnected");
            }
        }).on("unauthorized", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("SOCKET:", "unauthorized");
            }
        }).on("authenticated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("SOCKET:", "authenticated");
            }
        });
        mSocket.connect();
    }

    protected void authenticate() {
		create(null, "authentication", mAuthenticationRequest, new FeathersCallback<AuthenticationResult>() {

			@Override
			public void onError(String errorMessage) {
				Log.e("SOCKET:", "Authentication failed");
			}

			@Override
			public void onSuccess(AuthenticationResult t) {
				Log.e("SOCKET:", "Authenticated");
			}
		}, AuthenticationResult.class);
	}

	@Override
    public <J> void find(String baseUrl, String serviceName, Map<String, String> params, final FeathersCallback<Result<J>> cb, final Class<J> jClass) {
        JSONObject obj = new JSONObject(params);
        mSocket.emit("find", serviceName, obj, getListHandler(cb, ServiceEvent.FIND, jClass));
    }

    @Override
    public <J> void get(String baseUrl, String serviceName, String id, final FeathersCallback<J> cb, final Class<J> jClass) {
        mSocket.emit("get", serviceName, id, getHandler(cb, ServiceEvent.GET, jClass));
    }

    @Override
    public <J> void remove(String baseUrl, String serviceName, String id, final FeathersCallback<J> cb, final Class<J> jClass) {
        mSocket.emit("remove", serviceName, id, getHandler(cb, ServiceEvent.REMOVE, jClass));
    }

    @Override
    public <J> void update(String baseUrl, String serviceName, String id, J item, final FeathersCallback<J> cb, final Class<J> jClass) {
        JSONObject obj = Serialization.serialize(item, gson);
        mSocket.emit("update", serviceName, id, obj, getHandler(cb, ServiceEvent.UPDATE, jClass));
    }

    @Override
    public <J> void patch(String baseUrl, String serviceName, String id, JSONObject item, final FeathersCallback<J> cb, final Class<J> jClass) {
        mSocket.emit("patch", serviceName, id, item, getHandler(cb, ServiceEvent.PATCH, jClass));
    }

    @Override
    public <J, K> void create(String baseUrl, String serviceName, J item, final FeathersCallback<K> cb, final Class<K> jClass) {
        JSONObject obj = Serialization.serialize(item, gson);
        mSocket.emit("create", serviceName, obj, getHandler(cb, ServiceEvent.CREATE, jClass));
    }


    private <J> void listenForEvent(String eventName, String serviceName, final OnEventCallback<J> callback, final Class<J> jClass) {
        mSocket.on(serviceName + " " + eventName,
                new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        if (args.length == 1) {
                            JSONObject obj = (JSONObject) args[0];
                            if (callback instanceof OnCreatedCallback) {
                                ((OnCreatedCallback) callback).onCreated(Serialization.deserializeObject(obj, jClass, gson));
                            }

                            if (callback instanceof OnUpdatedCallback) {
                                ((OnUpdatedCallback) callback).onUpdated(Serialization.deserializeObject(obj, jClass, gson));
                            }

                            if (callback instanceof OnRemovedCallback) {
                                ((OnRemovedCallback) callback).onRemoved(Serialization.deserializeObject(obj, jClass, gson));
                            }

                            if (callback instanceof OnPatchedCallback) {
                                ((OnPatchedCallback) callback).onPatched(Serialization.deserializeObject(obj, jClass, gson));
                            }
                        }
                    }
                });
    }


    //    @Override
    public <J> void onCreated(String serviceName, final OnEventCallback<J> callback, final Class<J> jClass) {
        listenForEvent("created", serviceName, callback, jClass);
    }

    //    @Override
    public <J> void onUpdated(String serviceName, final OnEventCallback<J> callback, final Class<J> jClass) {
        listenForEvent("updated", serviceName, callback, jClass);
    }

    //    @Override
    public <J> void onRemoved(String serviceName, final OnEventCallback<J> callback, final Class<J> jClass) {
        listenForEvent("removed", serviceName, callback, jClass);
    }

    //    @Override
    public <J> void onPatched(String serviceName, final OnEventCallback<J> callback, final Class<J> jClass) {
        listenForEvent("patched", serviceName, callback, jClass);
    }


    // Authentication
    public <J> void authenticate(final JSONObject options, final FeathersCallback<AuthResponse<J>> cb, final Class<J> jClass) {
        mSocket.once("unauthorized", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                cb.onError("");
            }
        });
        mSocket.once("authenticated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject responseData = (JSONObject) args[0];
                AuthResponse<J> authResponse = Serialization.deserializeAuthResponse(responseData, jClass, gson);
                cb.onSuccess(authResponse);
            }
        });
        mSocket.emit("authenticate", options);
    }

    public void logout() {
        mSocket.emit("logout", new Ack() {
            @Override
            public void call(Object... args) {

            }
        });
    }

    private <J> Ack getHandler(final FeathersCallback<J> cb, final ServiceEvent serviceEvent, final Class<J> jClass) {
        return new Ack() {
            @Override
            public void call(Object... args) {
                if (args.length == 1) {


                    JSONObject error = (JSONObject) args[0];
                    cb.onError(error.optString("message", error.toString()));
                } else {
                    JSONObject responseData = (JSONObject) args[1];

                    Log.d(TAG, serviceEvent + ":onSuccess:" + responseData);
                    J item = null;
                    if (jClass.isInstance(responseData)) {
                        item = (J) responseData;
                    } else {
                        item = gson.fromJson(responseData.toString(), jClass);
                    }
                    cb.onSuccess(item);

                    sendEvent(serviceEvent, item);
                }
            }
        };
    }


    private <J> Ack getListHandler(final FeathersCallback<Result<J>> cb, final ServiceEvent serviceEvent, final Class<J> jClass) {
        return new Ack() {
            @Override
            public void call(Object... args) {
                if (args.length == 1) {
                    JSONObject error = (JSONObject) args[0];
//                    Result result = Serialization.deserializeArray(array, mModelClass, gson);
                    cb.onError(error.optString("message", error.toString()));
                } else {
                    JSONObject array = (JSONObject) args[1];
                    Result<J> result = Serialization.deserializeArray(array, jClass, gson);
                    cb.onSuccess(result);
                }
            }
        };
    }


//    private void listenForEvent(final String eventName, final OnEventCallback<T> callback) {
//        socket.on(mServicePath + " " + eventName, new Emitter.Listener() {
//            @Override
//            public void call(Object... args) {
//                if (args.length == 1) {
//                    JSONObject obj = (JSONObject) args[0];
//                    T item = gson.fromJson(obj.toString(), mModelClass);
//                    Log.d("socket:" + eventName + ":", obj.toString());
//                    callback.onSuccess(item);
//                }
//            }
//        });
//    }


}
