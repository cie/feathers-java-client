package com.feathersjs.client.service;

public abstract class OnRemovedCallback <T> extends OnEventCallback {
    public abstract void onRemoved(T t);
}